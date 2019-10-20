package com.adeliosys.keybout.controller

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Constants.ACTION_CLAIM_WORD
import com.adeliosys.keybout.model.Constants.ACTION_CONNECT
import com.adeliosys.keybout.model.Constants.ACTION_CREATE_GAME
import com.adeliosys.keybout.model.Constants.ACTION_DELETE_GAME
import com.adeliosys.keybout.model.Constants.ACTION_JOIN_GAME
import com.adeliosys.keybout.model.Constants.ACTION_LEAVE_GAME
import com.adeliosys.keybout.model.Constants.ACTION_QUIT_GAME
import com.adeliosys.keybout.model.Constants.ACTION_START_GAME
import com.adeliosys.keybout.model.Constants.ACTION_START_ROUND
import com.adeliosys.keybout.service.WordGenerator
import com.adeliosys.keybout.util.*
import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

/**
 * Receive WebSocket messages and trigger their business processing
 * and update the client state.
 *
 * A WebSocket session contains several attributes:
 * - The server side state
 * - The user name
 * - The game ID (0 if the user is not related to any game, or the actual game ID)
 */
@Service
class PlayController : TextWebSocketHandler() {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Identified user names, used to check if a new user name is available.
     * An element is added when a user provides an valid and available name.
     * An element is removed when the connection is closed.
     */
    private val userNames = ConcurrentHashMap.newKeySet<String>()

    /**
     * WebSocket sessions of users getting/creating/joining/etc games.
     * The key is the user name.
     * Concurrency is handled by synchronizing on this class instance.
     */
    private val gamesSessions = mutableMapOf<String, WebSocketSession>()

    /**
     * Declared games, i.e. games that were created but not started yet.
     * The key is the game ID.
     * Concurrency is handled by synchronizing on this class instance.
     */
    private val declaredGames = mutableMapOf<Long, GameDescriptor>()

    /**
     * Running games.
     * The key is the game ID.
     * Concurrency is handled by synchronizing on this object.
     */
    private val runningGames = mutableMapOf<Long, Game>()

    private val gson = Gson()

    @Value("\${application.latency:0}")
    private var latency = 0L

    @Autowired
    private lateinit var wordGenerator: WordGenerator

    /**
     * Executor used to start games with a delay, to display a countdown
     * in the UI.
     */
    private val executor = Executors.newSingleThreadScheduledExecutor()

