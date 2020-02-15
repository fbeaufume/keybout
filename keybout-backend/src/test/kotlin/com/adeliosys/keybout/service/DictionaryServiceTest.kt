package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.Difficulty
import com.adeliosys.keybout.model.GameStyle
import com.adeliosys.keybout.model.Language
import com.adeliosys.keybout.model.Word
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DictionaryServiceTest {

    private val service = DictionaryService()

    private val wordsByLanguageAndLength: MutableMap<Language, MutableMap<Pair<GameStyle, Difficulty>, Int>> = mutableMapOf()

    @BeforeAll
    fun beforeAll() {
        // Count the words for each style-difficulty pair
        Language.values().forEach { lang ->
            service.getLabels(lang).forEach { value ->
                GameStyle.values().forEach { style ->
                    Difficulty.values().forEach { difficulty ->
                        if (value.length in difficulty.getRange(style)) {
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
        GameStyle.values().forEach { style ->
            Language.values().forEach { language ->
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
        GameStyle.values().forEach { style ->
            Language.values().forEach { language ->
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

        val words = service.generateWords(language, count, style, difficulty)

        // Check the number of words
        assertEquals(count, words.size) {
            "Words count of $count is incorrect"
        }

        // Check the words length
        words.forEach {
            val range = difficulty.getRange(style)
            assertTrue(it.label.length in range) {
                "Incorrect length of word '${it.label}', expected between ${range.first} and ${range.last}"
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
            assertFalse(words[i].conflictsWith(words[j].label)) {
                "${words[i].display} conflicts with ${words[j].display}"
            }
        }
    }
}
