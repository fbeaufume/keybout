package com.adeliosys.keybout.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameStyleTest {

    @Test
    fun regular() {
        assertEquals("notebook", GameStyle.REGULAR.transform("notebook", Difficulty.EASY))
    }

    @Test
    fun `hidden easy`() {
        val result = GameStyle.HIDDEN.transform("dancer", Difficulty.EASY)
        assertTrue(result == "_ancer" || result == "d_ncer" || result == "da_cer" || result == "dan_er"
                || result == "danc_r" || result == "dance_") { "Wrong result '$result'" }
    }

    @Test
    fun `hidden normal`() {
        val word = "grocery"
        val result = GameStyle.HIDDEN.transform(word, Difficulty.NORMAL)
        assertTrue(result.count { it == '_' } == 2) { "Wrong count of underscores in '$result'" }
        for (i in result.indices) {
            assertTrue(result[i] == word[i] || result[i] == '_') { "Wrong character in position $i of '$result'" }
        }
    }

    @Test
    fun anagram() {
        val result = GameStyle.ANAGRAM.transform("range", Difficulty.EASY)
        assertEquals(5, result.length)
        assertTrue("range" != result) {
            "Result is the same word"
        }
        assertTrue(result.contains("r") && result.contains("a")
                && result.contains("n") && result.contains("g") && result.contains("e")) {
            "Wrong result '$result'"
        }
    }
}
