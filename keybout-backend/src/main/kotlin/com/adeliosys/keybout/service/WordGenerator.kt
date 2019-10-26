package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.Constants.MAX_WORD_LENGTH
import com.adeliosys.keybout.model.Constants.MIN_WORD_COUNT
import com.adeliosys.keybout.model.Constants.MIN_WORD_LENGTH
import com.adeliosys.keybout.model.Word
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import kotlin.system.exitProcess

/**
 * Generate random for a game round.
 */
@Service
class WordGenerator {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Value("classpath:words-en.txt")
    private lateinit var wordsEn: Resource

    @Value("classpath:words-fr.txt")
    private lateinit var wordsFr: Resource

    @Value("\${keybout.min-word-length:$MIN_WORD_LENGTH}")
    private var minWordsLength = MIN_WORD_LENGTH

    @Value("\${keybout.max-word-length:$MAX_WORD_LENGTH}")
    private var maxWordsLength = MAX_WORD_LENGTH

    private var wordsByLang = mutableMapOf<String, MutableList<String>>()

    @PostConstruct
    private fun init() {
        logger.info("Using minimum word length of {}", minWordsLength)
        logger.info("Using maximum word length of {}", maxWordsLength)
        loadWords(wordsEn, "en")
        loadWords(wordsFr, "fr")
    }

    /**
     * Load the words for one language.
     */
    private fun loadWords(resource: Resource, lang: String) {
        logger.debug("Loading '{}' words", lang)

        val words = mutableListOf<String>()

        // Do not use resource.file, it does not work with a Spring Boot fat jar
        resource.inputStream.bufferedReader(Charsets.UTF_8).readLines().forEach {
            if (it.length in minWordsLength..maxWordsLength) {
                words.add(it)
            }
        }

        logger.info("Loaded {} '{}' words", words.size, lang)

        if (words.size < MIN_WORD_COUNT) {
            logger.error("Not enough words matching the required length parameters, use a larger range, stopping application")
            exitProcess(1)
        }

        wordsByLang[lang] = words
    }

    /**
     * Generate random words.
     */
    fun generateWords(language: String, count: Int): Map<String, Word> {
        val possibleWords = wordsByLang[language]!!
        val selectedWords = mutableMapOf<String, Word>()

        while (selectedWords.size < count) {
            val selectedWord = possibleWords[(0..possibleWords.size).random()]

            // Check if no other word begins with the same letters
            var found = false
            for (word in selectedWords.values) {
                if (word.label.startsWith(selectedWord)) {
                    found = true
                    break
                }
            }
            if (!found) {
                selectedWords[selectedWord] = Word(selectedWord)
            }
        }

        return selectedWords
    }
}
