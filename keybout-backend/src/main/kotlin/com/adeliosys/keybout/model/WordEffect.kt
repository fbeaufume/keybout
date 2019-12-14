package com.adeliosys.keybout.model

import kotlin.random.Random

/**
 * Effects are used to change the display of words, to make games a little more challenging.
 */
enum class WordEffect {

    /**
     * No effect, e.g. "history" remains "history".
     */
    NONE {
        override fun transform(word: String): String {
            return word
        }
    },
    /**
     * Replace one letter of the word by an underscore, e.g. "history" becomes "hist_ry".
     */
    HIDDEN {
        override fun transform(word: String): String {
            val position = Random.nextInt(0, word.length)
            return word.replaceRange(position, position + 1, "_")
        }
    },
    /**
     * Reverse the letters, e.g. "history" becomes "yrotsih".
     */
    REVERSE {
        override fun transform(word: String): String {
            return word.reversed()
        }
    },
    /**
     * Shuffle the letters, e.g. "history" becomes "shyriot".
     */
    SHUFFLE {
        override fun transform(word: String): String {
            return String(word.toList().shuffled().toCharArray())
        }
    };

    companion object {
        fun getByCode(code: String) = try {
            valueOf(code.toUpperCase())
        } catch (e: Exception) {
            NONE
        }
    }

    abstract fun transform(word: String): String
}
