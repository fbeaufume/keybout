package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.WordLength
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DictionaryServiceTest {

    private val service = DictionaryService()

    @Test
    fun `check shortest en`() {
        checkWords("en", WordLength.SHORTEST)
    }

    @Test
    fun `check shorter en`() {
        checkWords("en", WordLength.SHORTER)
    }

    @Test
    fun `check standard en`() {
        checkWords("en", WordLength.STANDARD)
    }

    @Test
    fun `check longer en`() {
        checkWords("en", WordLength.LONGER)
    }

    @Test
    fun `check longest en`() {
        checkWords("en", WordLength.LONGEST)
    }

    @Test
    fun `check shortest fr`() {
        checkWords("fr", WordLength.SHORTEST)
    }

    @Test
    fun `check shorter fr`() {
        checkWords("fr", WordLength.SHORTER)
    }

    @Test
    fun `check standard fr`() {
        checkWords("fr", WordLength.STANDARD)
    }

    @Test
    fun `check longer fr`() {
        checkWords("fr", WordLength.LONGER)
    }

    @Test
    fun `check longest fr`() {
        checkWords("fr", WordLength.LONGEST)
    }

    private fun checkWords(language: String, wordsLength: WordLength) {
        val count = 50 // Number of words to generate

        val words = service.generateWords(language, count, wordsLength)

        // Check the number of words
        assertEquals(count, words.size)

        // Check the words length
        words.forEach {
            assertTrue(it.length in wordsLength.getRange()) {
                "Incorrect length of word '$it', expected between ${wordsLength.getRange().first} and ${wordsLength.getRange().last}"
            }
        }

        // Check that no word is the beginning of another word
        for (i in 0 until count - 1) {
            for (j in i + 1 until count) {
                assertFalse(words[i].startsWith(words[j])) {
                    "${words[i]} starts with ${words[j]}"
                }
                assertFalse(words[j].startsWith(words[i])) {
                    "${words[j]} starts with ${words[i]}"
                }
            }
        }
    }
}