    @PostConstruct
    private fun init() {
        logger.info("Using latency of {} ms", latency)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        session.userName = ""
        session.setState(logger, ClientState.OPENED, 0)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            if (latency > 0) {
                Thread.sleep(latency)
            }

            logger.trace("Received message '{}' for {}", message.payload, session.description)

            val action = Action(message.payload)

            when (session.state) {
                ClientState.OPENED -> {
                    when (action.command) {
                        ACTION_CONNECT -> {
                            if (action.checkMinimumArgumentsCount(1)) {
                                val name = action.rawArguments

                                when {
                                    // Check the name length
                                    name.length > 32 -> {
                                        sendMessage(session, TooLongNameNotification())
                                    }
                                    // Check the name availability
                                    userNames.add(name) -> {
                                        session.userName = name
                                        goToGamesList(session)
                                    }
                                    else ->
                                        sendMessage(session, UsedNameNotification())
                                }
                            } else {
                                sendMessage(session, IncorrectNameNotification())
                            }
                        }
                        else -> invalidMessage(session, message)
                    }
                }
                ClientState.IDENTIFIED -> {
                    when (action.command) {
                        ACTION_CREATE_GAME -> {
                            if (action.checkArgumentsCount(4)) {
                                val gameDescriptor = GameDescriptor(
                                        session.userName,
                                        action.arguments[0],
                                        try {
                                            action.arguments[1].toInt()
                                        } catch (e: NumberFormatException) {
                                            1
                                        },
                                        try {
                                            action.arguments[2].toInt()
                                        } catch (e: NumberFormatException) {
                                            10
                                        },
                                        action.arguments[3])

                                createGame(session, gameDescriptor)
                            }
                        }
                        ACTION_JOIN_GAME -> {
                            if (action.checkArgumentsCount(1)) {
                                try {
                                    joinGame(session, action.arguments[0].toLong())
                                } catch (e: NumberFormatException) {
                                    logger.warn("Invalid game in message '{}' for {}", message, session.description)
                                }
                            }
                        }
                        else -> invalidMessage(session, message)
                    }
                }
                ClientState.CREATED -> {
                    when (action.command) {
                        ACTION_DELETE_GAME -> deleteGame(session)
                        ACTION_START_GAME -> startGame(session)
                        else -> invalidMessage(session, message)
                    }
                }
                ClientState.JOINED -> {
                    when (action.command) {
                        ACTION_LEAVE_GAME -> leaveGame(session)
                        else -> invalidMessage(session, message)
                    }
                }
                ClientState.PLAYING -> {
                    when (action.command) {
                        ACTION_CLAIM_WORD -> {
                            if (action.checkArgumentsCount(1)) {
                                claimWord(session, action.arguments[0])
                            }
                        }
                        ACTION_START_ROUND -> startRound(session)
                        ACTION_QUIT_GAME -> goToGamesList(session)
                        else -> invalidMessage(session, message)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Caught an exception during message processing", e)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        synchronized(this) {
            gamesSessions.remove(session.userName)
        }

        when (session.state) {
            ClientState.OPENED -> {
            }
            ClientState.IDENTIFIED ->  {
            }
            ClientState.CREATED -> deleteGame(session)
            ClientState.JOINED -> leaveGame(session)
            ClientState.PLAYING -> disconnectFromGame(session)
        }

        userNames.remove(session.userName)

        logger.debug("Closed {} with code {} and reason '{}', user count is {}",
                session.description,
                status.code,
                status.reason.orEmpty(),
                userNames.count())
    }

    private fun invalidMessage(session: WebSocketSession, message: TextMessage) {
        logger.warn("Invalid message '{}' for state {} of {}", message.payload, session.state, session.description)
    }

    /**
     * Called after a user identification or after a user quit a game, to go to the games list.
     */
    private fun goToGamesList(session: WebSocketSession) {
        synchronized(this) {
            session.setState(logger, ClientState.IDENTIFIED, 0, userNames.count())
            gamesSessions[session.userName] = session
            sendMessage(session, GamesListNotification(declaredGames.values))
        }
    }

    private fun createGame(session: WebSocketSession, gameDescriptor: GameDescriptor) {
        synchronized(this) {
            session.setState(logger, ClientState.CREATED, gameDescriptor.id)
            declaredGames[gameDescriptor.id] = gameDescriptor
            sendMessage(gamesSessions.values, GamesListNotification(declaredGames.values))
        }
    }

    private fun deleteGame(session: WebSocketSession) {
        synchronized(this) {
            val gameDescriptor = declaredGames[session.gameId]
            if (gameDescriptor != null) {
                declaredGames.remove(session.gameId)

                // Process the game creator
                session.setState(logger, ClientState.IDENTIFIED, 0)

                // Process the joined players
                gameDescriptor.players.forEach { n -> gamesSessions[n]?.setState(logger, ClientState.IDENTIFIED, 0) }

                sendMessage(gamesSessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    private fun joinGame(session: WebSocketSession, gameId: Long) {
        synchronized(this) {
            if (declaredGames[gameId]?.players?.add(session.userName) == true) {
                session.setState(logger, ClientState.JOINED, gameId)
                sendMessage(gamesSessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    private fun leaveGame(session: WebSocketSession) {
        synchronized(this) {
            if (declaredGames[session.gameId]?.players?.remove(session.userName) == true) {
                session.setState(logger, ClientState.IDENTIFIED, 0)
                sendMessage(gamesSessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    private fun startGame(session: WebSocketSession) {
        synchronized(this) {
            val descriptor = declaredGames[session.gameId]
            if (descriptor != null) {
                declaredGames.remove(session.gameId)

                // Move creator user to the running game
                gamesSessions.remove(session.userName)
                val players = mutableListOf(session)

                // Move joined users to the running game
                players.addAll(descriptor.players.mapNotNull { n -> gamesSessions.remove(n) })

                // Record the running game
                val game = Game(
                        descriptor.id,
                        descriptor.rounds,
                        descriptor.words,
                        descriptor.language,
                        descriptor.creator,
                        players)
                game.initializeRound(wordGenerator.generateWords(game.language, game.wordCount))
                runningGames[game.id] = game

                logger.info("Created game #{} ({} player{}, 'capture' type, {} round{}, {} '{}' words), running game count is {}",
                        descriptor.id,
                        players.size,
                        if (players.size > 1) "s" else "",
                        descriptor.rounds,
                        if (descriptor.rounds > 1) "s" else "",
                        descriptor.words,
                        descriptor.language,
                        runningGames.size)

                // Update the state of all players
                game.players.forEach { s -> s.setState(logger, ClientState.PLAYING, game.id) }

                // Notify non playing users
                sendMessage(gamesSessions.values, GamesListNotification(declaredGames.values))

                notifyRoundStart(game)
            }
        }
    }

    private fun notifyRoundStart(game: Game) {
        // Notify playing users to display the countdown
        sendMessage(game.players, GameStartNotification())

        // Notify playing users when the round begins
        executor.schedule({ sendMessage(game.players, WordsListNotification(game.getWordsDto())) }, 5L, TimeUnit.SECONDS)
    }

    private fun claimWord(session: WebSocketSession, label: String) {
        val game = runningGames[session.gameId]
        if (game != null) {
            val map = game.claimWord(session.userName, label)
            if (map.isNotEmpty()) {
                if (game.isRoundOver()) {
                    sendMessage(game.players, ScoresNotification(map, game))

                    if (game.isGameOver()) {
                        deleteRunningGame(game.id)
                    }
                } else {
                    sendMessage(game.players, WordsListNotification(map))
                }
            }
        }
    }

    private fun deleteRunningGame(gameId: Long) {
        runningGames.remove(gameId)
        logger.info("Deleted game #{}, running game count is {}", gameId, runningGames.size)
    }

    private fun startRound(session: WebSocketSession) {
        val game = runningGames[session.gameId]
        if (game != null) {
            game.initializeRound(wordGenerator.generateWords(game.language, game.wordCount))

            notifyRoundStart(game)
        }
    }

    private fun disconnectFromGame(session: WebSocketSession) {
        synchronized(this) {
            val game = runningGames[session.gameId]
            if (game != null) {
                val (changed, manager, empty) = game.removeUser(session)

                if (empty) {
                    // No user left, remove the game
                    deleteRunningGame(game.id)
                } else if (changed) {
                    // Notify the users about the new manager
                    sendMessage(game.players, ManagerNotification(manager))
                }
            }
        }
    }

    /**
     * Send a message to multiple clients.
     */
    private fun sendMessage(sessions: Collection<WebSocketSession>, obj: Any) {
        val msg = gson.toJson(obj)
        sessions.forEach { s -> sendStringMessage(s, msg) }
    }

    /**
     * Send a message to one client.
     */
    private fun sendMessage(session: WebSocketSession, obj: Any) {
        sendStringMessage(session, gson.toJson(obj))
    }

    private fun sendStringMessage(session: WebSocketSession, msg: String) {
        try {
            session.sendMessage(TextMessage(msg))
        } catch (e: Exception) {
            val shortenedMsg = if (msg.length > 40) msg.substring(0, 40) + "..." else msg
            logger.error("Failed to send message '{}' to {}: {}", shortenedMsg, session.description, e.toString())
        }
    }
}
