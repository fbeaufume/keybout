package com.adeliosys.keybout.model

import com.google.gson.annotations.SerializedName

/**
 * Supported languages.
 */
enum class Language(val code: String) {

    @SerializedName("en")
    EN("en"),
    @SerializedName("fr")
    FR("fr");

    companion object {
        fun getByCode(code: String) = try {
            valueOf(code.toUpperCase())
        } catch (e: Exception) {
            EN
        }
    }
}