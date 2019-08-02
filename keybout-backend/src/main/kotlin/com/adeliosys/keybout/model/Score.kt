package com.adeliosys.keybout.model

class Score(val userName: String) {

    var points = 1

    val timestamp = System.currentTimeMillis()

    fun increment() {
        points++
    }
}
