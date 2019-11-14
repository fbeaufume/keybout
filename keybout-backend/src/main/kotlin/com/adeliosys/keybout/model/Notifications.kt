package com.adeliosys.keybout.model

/**
 * Base class for the notifications.
 */
open class BaseNotification(val type: String)

/**
 * Sent when the user name is incorrect (empty for example).
 */
class IncorrectNameNotification : BaseNotification(Constants.NOTIFICATION_INCORRECT_NAME)

/**
 * Sent when the user name is too long.
 */
class TooLongNameNotification : BaseNotification(Constants.NOTIFICATION_TOO_LONG_NAME)

/**
 * Sent when the user name is already used.
 */
class UsedNameNotification : BaseNotification(Constants.NOTIFICATION_USED_NAME)

/**
 * Sent when the games list changed.
 */
class GamesListNotification(val games: Collection<GameDescriptor>) : BaseNotification(Constants.NOTIFICATION_GAMES_LIST)

/**
 * Sent when a game is starting, to display a countdown in the UI.
 */
class GameStartNotification(val gameType: String) : BaseNotification(Constants.NOTIFICATION_GAME_START)

/**
 * Sent when a game begins or the state of a word changes.
 */
class WordsListNotification(val words: Map<String, String>) : BaseNotification(Constants.NOTIFICATION_WORDS_LIST)

/**
 * Sent when a capture round ended. Contains the last words update and the scores.
 */
class CaptureScoresNotification(
        val words: Map<String, String>,
        val roundScores: List<CaptureScoreDto>,
        val gameScores: List<CaptureScoreDto>,
        val manager: String,
        val gameOver: Boolean) : BaseNotification(Constants.NOTIFICATION_SCORES)

/**
 * Sent when a race round ended. Contains the last words update and the scores.
 */
class RaceScoresNotification(
        val words: Map<String, String>,
        val roundScores: List<RaceScoreDto>,
        val gameScores: List<RaceScoreDto>,
        val manager: String,
        val gameOver: Boolean) : BaseNotification(Constants.NOTIFICATION_SCORES)

/**
 * Sent at the end of a round, when the manager changed.
 */
class ManagerNotification(val manager: String) : BaseNotification(Constants.NOTIFICATION_MANAGER)
