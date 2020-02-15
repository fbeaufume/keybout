package com.adeliosys.keybout.model

class Word(val label: String, val display: String) {

    var userName = ""

    fun conflictsWith(otherLabel: String): Boolean = label.startsWith(otherLabel) || otherLabel.startsWith(label)
}
