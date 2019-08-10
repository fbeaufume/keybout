package com.adeliosys.keybout.model

import com.adeliosys.keybout.controller.getUserName
import org.springframework.web.socket.WebSocketSession
import kotlin.math.round

/**
 * A running game.
 */
class Game(
        val id: Long,
        var remainingRounds: Int,
        val wordCount: Int,
        val language: String,
        var manager: String, // Name of the player that starts the next round
        val players: List<WebSocketSession>) {

    // Words by label
    var words: Map<String, Word> = mapOf()

    // Number of available words, when it reaches 0 the round ends
    var availableWords = 0

    // Round scores by user name, updated as the words are assigned
    val userScores: MutableMap<String, Score> = mutableMapOf()

    // Ordered round scores, updated at the end of the round
    var roundScores: List<Score> = emptyList()

    // Ordered game scores, updated at the end of the game
    var gameScores: List<Score> = emptyList()

    init {
        players.forEach { userScores[it.getUserName()] = Score(it.getUserName()) }
    }

    @Synchronized
    fun initializeRound(words: Map<String, Word>) {
        this.words = words
        availableWords = words.size

        // Reset the user scores
        userScores.forEach { (_, s) -> s.resetPoints() }
    }

    /**
     * Return UI friendly words, i.e. the key is the word label
     * and the value is the assigned user name (or empty).
     */
    fun getWordsDto(): Map<String, String> =
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
                    updateScores()
                }

                return getWordsDto()
            }
        }
        return mapOf()
    }

    fun isRoundOver() = availableWords <= 0

    /**
     * Update round and game scores.
     */
    private fun updateScores() {
        roundScores = userScores.values.sortedWith(compareBy({ -it.points }, { it.timestamp }))

        // Give 1 victory to the best user
        roundScores[0]?.incrementVictories()

        gameScores = userScores.values.sortedWith(compareBy({-it.victories}))
    }

    /**
     * Return UI friendly round scores.
     */
    fun getRoundScoresDto() = roundScores.map { ScoreDto(it.userName, it.points) }

    /**
     * Return UI friendly game scores.
     */
    fun getGameScoresDto() = gameScores.map { ScoreDto(it.userName, it.victories) }
}
