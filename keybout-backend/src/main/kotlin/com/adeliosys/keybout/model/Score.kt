package com.adeliosys.keybout.model

/**
 * A player score during a game.
 * Some attributes are for the current round, while others are for the whole game.
 */
class Score(val userName: String) {

    // Number of words caught in a round
    var points = 0

    // Timestamp of the latest word won, used in capture games only
    private var latestWordTimestamp = getTimestamp()

    // Number of words/min for the round, used in capture games only
    var wordsPerMin = 0.0f

    // Duration in mec taken to catch all words, used in race games only
    var duration = 0

    // Number of rounds won
    var victories = 0

    // Timestamp of the latest victory, used in capture games only
    var latestVictoryTimestamp = getTimestamp()

    // Best number of words/min so far, used in capture games only
    var bestWordsPerMin = 0.0f

    // Best duration in mec so far, used in race games only
    var bestDuration = 0

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
     * Update words/min and durations once all words are caught.
     */
    fun update(roundStart: Long) {
        wordsPerMin = if (points > 0) 60000.0f * points / (latestWordTimestamp - roundStart) else 0.0f

        if (bestWordsPerMin <= 0 || bestWordsPerMin < wordsPerMin) {
            bestWordsPerMin = wordsPerMin
        }

        duration = (latestWordTimestamp - roundStart).toInt()

        if (bestDuration == 0 || bestDuration > duration) {
            bestDuration = duration
        }
    }

    private fun getTimestamp() = System.currentTimeMillis()
}

class ScoreDto(val userName: String, val points: Int, val duration: Int, val wpm: Float)
