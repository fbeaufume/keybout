package com.adeliosys.keybout.model

import com.google.gson.annotations.SerializedName
import kotlin.random.Random

/**
 * Styles are used to change the display of words, to make games a little more challenging.
 */
enum class GameStyle(val isLetterStyle: Boolean, private val delay: Long) {

    /**
     * The word is unchanged, e.g. "history" remains "history".
     */
    @SerializedName("regular")
    REGULAR(true, 5L) {
        override fun transform(value: String, difficulty: Difficulty): String = value
    },
    /**
     * Replace one or more letters of the word by an underscore, e.g. "history" becomes "hist_r_".
     */
    @SerializedName("hidden")
    HIDDEN(true, 15L) {
        override fun transform(value: String, difficulty: Difficulty): String {
            val underscoreCount = when (difficulty) {
                Difficulty.EASY -> 1
                Difficulty.NORMAL -> 2
                Difficulty.HARD -> 3
            }

            var result = value
            do {
                val position = Random.nextInt(0, value.length)
                result = result.replaceRange(position, position + 1, "_")
            } while (result.count { it == '_' } < underscoreCount)

            return result
        }
    },
    /**
     * Shuffle the letters, e.g. "history" becomes "shyriot".
     */
    @SerializedName("anagram")
    ANAGRAM(true, 20L) {
        override fun transform(value: String, difficulty: Difficulty): String {
            var result: String
            do {
                result = String(value.toList().shuffled().toCharArray())
            } while (result == value)
            return result
        }
    },
    /**
     * Use a calculus, e.g. "16 + 7".
     */
    @SerializedName("calculus")
    CALCULUS(false, 10L) {
        override fun transform(value: String, difficulty: Difficulty): String = value
    };

    companion object {
        val letterStyles = values().filter { it.isLetterStyle }

        fun getByCode(code: String) = try {
            valueOf(code.toUpperCase())
        } catch (e: Exception) {
            REGULAR
        }
    }

    abstract fun transform(value: String, difficulty: Difficulty): String

    fun getExpirationDuration(wordCount: Int): Long = delay * wordCount
}
