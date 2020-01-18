package com.adeliosys.keybout.model

/**
 * Base class for the notifications.
 */
open class BaseNotification(val type: String)

/**
 * Sent when the user name is too short.
 */
class TooShortNameNotification : BaseNotification(Constants.NOTIFICATION_TOO_SHORT_NAME)

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
 * Sent when a round is starting, to display a countdown in the UI.
 */
class GameStartNotification : BaseNotification(Constants.NOTIFICATION_GAME_START)

/**
 * Sent when a round begins or the state of a word changes.
 */
class WordsListNotification(
        // The map key is word label and the map value is an array where the first element is the name of user
        // who caught the word and the second element is the display of the word (i.e. with the effect)
        val words: Map<String, Array<String>>) : BaseNotification(Constants.NOTIFICATION_WORDS_LIST)

/**
 * Sent when a round ended. Contains the last words update and the scores.
 */
class ScoresNotification(
        // Same as words attribute in preceding notification
        val words: Map<String, Array<String>>,
        val roundScores: List<ScoreDto>,
        val gameScores: List<ScoreDto>,
        val manager: String,
        val gameOver: Boolean) : BaseNotification(Constants.NOTIFICATION_SCORES)

/**
 * Sent at the end of a round, when the manager changed.
 */
class ManagerNotification(val manager: String) : BaseNotification(Constants.NOTIFICATION_MANAGER)
