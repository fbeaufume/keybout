package com.adeliosys.keybout.model

import java.util.concurrent.atomic.AtomicLong

/**
 * A declared game.
 */
class GameDescriptor(
        val creator: String,
        val type: String,
        val rounds: Int,
        language: String,
        val wordsCount: Int,
        wordsLength: String,
        wordsEffect: String) {

    companion object {
        val counter: AtomicLong = AtomicLong()
    }

    val id: Long = counter.incrementAndGet()

    val players = mutableListOf<String>()

    val language: Language = Language.getByCode(language)

    val wordsLength: WordLength = WordLength.getByCode(wordsLength)

    val wordsEffect: WordEffect = WordEffect.getByCode(wordsEffect)
}
