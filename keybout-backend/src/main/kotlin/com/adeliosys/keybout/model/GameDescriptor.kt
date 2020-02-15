package com.adeliosys.keybout.model

import java.util.concurrent.atomic.AtomicLong

/**
 * A declared game.
 */
class GameDescriptor(
        val creator: String,
        mode: String,
        style: String,
        language: String,
        difficulty: String) {

    companion object {
        val counter = AtomicLong()
    }

    val id: Long = counter.incrementAndGet()

    val players = mutableListOf<String>()

    val mode: GameMode = GameMode.getByCode(mode)

    val rounds = 2

    val style: GameStyle = GameStyle.getByCode(style)

    val language: Language = Language.getByCode(language, this.style)

    val wordsCount = 10

    val difficulty: Difficulty = Difficulty.getByCode(difficulty)
}
