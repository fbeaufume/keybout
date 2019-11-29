package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.util.sendMessage
import com.adeliosys.keybout.util.userName
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession

/**
 * A running capture game.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class CaptureGameService(private val dictionaryService: DictionaryService, scheduler: ThreadPoolTaskScheduler) : BaseGameService(scheduler) {

    /**
     * Shared words by label. Used to keep track of who captured what.
     */
    private var words: MutableMap<String, Word> = mutableMapOf()

    /**
     * Number of available words. Used to detect the end of the round (when it reaches 0).
     */
    private var availableWords = 0

    override fun getGameType(): String = "capture"

    override fun initializeGame(gameDescriptor: GameDescriptor, players: MutableList<WebSocketSession>) {
        super.initializeGame(gameDescriptor, players)

        wordsCount = gameDescriptor.wordsCount * players.size
    }

    override fun startCountdown() {
        super.startCountdown()

        // Initialize the shared list of words
        words.clear()
        dictionaryService.generateWords(language, wordsCount, minWordsLength, maxWordsLength).forEach {
            words[it] = Word(it)
        }

        availableWords = words.size
    }

    override fun startPlay() {
        super.startPlay()

        sendMessage(players, WordsListNotification(getWordsDto(words)))
    }

    @Synchronized
    override fun claimWord(session: WebSocketSession, label: String): Boolean {
        if (!isRoundOver()) {
            val userName = session.userName
            val word = words[label]

            // Ensure that the word is available
            if (word != null && word.userName.isEmpty()) {
                word.userName = userName

                // Add 1 point to the user score
                userScores[userName]?.incrementPoints()

                availableWords--

                if (isRoundOver()) {
                    updateScores()

                    sendMessage(players, ScoresNotification(getWordsDto(words), getRoundScoresDto(), getGameScoresDto(), manager, isGameOver()))

                    return isGameOver()
                } else {
                    sendMessage(players, WordsListNotification(getWordsDto(words)))
                }
            }
        }

        return false
    }

    private fun isRoundOver() = availableWords <= 0
}
