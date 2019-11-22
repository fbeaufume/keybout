package com.adeliosys.keybout.util

import org.slf4j.LoggerFactory
import java.io.File

// Utility methods to clean dictionary files, i.e. sort the words, remove duplicates, etc.

private val logger = LoggerFactory.getLogger("com.adeliosys.keybout.util.DictionaryCleaner")

fun main(args: Array<String>) {
    //logger.info("Working folder is '{}'", File(".").absolutePath)
    if (args.isEmpty()) {
        logger.warn("No file name is program arguments, exiting")
    } else {
        args.forEach { clean(it) }
    }
}

// Clean one dictionary file
private fun clean(fileName: String) {
    logger.info("Processing '{}'", fileName)
    var duration = -System.currentTimeMillis()

    val inputWords = mutableSetOf<String>()

    // Read the words and remove the duplicates
    var inputCount = 0
    var trimCount = 0
    var lowercaseCount = 0
    var ignoreCount = 0
    File(fileName).forEachLine {
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
        }
        else {
            inputWords.add(word2)
        }
    }

    // Sort the words
    val outputWords = inputWords.sorted()
    val removeCount = inputCount - outputWords.size - ignoreCount

    // Write the words
    File(fileName).printWriter().use { file -> outputWords.forEach { file.println(it) } }

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
