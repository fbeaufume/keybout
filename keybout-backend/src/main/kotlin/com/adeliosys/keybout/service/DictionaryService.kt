package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.Constants.MAX_GENERATOR_ATTEMPTS
import com.adeliosys.keybout.model.Constants.MAX_WORD_LENGTH
import com.adeliosys.keybout.model.Constants.MIN_WORD_LENGTH
import com.adeliosys.keybout.model.Difficulty
import com.adeliosys.keybout.model.GameStyle
import com.adeliosys.keybout.model.Language
import com.adeliosys.keybout.model.Word
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random

/**
 * Provide the words for a round.
 */
@Service
class DictionaryService {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private var valuesByLang = mutableMapOf<Language, MutableList<String>>()

    init {
        Language.realLanguages().forEach { loadValues(it) }
    }

    /**
     * Load the values for one language.
     */
    private fun loadValues(language: Language) {
        logger.debug("Loading '{}' words", language)

        val words = mutableListOf<String>()

        javaClass.getResource("/data/words-${language.code}.txt")!!.openStream().bufferedReader(Charsets.UTF_8).readLines().forEach {
            if (it.length in MIN_WORD_LENGTH..MAX_WORD_LENGTH) {
                words.add(it)
            }
        }

        logger.info("Loaded {} '{}' words", words.size, language)

        valuesByLang[language] = words
    }

    /**
     * Return the values of a given language.
     */
    fun getValues(language: Language): List<String> {
        return valuesByLang[language]!!
    }

    /**
     * Generate random words.
     */
    fun generateWords(language: Language, count: Int, style: GameStyle, difficulty: Difficulty): Pair<List<Word>, Int> {
        val possibleValues = getValues(language)
        val selectedWords = mutableListOf<Word>()
        var attempts = 0

        while (selectedWords.size < count && attempts < MAX_GENERATOR_ATTEMPTS) {
            val value = possibleValues[Random.nextInt(0, possibleValues.size)]

            // Check the word length
            if (value.length !in difficulty.getLetterRange(style)) {
                continue
            }

            addIfNoConflict(value, selectedWords, style, difficulty)

            attempts++
        }

        return Pair(selectedWords, attempts)
    }

    /**
     * Add a word to the list of words, only if the word does not conflict with any other words.
     */
    private fun addIfNoConflict(value: String, words: MutableList<Word>, style: GameStyle, difficulty: Difficulty) {
        var conflict = false

        for (word in words) {
            if (word.conflictsWith(value)) {
                conflict = true
                break
            }
        }

        if (!conflict) {
            words.add(Word(value, style.transform(value, difficulty)))
        }
    }
}

