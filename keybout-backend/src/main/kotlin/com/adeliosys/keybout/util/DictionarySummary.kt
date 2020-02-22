package com.adeliosys.keybout.util

import com.adeliosys.keybout.model.Difficulty
import com.adeliosys.keybout.model.GameStyle
import com.adeliosys.keybout.model.Language
import org.slf4j.LoggerFactory
import kotlin.math.max

private val logger = LoggerFactory.getLogger("DictionarySummary")

private const val MAX_LENGTH = 16

/**
 * Log a summary of the content of dictionary files.
 */
fun main() {
    Language.realLanguages().forEach { lang ->
        logger.info("Processing '{}' dictionary", lang)

        // Position i contains the number of words of length i
        // Position 0 contains the number of words longer than the max
        val wordsByCount = IntArray(MAX_LENGTH + 1)

        // Length of the longest word
        var maxLength = 0

        var wordsCount = 0

        // Count the words
        getFileForLang(lang).forEachLine {
            val length = it.length

            maxLength = max(length, maxLength)

            wordsCount++

            if (length > MAX_LENGTH) {
                wordsByCount[0]++
            } else {
                wordsByCount[length]++
            }
        }

        // Display the summary

        for (i in 1..MAX_LENGTH) {
            logger.info("Words of length {}: {}", i, wordsByCount[i])
        }

        logger.info("Longer words: {}", wordsByCount[0])
        logger.info("Max length: {}", maxLength)
        logger.info("Words count: {}", wordsCount)

        GameStyle.letterStyles.forEach { style ->
            Difficulty.values().forEach { difficulty ->
                var count = 0;
                val range = difficulty.getLetterRange(style)
                for (i in range) {
                    count += wordsByCount[i]
                }
                logger.info("Available words for '{}' style and '{}' difficulty (i.e. {} to {} letters): {}", style, difficulty, range.first, range.last, count)
            }
        }
    }
}
