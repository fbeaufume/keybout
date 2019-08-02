package com.adeliosys.keybout.model

import org.springframework.web.socket.WebSocketSession
import kotlin.math.round

/**
 * A running game.
 */
class Game(
        val id: Long,
        var remainingRounds: Int,
        val words: Map<String, Word>, // Words by label
        var manager: String, // Name of the player that starts the next round
        val players: List<WebSocketSession>) {

    // Number of available words, when it reaches 0 the round ends
    var availableWords = words.size

    // Scores by user name, updated as the words are assigned
    val userScores: MutableMap<String, Score> = mutableMapOf()

    // Ordered round scores, updated at the end of the round
    var roundScores: List<Score> = emptyList()

    // Ordered game scores, updated at the end of the game
    var gameScores: List<Score> = emptyList()

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

                updateUserScore(userName)

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
     * Add 1 point to the user score.
     */
    private fun updateUserScore(userName: String) {
        val score = userScores[userName]
        if (score == null) {
            userScores[userName] = Score(userName)
        } else {
            score.increment()
        }
    }

    /**
     * Update round and game scores.
     */
    private fun updateScores() {
        roundScores = userScores.values.sortedWith(compareBy({ -it.points }, { it.timestamp }))

        // TODO FBE update game scores
    }

    /**
     * Return UI friendly scores.
     */
    fun getRoundScoresDto() = roundScores.map { ScoreDto(it.userName, it.points) }
}
