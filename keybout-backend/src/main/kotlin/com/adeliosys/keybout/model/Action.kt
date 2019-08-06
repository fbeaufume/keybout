package com.adeliosys.keybout.model

/**
 * An action message, i.e. a message received from a client.
 *
 * Action syntax: "command argument1 argument2 ..."
 */
class Action(message: String) {

    var command: String = ""

    var rawArguments: String = ""

    var arguments: List<String> = listOf()

    init {
        val strings = message.trim().split(" ", limit = 2)
        if (strings.isNotEmpty()) {
            command = strings[0]

            if (strings.size > 1) {
                rawArguments = strings[1]

                arguments = rawArguments.split(" ")
            }
        }
    }

    fun checkArgumentsCount(count: Int) = arguments.size == count

    fun checkMinimumArgumentsCount(count: Int) = arguments.size >= count
}
