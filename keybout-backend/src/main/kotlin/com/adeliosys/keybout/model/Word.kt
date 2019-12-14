package com.adeliosys.keybout.model

class Word(val label: String, wordEffect: WordEffect) {
    var userName = ""
    var display = wordEffect.transform(label)
}
