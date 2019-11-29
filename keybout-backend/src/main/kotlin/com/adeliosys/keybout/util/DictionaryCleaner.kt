package com.adeliosys.keybout.util

import com.adeliosys.keybout.service.DictionaryService
import org.slf4j.LoggerFactory
import java.io.File

private val logger = LoggerFactory.getLogger("DictionaryCleaner")

/**
 * Clean the dictionary files, i.e. sort the words, remove duplicates, etc.
 */
fun main(args: Array<String>) {
    listOf("en", "fr").forEach { lang ->
        logger.info("Processing '{}' dictionary", lang)
        var duration = -System.currentTimeMillis()

        val inputWords = mutableSetOf<String>()

        // Read the words and remove the duplicates
        var inputCount = 0
        var trimCount = 0
        var lowercaseCount = 0
        var ignoreCount = 0
        getFileForLang(lang).forEachLine {
            inputCount++

            // Trim the word
            val word1 = it.trim()
            if (word1 != it) {
                trimCount++
            }

            // Lowercase the word
            val word2 = word1.toLowerCase()
            if (word2 != word1) {
                lowercaseCount++
            }

            if (word2.contains(' ') || word2.isEmpty()) {
                ignoreCount++
            } else {
                inputWords.add(word2)
            }
        }

        // Sort the words
        val outputWords = inputWords.sorted()
        val removeCount = inputCount - outputWords.size - ignoreCount

        // Write the words
        getFileForLang(lang).printWriter().use { file -> outputWords.forEach { file.println(it) } }

        duration += System.currentTimeMillis()
        logger.info("Done in {} msec (loaded {} word{}, trimmed {}, lowercased {}, ignored {}, removed {}, wrote {})",
                duration,
                inputCount,
                if (inputCount > 1) "s" else "",
                trimCount,
                lowercaseCount,
                ignoreCount,
                removeCount,
                outputWords.size)
    }
}

/**
 * Return a dictionary source file.
 */
fun getFileForLang(lang: String) = File(DictionaryService::class.java.protectionDomain.codeSource.location.path
        + "../../src/main/resources/words-${lang}.txt")
