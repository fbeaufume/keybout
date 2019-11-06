package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.Constants.MAX_WORD_LENGTH
import com.adeliosys.keybout.model.Constants.MIN_WORD_LENGTH
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

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

    private var wordsByLang = mutableMapOf<String, MutableList<String>>()

    @PostConstruct
    private fun init() {
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
            if (it.length in MIN_WORD_LENGTH..MAX_WORD_LENGTH) {
                words.add(it)
            }
        }

        logger.info("Loaded {} '{}' words", words.size, lang)

        wordsByLang[lang] = words
    }

    /**
     * Generate random words.
     */
    fun generateWords(language: String, count: Int, minLength: Int, maxLength: Int): List<String> {
        val possibleWords = wordsByLang[language]!!
        val selectedWords = mutableListOf<String>()

        while (selectedWords.size < count) {
            val selectedWord = possibleWords[(0..possibleWords.size).random()]

            // Check the word length
            if (selectedWord.length !in minLength..maxLength) {
                continue
            }

            // Check if no other word begins with the same letters
            var found = false
            for (word in selectedWords) {
                if (word.startsWith(selectedWord)) {
                    found = true
                    break
                }
            }
            if (!found) {
                selectedWords.add(selectedWord)
            }
        }

        return selectedWords
    }
}
