package com.adeliosys.keybout.model

class Word(label: String, style: GameStyle) {
    var userName = ""
    var display = style.transform(label)
}
