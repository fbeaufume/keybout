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
        override fun transform(label: String, difficulty: Difficulty): String {
            return label
        }
    },
    /**
     * Replace one or more letters of the word by an underscore, e.g. "history" becomes "hist_r_".
     */
    @SerializedName("hidden")
    HIDDEN(8L) {
        override fun transform(label: String, difficulty: Difficulty): String {
            val underscoreCount = when (difficulty) {
                Difficulty.EASY -> 1
                Difficulty.NORMAL -> 2
                Difficulty.HARD -> 3
            }

            var result = label
            do {
                val position = Random.nextInt(0, label.length)
                result = result.replaceRange(position, position + 1, "_")
            } while (result.count { it == '_' } < underscoreCount)

            return result
        }
    },
    /**
     * Shuffle the letters, e.g. "history" becomes "shyriot".
     */
    @SerializedName("anagram")
    ANAGRAM(10L) {
        override fun transform(label: String, difficulty: Difficulty): String {
            var result: String
            do {
                result = String(label.toList().shuffled().toCharArray())
            } while (result == label)
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

    abstract fun transform(label: String, difficulty: Difficulty): String

    fun getExpirationDuration(wordCount: Int): Long = delay * wordCount
}
