package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Constants.EXECUTOR
import com.adeliosys.keybout.util.sendMessage
import com.adeliosys.keybout.util.userName
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.TimeUnit

/**
 * A running capture game.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class CaptureGameService(private val wordGenerator: WordGenerator) : BaseGameService() {


    /**
     * Timestamp of the beginning of the current round,
     * used to compute the words/min.
     */
    private var roundStart: Long = 0

    /**
     * Duration of the current round,
     * used to compute the words/min.
     */
    var roundDuration: Long = 0

    /**
     * Shared words by label. Used to keep track of who captured what.
     */
    private var words: MutableMap<String, Word> = mutableMapOf()

    /**
     * Number of available words. Used to detect the end of the round (when it reaches 0).
     */
    private var availableWords = 0

    override fun initializeGame(gameDescriptor: GameDescriptor, players: MutableList<WebSocketSession>) {
        super.initializeGame(gameDescriptor, players)

        wordsCount = gameDescriptor.wordsCount * players.size
    }

    override fun startRound() {
        super.startRound()

        roundStart = System.currentTimeMillis()
        roundDuration = 0

        words.clear()
        wordGenerator.generateWords(language, wordsCount, minWordsLength, maxWordsLength).forEach {
            words[it] = Word(it)
        }

        availableWords = words.size

        // Notify playing users when the round begins
        EXECUTOR.schedule({ sendMessage(players, WordsListNotification(getWordsDto())) }, 5L, TimeUnit.SECONDS)
    }

    /**
     * Return UI friendly words, i.e. the key is the word label
     * and the value is the assigned user name (or empty).
     */
    private fun getWordsDto(): Map<String, String> =
            words.map { it.key to it.value.userName }.toMap()

    /**
     * Assign a word to a user, if currently available.
     * Return the map of label and user names if the assignment succeeded, an empty map otherwise.
     */
    @Synchronized
    fun claimWord(userName: String, label: String): Map<String, String> {
        if (availableWords > 0) {
            val word = words[label]
            if (word != null && word.userName.isEmpty()) {
                word.userName = userName

                // Add 1 point to the user score
                userScores[userName]?.incrementPoints()

                availableWords--
                if (isRoundOver()) {
                    roundDuration = System.currentTimeMillis() - roundStart

                    updateScores()
                }

                return getWordsDto()
            }
        }
        return mapOf()
    }

    fun isRoundOver() = availableWords <= 0

    fun isGameOver() = gameScores[0].victories >= roundsCount

    /**
     * Update round and game scores.
     */
    private fun updateScores() {
        // Update the words/min and best words/min
        userScores.values.forEach { it.updateWordsPerMin(roundDuration) }

        // Get the sorted round scores
        roundScores = userScores.values.sortedWith(compareBy({ -it.points }, { it.latestWordTimestamp }))

        // If the round winner has won the same number of words than the second player,
        // give him a small words/min bonus to prevent a frustrating tie in the UI
        if (roundScores.size > 1 && roundScores[0].points == roundScores[1].points) {
            roundScores[0].giveWordsPerMinBonus()
        }

        // Give 1 victory to the round winner
        roundScores[0].incrementVictories()

        // Get the sorted game scores
        gameScores = userScores.values.sortedWith(compareBy({ -it.victories }, { -it.bestWordsPerMin }, { it.latestVictoryTimestamp }))
    }

    /**
     * Return UI friendly round scores.
     */
    fun getRoundScoresDto() = roundScores.map { ScoreDto(it.userName, it.points, it.wordsPerMin) }

    /**
     * Return UI friendly game scores.
     */
    fun getGameScoresDto() = gameScores.map { ScoreDto(it.userName, it.victories, it.bestWordsPerMin) }

    /**
     * A user disconnected, remove him from the game.
     * Return true if the manager changed (when it was the disconnected user),
     * the new manager name, the number of remaining users.
     */
    @Synchronized
    fun removeUser(session: WebSocketSession): Triple<Boolean, String, Boolean> {
        var changed = false
        if (players.remove(session)) {
            if (players.size > 0 && session.userName == manager) {
                // Choose a new manager
                manager = players[0].userName
                changed = true
            }
        }
        return Triple(changed, manager, players.size <= 0)
    }
}
