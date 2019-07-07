package com.adeliosys.keybout.model

import java.util.concurrent.atomic.AtomicLong

/**
 * Declaration of a game.
 */
class GameDescriptor(val creator:String, val type:String, val rounds:Int, val words:Int, val language:String) {

    companion object {
        val counter: AtomicLong = AtomicLong()
    }

    val id:Long = counter.incrementAndGet()

    val players = mutableListOf<String>()
}
