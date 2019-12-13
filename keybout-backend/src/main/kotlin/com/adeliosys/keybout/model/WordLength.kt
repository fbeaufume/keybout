package com.adeliosys.keybout.model

enum class WordLength(private val minLength: Int, private val maxLength: Int) {

    SHORTEST(3, 6),
    SHORTER(4, 8),
    STANDARD(5, 10),
    LONGER(6, 12),
    LONGEST(7, 14);

    companion object {
        fun getByCode(code: String) = try {
            valueOf(code.toUpperCase())
        } catch (e: Exception) {
            STANDARD
        }
    }

    fun getRange() = minLength..maxLength
}
