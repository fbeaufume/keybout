package com.adeliosys.keybout.model

import com.google.gson.annotations.SerializedName
import kotlin.random.Random

/**
 * Styles are used to change the display of words, to make games a little more challenging.
 */
enum class GameStyle(private val delay: Long) {

    /**
     * The word is unchanged, e.g. "history" remains "history".
     */
    @SerializedName("regular")
    REGULAR(5L) {
        override fun transform(word: String): String {
            return word
        }
    },
    /**
     * Replace one or more letters of the word by an underscore, e.g. "history" becomes "hist_r_".
     */
    @SerializedName("hidden")
    HIDDEN(8L) {
        override fun transform(word: String): String {
            val underscoreCount = when (word.length) {
                in 1..6 -> 1
                in 7..10 -> 2
                else -> 3
            }

            var result = word
            do {
                val position = Random.nextInt(0, word.length)
                result = result.replaceRange(position, position + 1, "_")
            } while (result.count { it == '_' } < underscoreCount)

            return result
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
            REGULAR
        }
    }

    abstract fun transform(word: String): String

    fun getExpirationDuration(wordCount: Int): Long = delay * wordCount
}
