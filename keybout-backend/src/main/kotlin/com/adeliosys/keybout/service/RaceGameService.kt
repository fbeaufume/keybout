package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.util.sendMessage
import com.adeliosys.keybout.util.sendObjectMessage
import com.adeliosys.keybout.util.userName
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession

/**
 * A running race game.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class RaceGameService(private val wordGenerator: WordGenerator, scheduler: ThreadPoolTaskScheduler) : BaseGameService(scheduler) {

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

        val generatedWords = wordGenerator.generateWords(language, wordsCount, minWordsLength, maxWordsLength)

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
                    userScore.update(roundStart)
                }

                if (isRoundOver()) {
                    updateScores()

                    val roundScoresDto = getRoundScoresDto()
                    val gameScoresDto = getGameScoresDto()
                    for (tempSession in players) {
                        tempSession.sendObjectMessage(ScoresNotification(getWordsDto(words[tempSession.userName]!!), roundScoresDto, gameScoresDto, manager, isGameOver()))
                    }

                    return isGameOver()
                } else {
                    session.sendObjectMessage(WordsListNotification(getWordsDto(words[userName]!!)))
                }
            }
        }

        return false
    }

    // Should probably rather consider the round as over when there is only one player
    // that hasn't typed all its words, otherwise such AFK player will prevent the round
    // from being over
    private fun isRoundOver() = finishedPlayers >= players.size

    /**
     * Update round and game scores.
     */
    private fun updateScores() {
        // Get the sorted round scores
        roundScores = userScores.values.sortedWith(compareBy { it.duration })

        // Give 1 victory to the round winner
        roundScores[0].incrementVictories()

        // Get the sorted game scores
        gameScores = userScores.values.sortedWith(compareBy({ -it.victories }, { it.bestDuration }))
    }
}
