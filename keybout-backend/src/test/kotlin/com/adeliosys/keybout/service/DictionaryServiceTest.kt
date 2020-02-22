package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.Constants.MAX_GENERATOR_ATTEMPTS
import com.adeliosys.keybout.model.Difficulty
import com.adeliosys.keybout.model.GameStyle
import com.adeliosys.keybout.model.Language
import com.adeliosys.keybout.model.Word
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DictionaryServiceTest {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val service = DictionaryService()

    private val wordsByLanguageAndLength: MutableMap<Language, MutableMap<Pair<GameStyle, Difficulty>, Int>> = mutableMapOf()

    @BeforeAll
    fun beforeAll() {
        // Count the words for each style-difficulty pair
        Language.realLanguages().forEach { lang ->
            service.getValues(lang).forEach { value ->
                GameStyle.letterStyles.forEach { style ->
                    Difficulty.values().forEach { difficulty ->
                        if (value.length in difficulty.getLetterRange(style)) {
                            with(wordsByLanguageAndLength.getOrPut(lang) { mutableMapOf() }) {
                                put(Pair(style, difficulty), getOrPut(Pair(style, difficulty)) { 0 } + 1)
                            }
                        }
                    }
                }
            }
        }
    }

    @TestFactory
    fun countWords(): List<DynamicTest> {
        val tests = mutableListOf<DynamicTest>()
        GameStyle.letterStyles.forEach { style ->
            Language.realLanguages().forEach { language ->
                Difficulty.values().forEach { difficulty ->
                    tests.add(DynamicTest.dynamicTest("count $style $language $difficulty") {
                        countWords(style, language, difficulty)
                    })
                }
            }
        }
        return tests
    }

    private fun countWords(style: GameStyle, language: Language, difficulty: Difficulty) {
        val count = wordsByLanguageAndLength[language]!![Pair(style, difficulty)]!!
        assertTrue(count >= 500) {
            "Not enough words for style $style and difficulty $difficulty in $language, found only $count"
        }
    }

    @TestFactory
    fun generateWords(): List<DynamicTest> {
        val tests = mutableListOf<DynamicTest>()
        GameStyle.letterStyles.forEach { style ->
            Language.realLanguages().forEach { language ->
                Difficulty.values().forEach { difficulty ->
                    tests.add(DynamicTest.dynamicTest("generate $style $language $difficulty") {
                        generateWords(style, language, difficulty)
                    })
                }
            }
        }
        return tests
    }

    private fun generateWords(style: GameStyle, language: Language, difficulty: Difficulty) {
        val count = 80 // Number of words to generate

        val (words, attempts) = service.generateWords(language, count, style, difficulty)

        logger.info("Generated ${words.size} words in $attempts attempts for $style style, $language language and $difficulty difficulty")

        // Check the number of words
        assertEquals(count, words.size) {
            "Words count of $count is incorrect"
        }

        // Check the number of attempts
        assertTrue(attempts <= MAX_GENERATOR_ATTEMPTS / 2) {
            "Attempts count of $attempts is too high"
        }

        // Check the words length
        words.forEach {
            val range = difficulty.getLetterRange(style)
            assertTrue(it.value.length in range) {
                "Incorrect length of word '${it.value}', expected between ${range.first} and ${range.last}"
            }
        }

        checkConflicts(words)
    }

}

/**
 * Check that no word is the beginning of another word.
 */
fun checkConflicts(words: List<Word>) {
    val count = words.size
    for (i in 0 until count - 1) {
        for (j in i + 1 until count) {
            assertFalse(words[i].conflictsWith(words[j].value)) {
                "${words[i].value} conflicts with ${words[j].value}"
            }
        }
    }
}
