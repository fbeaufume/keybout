package com.adeliosys.keybout.model

/**
 * A word or other type or expression (such as a calculus) typed by a user.
 */
class Word(val value: String, val display: String, var userName: String = "") {

    fun conflictsWith(otherValue: String): Boolean = value.startsWith(otherValue) || otherValue.startsWith(value)

    fun copy() = Word(value, display, userName)
}
