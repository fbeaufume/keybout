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
abstract class BaseGameService(
        protected val awardService: AwardService,
        protected val scheduler: ThreadPoolTaskScheduler) {

    var id: Long = 0

    private var roundsCount: Int = 0

    var language: String = ""

    var wordsCount: Int = 0

    var wordsLength: WordLength = WordLength.STANDARD

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
    private var roundScores: List<Score> = emptyList()

    /**
     * Ordered game scores, updated at the end of the game.
     */
    private var gameScores: List<Score> = emptyList()

    /**
     * ID of the current round.
     */
    protected var roundId = 0

    /**
     * Timestamp of the beginning of the current round,
     * used to compute the words/min.
     */
    private var roundStart = 0L

    /**
     * Used by the UI.
     * @return the game type such as 'capture" or 'race'.
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

        wordsLength = gameDescriptor.wordsLength

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
        roundId++
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

    fun isGameOver() = gameScores.isNotEmpty() && gameScores[0].victories >= roundsCount

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
     * @return UI friendly round scores.
     */
    fun getRoundScoresDto() = roundScores.map { ScoreDto(it.userName, it.points, it.wordsPerMin, it.awards) }

    /**
     * @return UI friendly game scores.
     */
    fun getGameScoresDto() = gameScores.map { ScoreDto(it.userName, it.victories, it.bestWordsPerMin, null) }

    /**
     * A user disconnected, remove him from the game.
     *
     * @return true if the game has ended
     */
    @Synchronized
    open fun removeUser(session: WebSocketSession): Boolean {
        if (players.remove(session)) {
            if (players.size > 0 && session.userName == manager) {
                // Choose a new manager
                manager = players[0].userName
                sendMessage(players, ManagerNotification(manager))
            }

            // The game has ended because there is no player left
            return players.size <= 0
        }
        return false
    }
}
