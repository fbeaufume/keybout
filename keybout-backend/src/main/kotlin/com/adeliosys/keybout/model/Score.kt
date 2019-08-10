package com.adeliosys.keybout.model

class Score(val userName: String) {

    // Number of points scored in a round
    var points = 0

    // Number of rounds won
    var victories = 0

    val timestamp = System.currentTimeMillis()

    fun incrementPoints() {
        points++
    }

    fun resetPoints() {
        points = 0
    }

    fun incrementVictories() {
        victories++
    }
}

class ScoreDto(val userName: String, val points: Int)
