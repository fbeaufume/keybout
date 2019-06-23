package com.adeliosys.keybout.model

/**
 * An action message, i.e. a message received from a client.
 *
 * Action syntax: "command argument1 argument2 ..."
 */
class Action(message: String) {

    var command: String = ""

    var arguments: List<String> = listOf()

    init {
        val strings = message.trim().split(" ")
        if (strings.isNotEmpty()) {
            command = strings[0]

            if (strings.size > 1) {
                arguments = strings.drop(1)
            }
        }
    }

    fun checkArgumentsCount(count: Int) = arguments.size == count
}
