package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Constants.EXECUTOR
import com.adeliosys.keybout.util.sendMessage
import com.adeliosys.keybout.util.userName
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.TimeUnit

/**
 * A running game.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class GameService(private val wordGenerator: WordGenerator) {

    var id: Long = 0

    private var roundsCount: Int = 0

    var language: String = ""

    var wordCount: Int = 0

    var minWordLength = 5

    var maxWordLength = 10

    /**
     * Name of the player that starts the next round.
     */
    var manager: String = ""

    /**
     * All game players, including the manager.
     */
    val players: MutableList<WebSocketSession> = mutableListOf()

    /**
     * Timestamp of the beginning of the current round,
     * used to compute the words/min.
     */
    private var roundStart: Long = 0

    /**
     * Duration of the current round,
     * used to compute the words/min.
     */
    var roundDuration: Long = 0

    /**
     * Shared words by label. Used to keep track of who captured what.
     */
    private var words: MutableMap<String, Word> = mutableMapOf()

    /**
     * Number of available words. Used to detect the end of the round (when it reaches 0).
     */
    private var availableWords = 0

    /**
     * Round and game scores by user name, updated as the words are assigned.
     */
    private val userScores: MutableMap<String, Score> = mutableMapOf()

    /**
     * Ordered round scores, updated at the end of the round.
     */
    private var roundScores: List<Score> = emptyList()

    /**
     * Ordered game scores, updated at the end of the game.
     */
    private var gameScores: List<Score> = emptyList()

    /**
     * One-time initialization. Should be in a constructor or Kotlin init block,
     * but would not be Spring friendly since this class is a Spring service.
     */
    fun initializeGame(gameDescriptor: GameDescriptor, players: MutableList<WebSocketSession>) {
        id = gameDescriptor.id
        roundsCount = gameDescriptor.rounds
        language = gameDescriptor.language
        wordCount = gameDescriptor.wordCount * players.size
        val pair = when (gameDescriptor.wordLength) {
            Constants.LENGTH_SHORTEST -> Pair(3, 6)
            Constants.LENGTH_SHORTER -> Pair(4, 8)
            Constants.LENGTH_LONGER -> Pair(6, 12)
            Constants.LENGTH_LONGEST -> Pair(7, 14)
            else -> Pair(5, 10)
        }
        minWordLength = pair.first
        maxWordLength = pair.second
        manager = gameDescriptor.creator
        this.players.addAll(players)

        players.forEach { userScores[it.userName] = Score(it.userName) }
    }

    /**
     * Start the next round.
     */
    fun startRound() {
        roundStart = System.currentTimeMillis()
        roundDuration = 0

        words.clear()
        wordGenerator.generateWords(language, wordCount, minWordLength, maxWordLength).forEach {
            words[it] = Word(it)
        }

        availableWords = words.size

        // Reset the players scores
        userScores.forEach { (_, s) -> s.resetPoints() }

        // Notify players to display the countdown
        sendMessage(players, GameStartNotification())

        // Notify playing users when the round begins
        EXECUTOR.schedule({ sendMessage(players, WordsListNotification(getWordsDto())) }, 5L, TimeUnit.SECONDS)
    }

    /**
     * Return UI friendly words, i.e. the key is the word label
     * and the value is the assigned user name (or empty).
     */
    private fun getWordsDto(): Map<String, String> =
            words.map { entry -> entry.key to entry.value.userName }.toMap()

    /**
     * Assign a word to a user, if currently available.
     * Return the map of label and user names if the assignment succeeded, an empty map otherwise.
     */
    @Synchronized
    fun claimWord(userName: String, label: String): Map<String, String> {
        if (availableWords > 0) {
            val word = words[label]
            if (word != null && word.userName.isEmpty()) {
                word.userName = userName

                // Add 1 point to the user score
                userScores[userName]?.incrementPoints()

                availableWords--
                if (isRoundOver()) {
                    roundDuration = System.currentTimeMillis() - roundStart

                    updateScores()
                }

                return getWordsDto()
            }
        }
        return mapOf()
    }

    fun isRoundOver() = availableWords <= 0

    fun isGameOver() = gameScores[0].victories >= roundsCount

    /**
     * Update round and game scores.
     */
    private fun updateScores() {
        // Update the words/min and best words/min
        userScores.values.forEach { it.updateWordsPerMin(roundDuration) }

        // Get the sorted round scores
        roundScores = userScores.values.sortedWith(compareBy({ -it.points }, { it.latestWordTimestamp }))

        // If the round winner has won the same number of words than the second player,
        // give him a small words/min bonus to prevent a frustrating tie in the UI
        if (roundScores.size > 1 && roundScores[0].points == roundScores[1].points) {
            roundScores[0].giveWordsPerMinBonus()
        }

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
