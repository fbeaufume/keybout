package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession

/**
 * Manage the declared games and users state.
 */
@Service
class PlayService(private val applicationContext: ApplicationContext) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * WebSocket sessions of users in the lobby.
     * The key is the user name.
     * Concurrency is handled by synchronizing on the service class instance.
     */
    private val lobbySessions = mutableMapOf<String, WebSocketSession>()

    /**
     * Declared games, i.e. games visible in the lobby.
     * The key is the game ID.
     * Concurrency is handled by synchronizing on the service class instance.
     */
    private val declaredGames = mutableMapOf<Long, GameDescriptor>()

    /**
     * Concurrency is handled by synchronizing on the service class instance.
     */
    val declaredGamesCounter = Counter()

    /**
     * Running games.
     * The key is the game ID.
     * Concurrency is handled by synchronizing on the service class instance.
     */
    private val runningGames = mutableMapOf<Long, BaseGameService>()

    /**
     * Concurrency is handled by the counter.
     */
    val runningGamesCounter = Counter()

    /**
     * Called after a user identification or after a user quits a game, to go to the lobby.
     */
    @Synchronized
    fun goToLobby(session: WebSocketSession) {
            session.setState(ClientState.LOBBY, 0)
            lobbySessions[session.userName] = session
            session.sendObjectMessage(GamesListNotification(declaredGames.values))
    }

    /**
     * A user creates a declared game.
     */
    @Synchronized
    fun createGame(session: WebSocketSession, gameDescriptor: GameDescriptor) {
        session.setState(ClientState.CREATED, gameDescriptor.id)
        declaredGames[gameDescriptor.id] = gameDescriptor
        declaredGamesCounter.increment()

        sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
    }

    /**
     * A user deletes a declared game.
     */
    @Synchronized
    fun deleteGame(session: WebSocketSession) {
        val gameDescriptor = declaredGames[session.gameId]
        if (gameDescriptor != null) {
            declaredGames.remove(session.gameId)
            declaredGamesCounter.decrement()

            // Process the game creator
            session.setState(ClientState.LOBBY, 0)

            // Process the joined players
            gameDescriptor.players.forEach { lobbySessions[it]?.setState(ClientState.LOBBY, 0) }

            sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
        }
    }

    /**
     * A user joins a declared game.
     */
    @Synchronized
    fun joinGame(session: WebSocketSession, gameId: Long) {
        if (declaredGames[gameId]?.players?.add(session.userName) == true) {
            session.setState(ClientState.JOINED, gameId)
            sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
        }
    }

    /**
     * A user leaves a declared game.
     */
    @Synchronized
    fun leaveGame(session: WebSocketSession) {
        if (declaredGames[session.gameId]?.players?.remove(session.userName) == true) {
            session.setState(ClientState.LOBBY, 0)
            sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))
        }
    }

    /**
     * A user starts a game.
     * This involves multiple steps, such as displaying the game countdown,
     * before the players can actually play the game.
     */
    @Synchronized
    fun startGame(session: WebSocketSession) {
        val descriptor = declaredGames[session.gameId]
        if (descriptor != null) {
            declaredGames.remove(session.gameId)
            declaredGamesCounter.decrement()

            // Move creator user to the running game
            lobbySessions.remove(session.userName)
            val players = mutableListOf(session)

            // Move joined users to the running game
            players.addAll(descriptor.players.mapNotNull { lobbySessions.remove(it) })

            // Build the game instance
            val game = applicationContext.getBean(descriptor.mode.type).apply {
                initializeGame(descriptor, players)
                startCountdown()
            }

            // Record the running game
            runningGames[game.id] = game
            runningGamesCounter.increment()

            // Update the state of all players of that game
            game.players.forEach { it.setState(ClientState.PLAYING, game.id) }

            // Notify other users
            sendMessage(lobbySessions.values, GamesListNotification(declaredGames.values))

            logger.info("Started game {} ({} player{}, '{}' mode, '{}' style, '{}' language, '{}' difficulty), running game count is {}",
                    descriptor.id,
                    players.size,
                    if (players.size > 1) "s" else "",
                    descriptor.mode,
                    descriptor.style,
                    descriptor.language,
                    descriptor.difficulty,
                    runningGames.size)
        }
    }

    /**
     * A user typed a complete word.
     */
    fun claimWord(session: WebSocketSession, value: String) {
        val game = runningGames[session.gameId]
        if (game != null) {
            if (game.claimWord(session, value)) {
                endGame(game.id)
            }
        }
    }

    /**
     * Delete a running game.
     */
    fun endGame(gameId: Long) {
        runningGames.remove(gameId)
        runningGamesCounter.decrement()
        logger.info("Ended game {}, running game count is {}", gameId, runningGames.size)
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
                    runningGames[session.gameId]?.let {
                        if (it.removeUser(session)) {
                            // No player left or race game ended, remove the game
                            endGame(it.id)
                        }
                    }
                }
            }
        }
    }
}
