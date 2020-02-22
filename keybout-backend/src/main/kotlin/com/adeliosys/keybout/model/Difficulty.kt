package com.adeliosys.keybout.model

import com.google.gson.annotations.SerializedName

/**
 * The word length ranges.
 */
enum class Difficulty(
        private val letterRanges: Map<GameStyle, IntRange>,
        val additionRanges: Pair<IntRange, IntRange>,
        val subtractionRanges: Pair<IntRange, IntRange>) {

    @SerializedName("easy")
    EASY(mapOf(GameStyle.REGULAR to 4..6, GameStyle.HIDDEN to 5..7, GameStyle.ANAGRAM to 3..5),
            Pair(100..190, 1..10), // 100 result is in 101..200
            Pair(30..100, 1..10)), // 80 results in 20..99
    @SerializedName("normal")
    NORMAL(mapOf(GameStyle.REGULAR to 5..7, GameStyle.HIDDEN to 6..8, GameStyle.ANAGRAM to 4..6),
            Pair(100..190, 11..19), // 99 result is in 111..209
            Pair(40..120, 11..19)), // 89 result is in 21..109
    @SerializedName("hard")
    HARD(mapOf(GameStyle.REGULAR to 7..9, GameStyle.HIDDEN to 7..9, GameStyle.ANAGRAM to 5..7),
            Pair(100..190, 21..39), // 108 result is in 121..229
            Pair(70..149, 21..29)); // 88 result is in 41..128

    companion object {
        fun getByCode(code: String) = try {
            valueOf(code.toUpperCase())
        } catch (e: Exception) {
            EASY
        }
    }

    fun getLetterRange(style: GameStyle) = letterRanges[style]!!
}
