package com.adeliosys.keybout.model

import com.google.gson.annotations.SerializedName

/**
 * Supported languages.
 */
enum class Language(val code: String) {

    @SerializedName("en")
    EN("en"),
    @SerializedName("fr")
    FR("fr"),
    @SerializedName("none")
    NONE("none");

    companion object {
        fun realLanguages() = listOf(EN, FR)

        fun getByCode(code: String, style: GameStyle): Language {
            var language = try {
                valueOf(code.uppercase())
            } catch (e: Exception) {
                default
            }

            if (style == GameStyle.CALCULUS) {
                // Calculus style does not use any language
                language = NONE
            }
            else if (language == NONE) {
                // Other styles require a real language
                language = default
            }

            return language
        }

        private val default = EN;
    }
}