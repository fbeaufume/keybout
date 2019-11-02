package com.adeliosys.keybout.model

import java.util.concurrent.atomic.AtomicLong

/**
 * A declared game.
 */
class GameDescriptor(val creator: String, val type: String, val rounds: Int, val language: String, val wordCount: Int, val wordLength: String) {

    companion object {
        val counter: AtomicLong = AtomicLong()
    }

    val id: Long = counter.incrementAndGet()

    val players = mutableListOf<String>()
}
