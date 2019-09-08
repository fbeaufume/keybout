package com.adeliosys.keybout.model

class Score(val userName: String) {

    // Number of points scored in a round
    var points = 0

    // Timestamp of the latest word won
    var latestWordTimestamp = getTimestamp()

    // Number of rounds won
    var victories = 0

    // Timestamp of the latest victory
    var latestVictoryTimestamp = getTimestamp()

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

    private fun getTimestamp() = System.currentTimeMillis()
}

class ScoreDto(val userName: String, val points: Int)
