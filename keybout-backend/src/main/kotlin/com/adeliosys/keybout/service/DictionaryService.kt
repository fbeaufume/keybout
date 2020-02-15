package com.adeliosys.keybout.service

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

    private var labelsByLang = mutableMapOf<Language, MutableList<String>>()

    init {
        Language.values().forEach { loadLabels(it) }
    }

    /**
     * Load the labels for one language.
     */
    private fun loadLabels(language: Language) {
        logger.debug("Loading '{}' words", language)

        val words = mutableListOf<String>()

        javaClass.getResource("/words-${language.code}.txt").openStream().bufferedReader(Charsets.UTF_8).readLines().forEach {
            if (it.length in MIN_WORD_LENGTH..MAX_WORD_LENGTH) {
                words.add(it)
            }
        }

        logger.info("Loaded {} '{}' words", words.size, language)

        labelsByLang[language] = words
    }

    /**
     * Return the labels of a given language.
     */
    fun getLabels(language: Language): List<String> {
        return labelsByLang[language]!!
    }

    /**
     * Generate random words.
     */
    fun generateWords(language: Language, count: Int, style: GameStyle, difficulty: Difficulty): List<Word> {
        val possibleLabels = getLabels(language)
        val selectedWords = mutableListOf<Word>()

        while (selectedWords.size < count) {
            val label = possibleLabels[Random.nextInt(0, possibleLabels.size)]

            // Check the word length
            if (label.length !in difficulty.getRange(style)) {
                continue
            }

            // Check if the selected word does not conflict with another word
            var conflict = false
            for (word in selectedWords) {
                if (word.conflictsWith(label)) {
                    conflict = true
                    break
                }
            }
            if (!conflict) {
                selectedWords.add(Word(label, style.transform(label, difficulty)))
            }
        }

        return selectedWords
    }
}
