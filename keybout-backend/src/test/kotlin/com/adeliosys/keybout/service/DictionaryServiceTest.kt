package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.WordLength
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DictionaryServiceTest {

    private val service = DictionaryService()

    private val wordsByLanguageAndLength: MutableMap<String, MutableMap<WordLength, Int>> = mutableMapOf()

    @BeforeAll
    fun beforeAll() {
        // Count the words for each length
        listOf("en", "fr").forEach { lang ->
            service.getWords(lang).forEach { word ->
                WordLength.values().forEach { length ->
                    if (word.length in length.getRange()) {
                        with(wordsByLanguageAndLength.getOrPut(lang) { mutableMapOf() }) {
                            put(length, getOrPut(length) { 0 } + 1)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `count shortest en`() {
        countWords("en", WordLength.SHORTEST)
    }

    @Test
    fun `count shorter en`() {
        countWords("en", WordLength.SHORTER)
    }

    @Test
    fun `count standard en`() {
        countWords("en", WordLength.STANDARD)
    }

    @Test
    fun `count longer en`() {
        countWords("en", WordLength.LONGER)
    }

    @Test
    fun `count longest en`() {
        countWords("en", WordLength.LONGEST)
    }

    @Test
    fun `count shortest fr`() {
        countWords("fr", WordLength.SHORTEST)
    }

    @Test
    fun `count shorter fr`() {
        countWords("fr", WordLength.SHORTER)
    }

    @Test
    fun `count standard fr`() {
        countWords("fr", WordLength.STANDARD)
    }

    @Test
    fun `count longer fr`() {
        countWords("fr", WordLength.LONGER)
    }

    @Test
    fun `count longest fr`() {
        countWords("fr", WordLength.LONGEST)
    }

    private fun countWords(language: String, length: WordLength) {
        val count = wordsByLanguageAndLength[language]!![length]!!
        assertTrue(count >= 500) {
            "Not enough words for length $length in $language, found only $count"
        }
    }

    @Test
    fun `generate shortest en`() {
        generateWords("en", WordLength.SHORTEST)
    }

    @Test
    fun `generate shorter en`() {
        generateWords("en", WordLength.SHORTER)
    }

    @Test
    fun `generate standard en`() {
        generateWords("en", WordLength.STANDARD)
    }

    @Test
    fun `generate longer en`() {
        generateWords("en", WordLength.LONGER)
    }

    @Test
    fun `generate longest en`() {
        generateWords("en", WordLength.LONGEST)
    }

    @Test
    fun `generate shortest fr`() {
        generateWords("fr", WordLength.SHORTEST)
    }

    @Test
    fun `generate shorter fr`() {
        generateWords("fr", WordLength.SHORTER)
    }

    @Test
    fun `generate standard fr`() {
        generateWords("fr", WordLength.STANDARD)
    }

    @Test
    fun `generate longer fr`() {
        generateWords("fr", WordLength.LONGER)
    }

    @Test
    fun `generate longest fr`() {
        generateWords("fr", WordLength.LONGEST)
    }

    private fun generateWords(language: String, wordsLength: WordLength) {
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
