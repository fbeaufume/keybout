package com.adeliosys.keybout.model

import org.springframework.web.socket.WebSocketSession

/**
 * A running game.
 */
class Game(descriptor: GameDescriptor, val players: List<WebSocketSession>) {

    val id: Long = descriptor.id

    /**
     * Name of the player that starts the next round.
     */
    val manager: String = descriptor.creator

    val remainingRounds: Int = descriptor.rounds

    val wordsCount: Int = descriptor.words

    val language: String = descriptor.language

    val words = listOf<String>()
}
