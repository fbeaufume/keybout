package com.adeliosys.keybout.model

/**
 * Base class for the notifications.
 */
open class BaseNotification(val type:String)

/**
 * Sent when the user name is already used.
 */
class UsedNameNotification: BaseNotification(Constants.NOTIFICATION_USED_NAME)

class GamesListNotification: BaseNotification(Constants.NOTIFICATION_GAMES_LIST)
