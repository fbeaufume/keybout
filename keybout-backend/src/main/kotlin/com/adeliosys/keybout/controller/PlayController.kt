package com.adeliosys.keybout.controller

import com.adeliosys.keybout.controller.PlayController.Companion.GAME_ID
import com.adeliosys.keybout.controller.PlayController.Companion.NAME
import com.adeliosys.keybout.controller.PlayController.Companion.STATE
import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Constants.ACTION_CLAIM_WORD
import com.adeliosys.keybout.model.Constants.ACTION_CONNECT
import com.adeliosys.keybout.model.Constants.ACTION_CREATE_GAME
import com.adeliosys.keybout.model.Constants.ACTION_DELETE_GAME
import com.adeliosys.keybout.model.Constants.ACTION_JOIN_GAME
import com.adeliosys.keybout.model.Constants.ACTION_LEAVE_GAME
import com.adeliosys.keybout.model.Constants.ACTION_START_GAME
import com.adeliosys.keybout.model.Constants.ACTION_START_ROUND
import com.adeliosys.keybout.service.GameBuilder
import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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

    companion object {
        const val STATE = "state"
        const val NAME = "name"
        const val GAME_ID = "GAME_ID"
    }

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
     * WebSocket sessions for a running game.
     * The key is the game ID.
     * Concurrency is handled by synchronizing on this object.
     */
    private val runningGames = mutableMapOf<Long, Game>()

    private val gson = Gson()

    @Autowired
    private lateinit var gameBuilder: GameBuilder

    /**
     * Executor used to start games with a delay, to display a countdown
     * in the UI.
     */
    private val executor = Executors.newSingleThreadScheduledExecutor()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("Opened connection '{}'", session.id)
        session.setState(ClientState.UNIDENTIFIED, 0, logger)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            logger.debug("Received message '{}' for connection '{}'", message.payload, session.id)

            val action = Action(message.payload)

            when (session.getState()) {
                ClientState.UNIDENTIFIED -> {
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
                                        logger.debug("Using name '{}' for connection '{}', user count is {}", name, session.id, userNames.count())

                                        session.setUserName(name)
                                        session.setState(ClientState.IDENTIFIED, 0, logger)

                                        synchronized(this) {
                                            gamesSessions[name] = session
                                            sendMessage(session, GamesListNotification(declaredGames.values))
                                        }
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
                                        session.getUserName(),
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
                                    logger.warn("Invalid game ID is message '{}' for connection '{}'", message, session.id)
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
                ClientState.IN_GAME -> {
                    when (action.command) {
                        ACTION_CLAIM_WORD -> {
                            if (action.checkArgumentsCount(1)) {
                                claimWord(session, action.arguments[0])
                            }
                        }
                        ACTION_START_ROUND -> startRound(session)
                        else -> invalidMessage(session, message)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Caught an exception during message processing", e)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        when (session.getState()) {
            ClientState.UNIDENTIFIED -> {
            }
            ClientState.IDENTIFIED -> {
            }
            ClientState.CREATED -> deleteGame(session)
            ClientState.JOINED -> leaveGame(session)
            ClientState.IN_GAME -> {
                // TODO FBE
            }
            // TODO FBE other cases
        }

        userNames.remove(session.getUserName())

        logger.debug("Closed connection '{}' with code {} and reason '{}', user count is {}",
                session.id,
                status.code,
                status.reason.orEmpty(),
                userNames.count())
    }

    private fun invalidMessage(session: WebSocketSession, message: TextMessage) {
        logger.warn("Invalid message '{}' for state {} and connection '{}'", message.payload, session.id, session.getState())
    }

    private fun createGame(session: WebSocketSession, gameDescriptor: GameDescriptor) {
        synchronized(this) {
            session.setState(ClientState.CREATED, gameDescriptor.id, logger)
            declaredGames[gameDescriptor.id] = gameDescriptor
            sendMessage(gamesSessions.values, GamesListNotification(declaredGames.values))
        }
    }

    private fun deleteGame(session: WebSocketSession) {
        synchronized(this) {
            val gameDescriptor = declaredGames[session.getGameId()]
            if (gameDescriptor != null) {
                declaredGames.remove(session.getGameId())

                // Process the game creator
                session.setState(ClientState.IDENTIFIED, 0, logger)

                // Process the joined players
                gameDescriptor.players.forEach { n -> gamesSessions[n]?.setState(ClientState.IDENTIFIED, 0, logger) }

                sendMessage(gamesSessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    private fun joinGame(session: WebSocketSession, gameId: Long) {
        synchronized(this) {
            if (declaredGames[gameId]?.players?.add(session.getUserName()) == true) {
                session.setState(ClientState.JOINED, gameId, logger)
                sendMessage(gamesSessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    private fun leaveGame(session: WebSocketSession) {
        synchronized(this) {
            if (declaredGames[session.getGameId()]?.players?.remove(session.getUserName()) == true) {
                session.setState(ClientState.IDENTIFIED, 0, logger)
                sendMessage(gamesSessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    private fun startGame(session: WebSocketSession) {
        synchronized(this) {
            val descriptor = declaredGames[session.getGameId()]
            if (descriptor != null) {
                declaredGames.remove(session.getGameId())

                // Move creator user to the running game
                gamesSessions.remove(session.getUserName())
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
                game.initializeRound(gameBuilder.generateWords(game.language, game.wordCount))
                runningGames[game.id] = game

                // Update the state of all players
                game.players.forEach { s -> s.setState(ClientState.IN_GAME, game.id, logger) }

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
        val game = runningGames[session.getGameId()]
        if (game != null) {
            val map = game.claimWord(session.getUserName(), label)
            if (map.isNotEmpty()) {
                if (game.isRoundOver()) {
                    sendMessage(game.players, ScoresNotification(map, game.getRoundScoresDto(), game.manager))
                } else {
                    sendMessage(game.players, WordsListNotification(map))
                }
            }
        }
    }

    private fun startRound(session: WebSocketSession) {
        val game = runningGames[session.getGameId()]
        if (game != null) {
            game.initializeRound(gameBuilder.generateWords(game.language, game.wordCount))

            notifyRoundStart(game)
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
            logger.error("Failed to send message to connection '{}': {}", session.id, e.toString())
        }
    }
}

// Several extensions to WebSocketSession

fun WebSocketSession.getUserName(): String = attributes[NAME] as String

fun WebSocketSession.setUserName(name: String) {
    attributes[NAME] = name
}

fun WebSocketSession.getState(): ClientState = attributes[STATE] as ClientState

fun WebSocketSession.setState(state: ClientState, gameId: Long, logger: Logger) {
    attributes[STATE] = state
    attributes[GAME_ID] = gameId
    logger.debug("Changed state to '{}' and game {} for connection '{}'", state, gameId, id)
}

fun WebSocketSession.getGameId(): Long = attributes[GAME_ID] as Long
