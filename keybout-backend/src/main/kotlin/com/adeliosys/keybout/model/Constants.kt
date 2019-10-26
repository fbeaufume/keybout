package com.adeliosys.keybout.model

import com.google.gson.Gson

/**
 * Application constants.
 */
object Constants {

    // Action commands

    const val ACTION_CONNECT = "connect"
    const val ACTION_CREATE_GAME = "create-game"
    const val ACTION_DELETE_GAME = "delete-game"
    const val ACTION_JOIN_GAME = "join-game"
    const val ACTION_LEAVE_GAME = "leave-game"
    const val ACTION_START_GAME = "start-game"
    const val ACTION_CLAIM_WORD = "claim-word"
    const val ACTION_START_ROUND = "start-round"
    const val ACTION_QUIT_GAME = "quit-game"

    // Notification types

    const val NOTIFICATION_INCORRECT_NAME = "incorrect-name"
    const val NOTIFICATION_TOO_LONG_NAME = "too-long-name"
    const val NOTIFICATION_USED_NAME = "used-name"
    const val NOTIFICATION_GAMES_LIST = "games-list"
    const val NOTIFICATION_GAME_START = "game-start"
    const val NOTIFICATION_WORDS_LIST = "words-list"
    const val NOTIFICATION_SCORES = "scores"
    const val NOTIFICATION_MANAGER = "manager"

    // Misc

    /**
     * Maximum length of a player name.
     */
    const val MAX_NAME_LENGTH = 32

    /**
     * Minimum length of words during games.
     */
    const val MIN_WORD_LENGTH = 5

    /**
     * Maximum lenght of words during games.
     */
    const val MAX_WORD_LENGTH = 12

    /**
     * Minimum amount of loaded words allowed, to prevent incorrect values of minimum and maximum word length.
     */
    const val MIN_WORD_COUNT = 200

    /**
     * Not technically a constant, single GSON instance used by several classes.
     */
    val GSON = Gson()
}
