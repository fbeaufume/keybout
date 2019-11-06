package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.util.*
import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Manage the declared games and users state.
 */
@Service
class PlayService {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * WebSocket sessions of users in the lobby.
     * The key is the user name.
     * Concurrency is handled by synchronizing on the class instance.
     */
    private val lobbySessions = mutableMapOf<String, WebSocketSession>()

    /**
     * Declared games, i.e. games that were created but not started yet.
     * The key is the game ID.
     * Concurrency is handled by synchronizing on the class instance.
     */
    private val declaredGames = mutableMapOf<Long, GameDescriptor>()

    /**
     * Running games.
     * The key is the game ID.
     * Concurrency is handled by synchronizing on the class instance.
     */
    private val runningGames = mutableMapOf<Long, GameService>()

    private val gson = Gson()

    /**
     * Executor used to start games with a delay, to display a countdown
     * in the UI.
     */
    private val executor = Executors.newSingleThreadScheduledExecutor()

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    /**
     * Called after a user identification or after a user quit a game, to go to the lobby.
     */
    fun goToLobby(session: WebSocketSession) {
        synchronized(this) {
            session.setState(ClientState.LOBBY, 0, logger)
            lobbySessions[session.userName] = session
            session.sendObjectMessage(GamesListNotification(declaredGames.values), logger)
        }
    }

    fun createGame(session: WebSocketSession, gameDescriptor: GameDescriptor) {
        synchronized(this) {
            session.setState(ClientState.CREATED, gameDescriptor.id, logger)
            declaredGames[gameDescriptor.id] = gameDescriptor
            sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
        }
    }

    fun deleteGame(session: WebSocketSession) {
        synchronized(this) {
            val gameDescriptor = declaredGames[session.gameId]
            if (gameDescriptor != null) {
                declaredGames.remove(session.gameId)

                // Process the game creator
                session.setState(ClientState.LOBBY, 0, logger)

                // Process the joined players
                gameDescriptor.players.forEach { n -> lobbySessions[n]?.setState(ClientState.LOBBY, 0, logger) }

                sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    fun joinGame(session: WebSocketSession, gameId: Long) {
        synchronized(this) {
            if (declaredGames[gameId]?.players?.add(session.userName) == true) {
                session.setState(ClientState.JOINED, gameId, logger)
                sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    fun leaveGame(session: WebSocketSession) {
        synchronized(this) {
            if (declaredGames[session.gameId]?.players?.remove(session.userName) == true) {
                session.setState(ClientState.LOBBY, 0, logger)
                sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    fun startGame(session: WebSocketSession) {
        synchronized(this) {
            val descriptor = declaredGames[session.gameId]
            if (descriptor != null) {
                declaredGames.remove(session.gameId)

                // Move creator user to the running game
                lobbySessions.remove(session.userName)
                val players = mutableListOf(session)

                // Move joined users to the running game
                players.addAll(descriptor.players.mapNotNull { n -> lobbySessions.remove(n) })

                // Record the running game
                val game = applicationContext.getBean(GameService::class.java)
                game.initializeGame(descriptor, players)
                game.initializeRound()
                runningGames[game.id] = game

                logger.info("Started game #{} ({} player{}, 'capture' type, {} round{}, {} {} '{}' words), running game count is {}",
                        descriptor.id,
                        players.size,
                        if (players.size > 1) "s" else "",
                        descriptor.rounds,
                        if (descriptor.rounds > 1) "s" else "",
                        descriptor.wordCount,
                        descriptor.wordLength,
                        descriptor.language,
                        runningGames.size)

                // Update the state of all players
                game.players.forEach { s -> s.setState(ClientState.PLAYING, game.id, logger) }

                // Notify non playing users
                sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))

                notifyRoundStart(game)
            }
        }
    }

    fun notifyRoundStart(game: GameService) {
        // Notify playing users to display the countdown
        sendMessage(game.players, GameStartNotification())

        // Notify playing users when the round begins
        executor.schedule({ sendMessage(game.players, WordsListNotification(game.getWordsDto())) }, 5L, TimeUnit.SECONDS)
    }

    fun claimWord(session: WebSocketSession, label: String) {
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

    fun deleteRunningGame(gameId: Long) {
        runningGames.remove(gameId)
        logger.info("Ended game #{}, running game count is {}", gameId, runningGames.size)
    }

    fun startRound(session: WebSocketSession) {
        val game = runningGames[session.gameId]
        if (game != null) {
            game.initializeRound()
            notifyRoundStart(game)
        }
    }

    fun disconnect(session: WebSocketSession) {
        synchronized(this) {
            lobbySessions.remove(session.userName)
        }

        when (session.state) {
            ClientState.OPENED -> {
            }
            ClientState.LOBBY -> {
            }
            ClientState.CREATED -> deleteGame(session)
            ClientState.JOINED -> leaveGame(session)
            ClientState.PLAYING -> disconnectFromGame(session)
        }
    }

    fun disconnectFromGame(session: WebSocketSession) {
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
        sessions.forEach { s -> s.sendStringMessage(msg, logger) }
    }
}
