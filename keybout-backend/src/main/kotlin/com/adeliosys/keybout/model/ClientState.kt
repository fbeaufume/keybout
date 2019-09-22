package com.adeliosys.keybout.model

/**
 * Server side view of the state of a client connection.
 */
enum class ClientState {
    /**
     * The connection was successfully opened and the user is not yet identified.
     */
    OPENED,
    /**
     * The user has a name.
     */
    IDENTIFIED,
    /**
     * The user created a game.
     */
    CREATED,
    /**
     * The user joined a game.
     */
    JOINED,
    /**
     * A game is in progress (starting or running or displaying result).
     */
    PLAYING
}
