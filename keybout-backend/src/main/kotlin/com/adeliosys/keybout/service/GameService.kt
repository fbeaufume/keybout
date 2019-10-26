package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.GameDescriptor
import com.adeliosys.keybout.model.Score
import com.adeliosys.keybout.model.ScoreDto
import com.adeliosys.keybout.model.Word
import com.adeliosys.keybout.util.userName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession

/**
 * A running game.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class GameService {

    var id: Long = 0

    private var roundsCount: Int = 0

    var wordCount: Int = 0

    var language: String = ""

    /**
     * Name of the player that starts the next round.
     */

    var manager: String = ""

    val players: MutableList<WebSocketSession> = mutableListOf()

    /**
     * Timestamp of the beginning of the current round.
     */

    private var roundStart: Long = 0

    /**
     * Duration of the current round.
     */
    var roundDuration: Long = 0

    /**
     * Words by label.
     */
    private var words: Map<String, Word> = mapOf()

    /**
     * Number of available words, when it reaches 0 the round ends.
     */
    private var availableWords = 0

    /**
     * Round and game scores by user name, updated as the words are assigned.
     */
    private val userScores: MutableMap<String, Score> = mutableMapOf()

    /**
     * Ordered round scores, updated at the end of the round.
     */
    private var roundScores: List<Score> = emptyList()

    /**
     * Ordered game scores, updated at the end of the game.
     */
    private var gameScores: List<Score> = emptyList()

    @Autowired
    private lateinit var wordGenerator: WordGenerator

    /**
     * One-time initialization. Should be in a constructor or Kotlin init block,
     * but would not be Spring friendly since this class is a Spring service.
     */
    fun initialize(gameDescriptor: GameDescriptor, players: MutableList<WebSocketSession>) {
        id = gameDescriptor.id
        roundsCount = gameDescriptor.rounds
        wordCount = gameDescriptor.words * players.size
        language = gameDescriptor.language
        this.players.addAll(players)

        players.forEach { userScores[it.userName] = Score(it.userName) }
    }

    /**
     * Initialization at the beginning of each round.
     */
    fun initializeRound() {
        roundStart = System.currentTimeMillis()
        roundDuration = 0

        words = wordGenerator.generateWords(language, wordCount)
        availableWords = words.size

        // Reset the user scores
        userScores.forEach { (_, s) -> s.resetPoints() }
    }

    /**
     * Return UI friendly words, i.e. the key is the word label
     * and the value is the assigned user name (or empty).
     */
    fun getWordsDto(): Map<String, String> =
            words.map { entry -> entry.key to entry.value.userName }.toMap()

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
        roundScores = userScores.values.sortedWith(compareBy({ -it.points }, { it.latestWordTimestamp }))

        // Give 1 victory to the best user
        roundScores[0].incrementVictories()

        gameScores = userScores.values.sortedWith(compareBy({ -it.victories }, { it.latestVictoryTimestamp }))
    }

    /**
     * Return UI friendly round scores.
     */
    fun getRoundScoresDto() = roundScores.map { ScoreDto(it.userName, it.points) }

    /**
     * Return UI friendly game scores.
     */
    fun getGameScoresDto() = gameScores.map { ScoreDto(it.userName, it.victories) }

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
