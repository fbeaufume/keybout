package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Difficulty.HARD
import com.adeliosys.keybout.model.Difficulty.NORMAL
import com.adeliosys.keybout.model.GameStyle.HIDDEN
import com.adeliosys.keybout.model.GameStyle.REGULAR
import com.adeliosys.keybout.model.Language.EN
import com.adeliosys.keybout.model.Language.FR
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(Alphanumeric::class)
class ScoreServiceTest {

    private val size = 3

    private val service = ScoreService(size)

    @Test
    fun `step 01 empty scores`() {
        verifyTopScores(REGULAR, EN, NORMAL, TopScore(), TopScore(), TopScore())
    }

    @Test
    fun `step 02 add Alice`() {
        verifyPlayerScores(updateTopScores(Score("Alice", 8f)),
                Score(1, 8f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Alice", 8f),
                TopScore(),
                TopScore())

        verifyTopScores(HIDDEN, EN, NORMAL, TopScore(), TopScore(), TopScore())
        verifyTopScores(REGULAR, FR, NORMAL, TopScore(), TopScore(), TopScore())
        verifyTopScores(REGULAR, EN, HARD, TopScore(), TopScore(), TopScore())
    }

    @Test
    fun `step 03 change Alice`() {
        verifyPlayerScores(updateTopScores(Score("Alice", 6f)),
                Score(1, 8f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Alice", 8f),
                TopScore(),
                TopScore())
    }

    @Test
    fun `step 04 change Alice`() {
        verifyPlayerScores(updateTopScores(Score("Alice", 10f)),
                Score(1, 10f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Alice", 10f),
                TopScore(),
                TopScore())
    }

    @Test
    fun `step 05 add Bob`() {
        verifyPlayerScores(updateTopScores(Score("Bob", 5f)),
                Score(2, 5f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Alice", 10f),
                TopScore("Bob", 5f),
                TopScore())
    }

    @Test
    fun `step 06 change Bob`() {
        verifyPlayerScores(updateTopScores(Score("Bob", 10f)),
                Score(2, 10f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Alice", 10f),
                TopScore("Bob", 10f),
                TopScore())
    }

    @Test
    fun `step 07 change Bob`() {
        verifyPlayerScores(updateTopScores(Score("Bob", 15f)),
                Score(1, 15f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Bob", 15f),
                TopScore("Alice", 10f),
                TopScore())
    }

    @Test
    fun `step 08 add Charlie`() {
        verifyPlayerScores(updateTopScores(Score("Charlie", 20f)),
                Score(1, 20f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Charlie", 20f),
                TopScore("Bob", 15f),
                TopScore("Alice", 10f))
    }

    @Test
    fun `step 09 change Bob and Alice`() {
        verifyPlayerScores(updateTopScores(Score("Alice", 30f), Score("Bob", 25f)),
                Score(1, 30f),
                Score(2, 25f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Alice", 30f),
                TopScore("Bob", 25f),
                TopScore("Charlie", 20f))
    }

    @Test
    fun `step 10 add David`() {
        verifyPlayerScores(updateTopScores(Score("David", 18f)),
                Score(0, 0f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Alice", 30f),
                TopScore("Bob", 25f),
                TopScore("Charlie", 20f))
    }

    @Test
    fun `step 11 add David`() {
        verifyPlayerScores(updateTopScores(Score("David", 22f)),
                Score(3, 22f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Alice", 30f),
                TopScore("Bob", 25f),
                TopScore("David", 22f))
    }

    @Test
    fun `step 12 add Eric`() {
        verifyPlayerScores(updateTopScores(Score("Eric", 40f)),
                Score(1, 40f))

        verifyTopScores(REGULAR, EN, NORMAL,
                TopScore("Eric", 40f),
                TopScore("Alice", 30f),
                TopScore("Bob", 25f))
    }

    private fun updateTopScores(vararg scores: Score): List<Score> {
        return scores.asList().also {
            service.updateTopScores(REGULAR, EN, NORMAL, it)
        }
    }

    private fun verifyPlayerScores(scores: List<Score>, vararg expectedScores: Score) {
        assertEquals(expectedScores.count(), scores.count(), "Wrong size")
        for (i in 0 until scores.count()) {
            assertEquals(expectedScores[i].topRank, scores[i].topRank, "Wrong rank")
            assertEquals(expectedScores[i].topSpeed, scores[i].topSpeed, "Wrong speed")
        }
    }

    private fun verifyTopScores(style: GameStyle, language: Language, difficulty: Difficulty, vararg expectedScores: TopScore) {
        assertEquals(expectedScores.asList(), service.getTopScores(style, language, difficulty))
    }
}
