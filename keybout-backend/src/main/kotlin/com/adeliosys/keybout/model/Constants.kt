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

    // Awards

    const val FIRST_AWARD = 1 // Supported by all game modes
    const val LONGEST_AWARD = 2 // Supported by all game modes
    const val LAST_AWARD = 4 // Supported by capture mode only

    // Misc

    /**
     * Minimum length of a player name.
     */
    const val MIN_NAME_LENGTH = 2

    /**
     * Maximum length of a player name.
     */
    const val MAX_NAME_LENGTH = 16

    /**
     * Minimum length of words loaded from the dictionary files.
     */
    const val MIN_WORD_LENGTH = 3

    /**
     * Maximum length of words loaded from the dictionary files.
     */
    const val MAX_WORD_LENGTH = 9

    /**
     * Maximum number of attempts when generating words.
     */
    const val MAX_GENERATOR_ATTEMPTS = 500

    /**
     * Name of a fictional player used to take available words when a game round expires.
     */
    const val FICTIONAL_PLAYER_NAME = "-"

    /**
     * Maximum number of scores.
     */
    const val SCORES_LENGTH = 10

    /**
     * Id of the persistent stats entity.
     */
    const val STATS_ID = "5ec9104a248e1b74a55cb6b3"

    /**
     * GSON instance used by several classes.
     */
    val GSON = Gson()
}
