package com.adeliosys.keybout.model

class Score(val userName: String) {

    var points = 0

    val timestamp = System.currentTimeMillis()

    fun increment() {
        points++
    }
}

class ScoreDto(val userName: String, val points: Int)
