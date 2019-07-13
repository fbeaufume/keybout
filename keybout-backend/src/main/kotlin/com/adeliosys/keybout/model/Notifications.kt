package com.adeliosys.keybout.model

/**
 * Base class for the notifications.
 */
open class BaseNotification(val type:String)

/**
 * Sent when the user name is incorrect (empty for example).
 */
class IncorrectNameNotification: BaseNotification(Constants.NOTIFICATION_INCORRECT_NAME)

/**
 * Sent when the user name is too long.
 */
class TooLongNameNotification: BaseNotification(Constants.NOTIFICATION_TOO_LONG_NAME)

/**
 * Sent when the user name is already used.
 */
class UsedNameNotification: BaseNotification(Constants.NOTIFICATION_USED_NAME)

/**
 * Sent when the games list changed.
 */
class GamesListNotification(val games:Collection<GameDescriptor>): BaseNotification(Constants.NOTIFICATION_GAMES_LIST)

/**
 * Sent when a game is starting, to display a countdown in the UI.
 */
class GameStartNotification: BaseNotification(Constants.NOTIFICATION_GAME_START)

/**
 * Sent when a game is running.
 */
class GameRunNotification: BaseNotification(Constants.NOTIFICATION_GAME_RUN)
