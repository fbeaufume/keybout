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
class GameStartNotification : BaseNotification(Constants.NOTIFICATION_GAME_START)

/**
 * Sent when a game is begins running or the state of a word changes.
 */
class WordsListNotification(val words: Map<String, String>) : BaseNotification(Constants.NOTIFICATION_WORDS_LIST)

/**
 * Sent when a round ended. Contains the last words update and the scores.
 */
class ScoresNotification(val words: Map<String, String>, val scores: List<ScoreDto>) : BaseNotification(Constants.NOTIFICATION_SCORES)
