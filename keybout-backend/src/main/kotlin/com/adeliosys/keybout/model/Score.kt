package com.adeliosys.keybout.model

import com.adeliosys.keybout.model.Constants.WORDS_PER_MIN_BONUS

class Score(val userName: String) {

    // Number of points scored in a round
    var points = 0

    // Timestamp of the latest word won
    var latestWordTimestamp = getTimestamp()

    // Number of words/min for the round
    var wordsPerMin = 0.0f

    // Number of rounds won
    var victories = 0

    // Timestamp of the latest victory
    var latestVictoryTimestamp = getTimestamp()

    // Best number of words/min so far
    var bestWordsPerMin = 0.0f

    fun incrementPoints() {
        points++
        latestWordTimestamp = getTimestamp()
    }

    fun resetPoints() {
        points = 0
    }

    fun incrementVictories() {
        victories++
        latestVictoryTimestamp = getTimestamp()
    }

    /**
     * Update words/min and best words/min.
     */
    fun updateWordsPerMin(roundStart: Long) {
        wordsPerMin = if (points > 0) 60000.0f * points / (latestWordTimestamp - roundStart) else 0.0f
        updateBestWordsPerMin()
    }

    private fun updateBestWordsPerMin() {
        if (bestWordsPerMin <= 0 || bestWordsPerMin < wordsPerMin) {
            bestWordsPerMin = wordsPerMin
        }
    }

    private fun getTimestamp() = System.currentTimeMillis()
}

class ScoreDto(val userName: String, val points: Int, val wpm: Float)
