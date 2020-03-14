package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Constants.SCORES_LENGTH
import org.springframework.stereotype.Service

typealias Type = Triple<GameStyle, Language, Difficulty>

/**
 * Keep the best scores in memory.
 */
@Service
class ScoreService(val size: Int = SCORES_LENGTH) {

    private val topScoresByType: MutableMap<Type, MutableList<TopScore>> = mutableMapOf()

    @Synchronized
    fun getTopScores(style: GameStyle, language: Language): List<TopScoresDto> =
            MutableList(Difficulty.values().size) {
                TopScoresDto(style, language, Difficulty.values()[it], getTopScoresInternal(style, language, Difficulty.values()[it]))
            }

    @Synchronized
    fun getTopScores(style: GameStyle, language: Language, difficulty: Difficulty): List<TopScoresDto> =
            listOf(TopScoresDto(style, language, difficulty, getTopScoresInternal(style, language, difficulty)))

    private fun getTopScoresInternal(style: GameStyle, language: Language, difficulty: Difficulty): MutableList<TopScore> =
            topScoresByType.getOrPut(Type(style, language, difficulty)) {
                MutableList(size) { TopScore() }
            }

    /**
     * Update the stored top scores with some round scores (that are expected to be sorted by decreasing speed).
     */
    @Synchronized
    fun updateTopScores(style: GameStyle, language: Language, difficulty: Difficulty, scores: List<Score>, wordsCount: Int) {
        val topScores = getTopScoresInternal(style, language, difficulty)

        // Since the round scores are sorted by decreasing speed, a single pass is enough
        scores.forEach { score ->
            // Ignore scores that did not caught all words
            if (score.points == wordsCount) {

                var previousRank = 0 // one-indexed, 0 means not ranked
                var newRank = 0 // one-indexed, 0 means not ranked
                var speed = 0.0f

                // Find the previous and new rank of the player
                for (i in topScores.size - 1 downTo 0) {
                    val topScore = topScores[i]

                    if (score.userName == topScore.userName) {
                        previousRank = i + 1
                        speed = topScore.speed
                    }

                    val nextTopScore = if (i > 0) topScores[i - 1] else TopScore("", Float.MAX_VALUE)

                    if (score.speed > topScore.speed && score.speed <= nextTopScore.speed) {
                        newRank = i + 1
                        speed = score.speed
                    }
                }

                // The new rank cannot be worst the the previous rank
                if (newRank > 0 && previousRank > 0 && newRank > previousRank) {
                    newRank = 0
                }

                // Update the top scores
                if (newRank > 0) {
                    when {
                        previousRank == 0 -> {
                            topScores.add(newRank - 1, TopScore(score.userName, speed))

                            if (topScores.size > size) {
                                topScores.removeAt(size)
                            }
                        }
                        previousRank > newRank -> {
                            topScores.removeAt(previousRank - 1)
                            topScores.add(newRank - 1, TopScore(score.userName, speed))
                        }
                        previousRank == newRank -> {
                            topScores[previousRank - 1] = TopScore(score.userName, speed)
                        }
                    }
                }

                // Update the player score
                val rank = if (newRank > 0) newRank else previousRank
                if (rank > 0) {
                    score.updateTops(rank, speed)
                }
            }
        }
    }
}
