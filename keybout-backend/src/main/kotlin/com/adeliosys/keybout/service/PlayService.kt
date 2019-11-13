package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession

/**
 * Manage the declared games and users state.
 */
@Service
class PlayService(private val gameBuilder: GameBuilder) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * WebSocket sessions of users in the lobby.
     * The key is the user name.
     * Concurrency is handled by synchronizing on the class instance.
     */
    private val lobbySessions = mutableMapOf<String, WebSocketSession>()

    /**
     * Declared games, i.e. games visible in the lobby.
     * The key is the game ID.
     * Concurrency is handled by synchronizing on the class instance.
     */
    private val declaredGames = mutableMapOf<Long, GameDescriptor>()

    /**
     * Running games.
     * The key is the game ID.
     * Concurrency is handled by synchronizing on the class instance.
     */
    private val runningGames = mutableMapOf<Long, BaseGameService>()

    /**
     * Called after a user identification or after a user quits a game, to go to the lobby.
     */
    fun goToLobby(session: WebSocketSession) {
        synchronized(this) {
            session.setState(ClientState.LOBBY, 0)
            lobbySessions[session.userName] = session
            session.sendObjectMessage(GamesListNotification(declaredGames.values))
        }
    }

    /**
     * A user creates a declared game.
     */
    fun createGame(session: WebSocketSession, gameDescriptor: GameDescriptor) {
        synchronized(this) {
            session.setState(ClientState.CREATED, gameDescriptor.id)
            declaredGames[gameDescriptor.id] = gameDescriptor
            sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
        }
    }

    /**
     * A user deletes a declared game.
     */
    fun deleteGame(session: WebSocketSession) {
        synchronized(this) {
            val gameDescriptor = declaredGames[session.gameId]
            if (gameDescriptor != null) {
                declaredGames.remove(session.gameId)

                // Process the game creator
                session.setState(ClientState.LOBBY, 0)

                // Process the joined players
                gameDescriptor.players.forEach { lobbySessions[it]?.setState(ClientState.LOBBY, 0) }

                sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    /**
     * A user joins a declared game.
     */
    fun joinGame(session: WebSocketSession, gameId: Long) {
        synchronized(this) {
            if (declaredGames[gameId]?.players?.add(session.userName) == true) {
                session.setState(ClientState.JOINED, gameId)
                sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    /**
     * A user leaves a declared game.
     */
    fun leaveGame(session: WebSocketSession) {
        synchronized(this) {
            if (declaredGames[session.gameId]?.players?.remove(session.userName) == true) {
                session.setState(ClientState.LOBBY, 0)
                sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
            }
        }
    }

    /**
     * A user starts a game.
     * This involves multiple steps, such as displaying the game countdown,
     * before the players can actually play the game.
     */
    fun startGame(session: WebSocketSession) {
        synchronized(this) {
            val descriptor = declaredGames[session.gameId]
            if (descriptor != null) {
                declaredGames.remove(session.gameId)

                // Move creator user to the running game
                lobbySessions.remove(session.userName)
                val players = mutableListOf(session)

                // Move joined users to the running game
                players.addAll(descriptor.players.mapNotNull { lobbySessions.remove(it) })

                // Record the running game
                val game = gameBuilder.buildGame(descriptor, players)
                runningGames[game.id] = game

                // Update the state of all players of that game
                game.players.forEach { it.setState(ClientState.PLAYING, game.id) }

                // Notify other users
                sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))

                logger.info("Started game #{} ({} player{}, 'capture' type, {} round{}, {} {} '{}' words), running game count is {}",
                        descriptor.id,
                        players.size,
                        if (players.size > 1) "s" else "",
                        descriptor.rounds,
                        if (descriptor.rounds > 1) "s" else "",
                        descriptor.wordsCount,
                        descriptor.wordsLength,
                        descriptor.language,
                        runningGames.size)
            }
        }
    }

    /**
     * A user typed a complete word.
     */
    fun claimWord(session: WebSocketSession, label: String) {
        val game = runningGames[session.gameId]
        if (game != null) {
            if (game.claimWord(session, label)) {
                deleteRunningGame(game.id)
            }
        }
    }

    private fun deleteRunningGame(gameId: Long) {
        runningGames.remove(gameId)
        logger.info("Ended game #{}, running game count is {}", gameId, runningGames.size)
    }

    /**
     * A game manager starts the next round of a game.
     * It actually triggers the start of the round countdown.
     */
    fun startRound(session: WebSocketSession) {
        runningGames[session.gameId]?.startCountdown()
    }

    /**
     * A user was disconnected for some reason, do the cleanup.
     */
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
            ClientState.PLAYING -> {
                synchronized(this) {
                    val game = runningGames[session.gameId]
                    if (game != null) {
                        val (changed, manager, empty) = game.removeUser(session)

                        if (empty) {
                            // No player left, remove the game
                            deleteRunningGame(game.id)
                        } else if (changed) {
                            // Notify the players about the new manager
                            sendMessage(game.players, ManagerNotification(manager))
                        }
                    }
                }
            }
        }
    }
}
