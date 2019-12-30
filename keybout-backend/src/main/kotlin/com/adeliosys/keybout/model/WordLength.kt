package com.adeliosys.keybout.model

import com.google.gson.annotations.SerializedName

/**
 * The word length ranges.
 */
enum class WordLength(private val minLength: Int, private val maxLength: Int) {

    @SerializedName("shortest")
    SHORTEST(3, 5),
    @SerializedName("shorter")
    SHORTER(4, 6),
    @SerializedName("standard")
    STANDARD(5, 8),
    @SerializedName("longer")
    LONGER(6, 10),
    @SerializedName("longest")
    LONGEST(7, 12);

    companion object {
        fun getByCode(code: String) = try {
            valueOf(code.toUpperCase())
        } catch (e: Exception) {
            STANDARD
        }
    }

    fun getRange() = minLength..maxLength
}
