package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.util.sendMessage
import com.adeliosys.keybout.util.userName
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.socket.WebSocketSession
import java.time.Instant

/**
 * Base class for the various game types.
 */
abstract class BaseGameService(private val scheduler: ThreadPoolTaskScheduler) {

    var id: Long = 0

    private var roundsCount: Int = 0

    var language: String = ""

    var wordsCount: Int = 0

    var minWordsLength = 5

    var maxWordsLength = 10

    /**
     * Name of the player that starts the next round.
     */
    var manager: String = ""

    /**
     * All game players, including the manager.
     */
    val players: MutableList<WebSocketSession> = mutableListOf()

    /**
     * Round and game scores by user name, updated as the words are assigned.
     */
    protected val userScores: MutableMap<String, Score> = mutableMapOf()

    /**
     * Ordered round scores, updated at the end of the round.
     */
    protected var roundScores: List<Score> = emptyList()

    /**
     * Ordered game scores, updated at the end of the game.
     */
    protected var gameScores: List<Score> = emptyList()

    /**
     * Timestamp of the beginning of the current round,
     * used to compute the words/min.
     */
    protected var roundStart: Long = 0

    /**
     * Return the game type such as 'capture" or 'race'.
     * Used by the UI.
     */
    abstract fun getGameType(): String

    /**
     * One-time initialization. Should be in a constructor or Kotlin init block,
     * but would not be Spring friendly since this class is a Spring service.
     */
    open fun initializeGame(gameDescriptor: GameDescriptor, players: MutableList<WebSocketSession>) {
        id = gameDescriptor.id

        roundsCount = gameDescriptor.rounds

        language = gameDescriptor.language

        val pair = when (gameDescriptor.wordsLength) {
            Constants.LENGTH_SHORTEST -> Pair(3, 6)
            Constants.LENGTH_SHORTER -> Pair(4, 8)
            Constants.LENGTH_LONGER -> Pair(6, 12)
            Constants.LENGTH_LONGEST -> Pair(7, 14)
            else -> Pair(5, 10)
        }
        minWordsLength = pair.first
        maxWordsLength = pair.second

        manager = gameDescriptor.creator

        this.players.addAll(players)

        players.forEach { userScores[it.userName] = Score(it.userName) }
    }

    /**
     * Start the countdown for the next round.
     */
    open fun startCountdown() {
        // Reset the players scores
        userScores.forEach { (_, s) -> s.resetPoints() }

        // Notify players to display the countdown
        sendMessage(players, GameStartNotification(getGameType()))

        // Notify playing users when the round begins
        scheduler.schedule({ startPlay() }, Instant.now().plusSeconds(5L))
    }

    /**
     * Actually start the round.
     */
    open fun startPlay() {
        roundStart = System.currentTimeMillis()
    }

    /**
     * Utility method that returns a UI friendly map of words,
     * i.e. the key is the word label and the value is the assigned user name (or empty).
     */
    fun getWordsDto(words: Map<String, Word>): Map<String, String> = words.map { it.key to it.value.userName }.toMap()

    /**
     * Update the game state after a player completely typed a word.
     * @return true if the game is over
     */
    abstract fun claimWord(session: WebSocketSession, label: String): Boolean

    fun isGameOver() = gameScores[0].victories >= roundsCount

    /**
     * Update round and game scores.
     */
    fun updateScores() {
        // Update the words/min and best words/min
        userScores.values.forEach { it.update(roundStart) }

        // Get the sorted round scores
        roundScores = userScores.values.sortedWith(compareBy({ -it.points }, { -it.wordsPerMin }))

        // Give 1 victory to the round winner
        roundScores[0].incrementVictories()

        // Get the sorted game scores
        gameScores = userScores.values.sortedWith(compareBy({ -it.victories }, { -it.bestWordsPerMin }, { it.latestVictoryTimestamp }))
    }

    /**
     * Return UI friendly round scores.
     */
    fun getRoundScoresDto() = roundScores.map { ScoreDto(it.userName, it.points, it.wordsPerMin) }

    /**
     * Return UI friendly game scores.
     */
    fun getGameScoresDto() = gameScores.map { ScoreDto(it.userName, it.victories, it.bestWordsPerMin) }

    /**
     * A user disconnected, remove him from the game.
     * Return true if the manager changed (when it was the disconnected user),
     * the new manager name, the number of remaining users.
     */
    @Synchronized
    fun removeUser(session: WebSocketSession): Triple<Boolean, String, Boolean> {
        var changed = false
        if (players.remove(session)) {
            if (players.size > 0 && session.userName == manager) {
                // Choose a new manager
                manager = players[0].userName
                changed = true
            }
        }
        return Triple(changed, manager, players.size <= 0)
    }
}
