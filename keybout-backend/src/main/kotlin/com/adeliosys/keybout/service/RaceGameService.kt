package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Constants.RACE_PLAYER_NAME
import com.adeliosys.keybout.util.sendMessage
import com.adeliosys.keybout.util.sendObjectMessage
import com.adeliosys.keybout.util.userName
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import java.time.Instant

/**
 * A running race game.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class RaceGameService(
        private val dictionaryService: DictionaryService,
        private val playService: PlayService,
        scheduler: ThreadPoolTaskScheduler) : BaseGameService(scheduler) {

    /**
     * Remaining words for each user.
     * The key of the map is the player name.
     * The key of the sub-map is the word label.
     */
    private var words: MutableMap<String, MutableMap<String, Word>> = mutableMapOf()

    /**
     * Number of players that typed all their words. Used to detect the end of the round.
     */
    private var finishedPlayers = 0

    override fun getGameType(): String = "race"

    override fun initializeGame(gameDescriptor: GameDescriptor, players: MutableList<WebSocketSession>) {
        super.initializeGame(gameDescriptor, players)

        wordsCount = gameDescriptor.wordsCount
    }

    override fun startCountdown() {
        super.startCountdown()

        val generatedWords = dictionaryService.generateWords(language, wordsCount, minWordsLength, maxWordsLength)

        // Initialize the words list for each user
        for (session in players) {
            val userWords = mutableMapOf<String, Word>()
            generatedWords.forEach { userWords[it] = Word(it) }
            words[session.userName] = userWords
        }

        finishedPlayers = 0
    }

    override fun startPlay() {
        super.startPlay()

        // Notify playing users when the round ends
        scheduler.schedule({ claimRemainingWords(roundId) }, Instant.now().plusSeconds(5L * wordsCount))

        // At the beginning of the round all words of all players have the same status (all are available),
        // so I use the words list of an arbitrary user, the manager
        sendMessage(players, WordsListNotification(getWordsDto(words[manager]!!)))
    }

    @Synchronized
    override fun claimWord(session: WebSocketSession, label: String): Boolean {
        if (!isRoundOver()) {
            val userName = session.userName
            val word = words[userName]?.get(label)
            val userScore = userScores[userName]

            // Ensure that the word is available
            if (word != null && word.userName.isEmpty() && userScore != null) {
                word.userName = userName

                // Add 1 point to the user score
                userScore.incrementPoints()

                // Has the player finished
                if (userScore.points >= wordsCount) {
                    finishedPlayers++
                }

                if (isRoundOver()) {
                    endRound()
                    return isGameOver()
                } else {
                    session.sendObjectMessage(WordsListNotification(getWordsDto(words[userName]!!)))
                }
            }
        }

        return false
    }

    private fun isRoundOver() = finishedPlayers >= players.size

    /**
     * Called when the current round expired.
     * Each available word is given to a fictional player name, so they are displayed in red in the UI.
     */
    @Synchronized
    private fun claimRemainingWords(roundId: Int) {
        if (roundId == this.roundId && !isRoundOver()) {
            finishedPlayers = players.size

            for (tempSession in players) {
                words[tempSession.userName]!!.values.forEach {
                    if (it.userName.isEmpty()) {
                        it.userName = RACE_PLAYER_NAME
                    }
                }
            }

            endRound()

            // Usually the deleteRunningGame method is called from within PlayService
            // as a response to a call to a game service, but claimRemainingWords is executed
            // asynchronously, so this is a special case and we make an explicit call
            // to deleteRunningGame
            if (isGameOver()) {
                playService.deleteRunningGame(id)
            }
        }
    }

    @Synchronized
    override fun removeUser(session: WebSocketSession): Boolean {
        if (super.removeUser(session)) {
            return true
        }

        // If the remaining players caught all their words, end the round
        if (isRoundOver()) {
            endRound()
        }

        return isGameOver()
    }

    /**
     * The round ended, updates the scores and send them to the players.
     */
    private fun endRound() {
        updateScores()

        val roundScoresDto = getRoundScoresDto()
        val gameScoresDto = getGameScoresDto()
        for (tempSession in players) {
            tempSession.sendObjectMessage(ScoresNotification(getWordsDto(words[tempSession.userName]!!), roundScoresDto, gameScoresDto, manager, isGameOver()))
        }
    }
}
