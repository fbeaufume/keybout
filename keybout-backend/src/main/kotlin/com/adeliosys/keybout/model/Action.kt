package com.adeliosys.keybout.message

/**
 * An action message, i.e. a message received from a client.
 *
 * Action syntax: "command argument1 argument2 ..."
 */
class Action {

    var command: String = ""

    var arguments: List<String> = listOf()

    constructor(message: String) {
        val strings = message.trim().split(" ")

        if  (strings.isNotEmpty()) {
            command = strings[0]

            if (strings.size > 1) {
                arguments = strings.drop(1)
            }
        }
    }
}
