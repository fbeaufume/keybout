package com.adeliosys.keybout.model

import com.google.gson.annotations.SerializedName
import kotlin.random.Random

/**
 * Effects are used to change the display of words, to make games a little more challenging.
 */
enum class WordEffect(private val delay: Long) {

    /**
     * No effect, e.g. "history" remains "history".
     */
    @SerializedName("none")
    NONE(5L) {
        override fun transform(word: String): String {
            return word
        }
    },
    /**
     * Replace one letter of the word by an underscore, e.g. "history" becomes "hist_ry".
     */
    @SerializedName("hidden")
    HIDDEN(8L) {
        override fun transform(word: String): String {
            val position = Random.nextInt(0, word.length)
            return word.replaceRange(position, position + 1, "_")
        }
    },
    /**
     * Reverse the letters, e.g. "history" becomes "yrotsih".
     */
    @SerializedName("reverse")
    REVERSE(7L) {
        override fun transform(word: String): String {
            return word.reversed()
        }
    },
    /**
     * Shuffle the letters, e.g. "history" becomes "shyriot".
     */
    @SerializedName("anagram")
    ANAGRAM(10L) {
        override fun transform(word: String): String {
            var result: String
            do {
                result = String(word.toList().shuffled().toCharArray())
            } while (result == word)
            return result
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

    fun getExpirationDuration(wordCount: Int): Long = delay * wordCount
}
