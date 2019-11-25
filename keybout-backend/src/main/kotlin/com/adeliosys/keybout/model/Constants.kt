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

    const val NOTIFICATION_TOO_SHORT_NAME = "too-short-name"
    const val NOTIFICATION_TOO_LONG_NAME = "too-long-name"
    const val NOTIFICATION_USED_NAME = "used-name"
    const val NOTIFICATION_GAMES_LIST = "games-list"
    const val NOTIFICATION_GAME_START = "game-start"
    const val NOTIFICATION_WORDS_LIST = "words-list"
    const val NOTIFICATION_SCORES = "scores"
    const val NOTIFICATION_MANAGER = "manager"

    // Word lengths

    const val LENGTH_SHORTEST = "shortest"
    const val LENGTH_SHORTER = "shorter"
    const val LENGTH_LONGER = "longer"
    const val LENGTH_LONGEST = "longest"

    // Misc

    /**
     * Minimum length of a player name.
     */
    const val MIN_NAME_LENGTH = 2

    /**
     * Maximum length of a player name.
     */
    const val MAX_NAME_LENGTH = 32

    /**
     * Minimum length of words loaded from the dictionary files.
     */
    const val MIN_WORD_LENGTH = 3

    /**
     * Maximum lenght of words loaded from the dictionary files.
     */
    const val MAX_WORD_LENGTH = 14

    /**
     * Name of a fictional player used to take available words when a race round expires.
     */
    const val RACE_PLAYER_NAME = "-"

    /**
     * GSON instance used by several classes.
     */
    val GSON = Gson()
}
