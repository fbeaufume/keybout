package com.adeliosys.keybout.model

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

    // Notification types

    const val NOTIFICATION_INCORRECT_NAME = "incorrect-name"
    const val NOTIFICATION_TOO_LONG_NAME = "too-long-name"
    const val NOTIFICATION_USED_NAME = "used-name"
    const val NOTIFICATION_GAMES_LIST = "games-list"
    const val NOTIFICATION_GAME_START = "game-start"
    const val NOTIFICATION_WORDS_LIST = "words-list"
    const val NOTIFICATION_SCORES = "scores"
}
