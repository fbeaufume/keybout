package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Constants.MAX_NAME_LENGTH
import com.adeliosys.keybout.model.Constants.MIN_NAME_LENGTH
import com.adeliosys.keybout.util.userName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

/**
 * Manage the users name unicity.
 */
@Service
class UserNameService {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Identified user names, used to check if a new user name is available.
     * An element is added when a user provides a valid and available name.
     * An element is removed when the connection is closed.
     * Concurrency is handled by the attribute.
     */
    private val userNames = ConcurrentHashMap.newKeySet<String>()

    /**
     * Register a user name.
     *
     * @return null on success or a notification if something failed.
     */
    fun registerUserName(userName: String): BaseNotification? {
        return when {
            // Check the minimum name length
            userName.length < MIN_NAME_LENGTH -> {
                TooShortNameNotification()
            }
            // Check the maximum name length
            userName.length > MAX_NAME_LENGTH -> {
                TooLongNameNotification()
            }
            // Check the name availability
            userNames.add(userName) -> {
                logger.info("User '{}' joined, user count is {}", userName, userNames.count())
                null
            }
            else -> {
                UsedNameNotification()
            }
        }
    }

    fun releaseUserName(session: WebSocketSession) {
        userNames.remove(session.userName)
        logger.info("User '{}' left, user count is {}", session.userName, userNames.count())
    }
}
