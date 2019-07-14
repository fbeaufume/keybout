package com.adeliosys.keybout.model

import org.springframework.web.socket.WebSocketSession

/**
 * A running game.
 */
class Game(
        val id: Long,
        var remainingRounds: Int,
        val words: List<String>,
        var manager: String, // Name of the player that starts the next round
        val players: List<WebSocketSession>)
