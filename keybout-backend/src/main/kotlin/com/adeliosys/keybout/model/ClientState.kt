package com.adeliosys.keybout.model

/**
 * Server side view of the state of a client connection.
 */
enum class ClientState {
    /**
     * The connection was successfully opened.
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
     * A game round is about to start.
     */
    STARTING,
    /**
     * A game round is in progress.
     */
    PLAYING,
    /**
     * A game round has ended.
     */
    END_ROUND,
    /**
     * A game has ended.
     */
    END_GAME
}