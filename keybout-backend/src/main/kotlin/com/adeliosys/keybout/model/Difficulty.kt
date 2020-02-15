package com.adeliosys.keybout.model

import com.google.gson.annotations.SerializedName

/**
 * The word length ranges.
 *
 * Tips for calculus difficulties in order to prevent generation conflicts:
 * - Do not generate a single digit result
 * - Use large ranges
 * - Ensure add and subtract ranges do not overlap
 */
enum class Difficulty(
        private val letterRanges: Map<GameStyle, IntRange>,
        val additionLeftRange: IntRange,
        val additionRightRange: IntRange,
        val subtractionLeftRange: IntRange,
        val subtractionRightRange: IntRange,
        val multiplicationLeftRange: IntRange,
        val multiplicationRightRange: IntRange) {

    @SerializedName("easy")
    EASY(mapOf(GameStyle.REGULAR to 4..6, GameStyle.HIDDEN to 5..7, GameStyle.ANAGRAM to 3..5),
            40..89, 1..10, // 59 result is in 41..99
            20..59, 1..10, // 49 result is in 10..58
            2..11, 2..10), // 90 result is in 4..110
    @SerializedName("normal")
    NORMAL(mapOf(GameStyle.REGULAR to 5..7, GameStyle.HIDDEN to 6..8, GameStyle.ANAGRAM to 4..6),
            41..69, 11..29, // 47 result is in 52..98
            31..59, 11..19, // 37 result is in 12..48
            11..19, 2..9), // 72 result is in 22..171
    @SerializedName("hard")
    HARD(mapOf(GameStyle.REGULAR to 7..9, GameStyle.HIDDEN to 7..9, GameStyle.ANAGRAM to 5..7),
            111..159, 21..39, // 67 result is in 132..198
            101..149, 21..39, // 67 result is in 62..128
            11..19, 11..16); // 54 result is in 121..304

    companion object {
        fun getByCode(code: String) = try {
            valueOf(code.toUpperCase())
        } catch (e: Exception) {
            EASY
        }
    }

    fun getLetterRange(style: GameStyle) = letterRanges[style]!!
}
