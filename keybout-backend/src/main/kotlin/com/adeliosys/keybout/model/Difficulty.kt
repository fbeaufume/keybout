package com.adeliosys.keybout.model

import com.google.gson.annotations.SerializedName

/**
 * The word length ranges.
 */
enum class Difficulty(private val ranges: Map<GameStyle, IntRange>) {

    @SerializedName("easy")
    EASY(mapOf(GameStyle.REGULAR to 4..6, GameStyle.HIDDEN to 5..7, GameStyle.ANAGRAM to 3..5)),
    @SerializedName("normal")
    NORMAL(mapOf(GameStyle.REGULAR to 5..8, GameStyle.HIDDEN to 6..8, GameStyle.ANAGRAM to 4..6)),
    @SerializedName("hard")
    HARD(mapOf(GameStyle.REGULAR to 7..10, GameStyle.HIDDEN to 7..9, GameStyle.ANAGRAM to 5..7));

    companion object {
        fun getByCode(code: String) = try {
            valueOf(code.toUpperCase())
        } catch (e: Exception) {
            EASY
        }
    }

    fun getRange(style: GameStyle) = ranges[style]!!
}
