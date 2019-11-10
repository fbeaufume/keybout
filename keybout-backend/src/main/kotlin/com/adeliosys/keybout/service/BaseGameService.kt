package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.Constants
import com.adeliosys.keybout.model.GameDescriptor
import com.adeliosys.keybout.model.GameStartNotification
import com.adeliosys.keybout.model.Score
import com.adeliosys.keybout.util.sendMessage
import com.adeliosys.keybout.util.userName
import org.springframework.web.socket.WebSocketSession

/**
 * Base class for the various game types.
 */
open class BaseGameService {

    var id: Long = 0

    protected var roundsCount: Int = 0

    var language: String = ""

    var wordsCount: Int = 0

    var minWordsLength = 5

    var maxWordsLength = 10

    /**
     * Name of the player that starts the next round.
     */
    var manager: String = ""

    /**
     * All game players, including the manager.
     */
    val players: MutableList<WebSocketSession> = mutableListOf()

    /**
     * Round and game scores by user name, updated as the words are assigned.
     */
    protected val userScores: MutableMap<String, Score> = mutableMapOf()

    /**
     * Ordered round scores, updated at the end of the round.
     */
    protected var roundScores: List<Score> = emptyList()

    /**
     * Ordered game scores, updated at the end of the game.
     */
    protected var gameScores: List<Score> = emptyList()

    /**
     * One-time initialization. Should be in a constructor or Kotlin init block,
     * but would not be Spring friendly since this class is a Spring service.
     */
    open fun initializeGame(gameDescriptor: GameDescriptor, players: MutableList<WebSocketSession>) {
        id = gameDescriptor.id

        roundsCount = gameDescriptor.rounds

        language = gameDescriptor.language

        val pair = when (gameDescriptor.wordsLength) {
            Constants.LENGTH_SHORTEST -> Pair(3, 6)
            Constants.LENGTH_SHORTER -> Pair(4, 8)
            Constants.LENGTH_LONGER -> Pair(6, 12)
            Constants.LENGTH_LONGEST -> Pair(7, 14)
            else -> Pair(5, 10)
        }
        minWordsLength = pair.first
        maxWordsLength = pair.second

        manager = gameDescriptor.creator

        this.players.addAll(players)

        players.forEach { userScores[it.userName] = Score(it.userName) }
    }

    /**
     * Start the next round.
     */
    open fun startRound() {
        // Reset the players scores
        userScores.forEach { (_, s) -> s.resetPoints() }

        // Notify players to display the countdown
        sendMessage(players, GameStartNotification())
    }
}
