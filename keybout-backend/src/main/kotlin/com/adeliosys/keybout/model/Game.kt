package com.adeliosys.keybout.model

import org.springframework.web.socket.WebSocketSession

/**
 * A running game.
 */
class Game(
        val id: Long,
        var remainingRounds: Int,
        val words: Map<String, Word>,
        var manager: String, // Name of the player that starts the next round
        val players: List<WebSocketSession>) {

    /**
     * Assign a word to a user, if currently available.
     * Return the map of label and user names if the assignment succeeded, an empty map otherwise.
     */
    @Synchronized
    fun claimWord(userName: String, label: String): Map<String, String> {
        val word = words[label]
        if (word != null && word.userName.isEmpty()) {
            word.userName = userName

            return getFlatWordsMap()
        }
        return mapOf()
    }

    fun getFlatWordsMap() : Map<String, String>  {
        val map = mutableMapOf<String, String>()
        words.forEach { (label, word) -> map[label] = word.userName }
        return map
    }
}
