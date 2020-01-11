package com.adeliosys.keybout.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WordEffectTest {

    @Test
    fun none() {
        assertEquals("notebook", WordEffect.NONE.transform("notebook"))
    }

    @Test
    fun `hidden short`() {
        val result = WordEffect.HIDDEN.transform("dancer")
        assertTrue(result == "_ancer" || result == "d_ncer" || result == "da_cer" || result == "dan_er"
                || result == "danc_r" || result == "dance_") { "Wrong result '$result'" }
    }

    @Test
    fun `hidden long`() {
        val word = "grocery"
        val result = WordEffect.HIDDEN.transform(word)
        assertTrue(result.count { it == '_' } == 2) { "Wrong count of underscores in '$result'" }
        for (i in result.indices) {
            assertTrue(result[i] == word[i] || result[i] == '_') { "Wrong character in position $i of '$result'" }
        }
    }

    @Test
    fun reverse() {
        assertEquals("koobeton", WordEffect.REVERSE.transform("notebook"))
    }

    @Test
    fun anagram() {
        val result = WordEffect.ANAGRAM.transform("range")
        assertEquals(5, result.length)
        assertTrue(result.contains("r") && result.contains("a")
                && result.contains("n") && result.contains("g") && result.contains("e")) {
            "Wrong result '$result'"
        }
    }
}
