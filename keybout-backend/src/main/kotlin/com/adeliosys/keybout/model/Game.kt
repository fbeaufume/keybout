package com.adeliosys.keybout.model

import org.springframework.web.socket.WebSocketSession

/**
 * A running game.
 */
class Game {

    val id:Long

    /**
     * Name of the player that starts the next round.
     */
    val manager:String

    val remainingRounds:Int

    val wordsCount:Int

    val language:String

    val words = listOf<String>()

    /**
     * All game players, including the creator.
     */
    val players: List<WebSocketSession>

    constructor(descriptor: GameDescriptor, players: List<WebSocketSession>) {
        this.id = descriptor.id
        this.manager = descriptor.creator
        this.remainingRounds = descriptor.rounds
        this.wordsCount = descriptor.words
        this.language = descriptor.language
        this.players = players
    }
}
