package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Constants.ACTION_CLAIM_WORD
import com.adeliosys.keybout.model.Constants.ACTION_CONNECT
import com.adeliosys.keybout.model.Constants.ACTION_CREATE_GAME
import com.adeliosys.keybout.model.Constants.ACTION_DELETE_GAME
import com.adeliosys.keybout.model.Constants.ACTION_JOIN_GAME
import com.adeliosys.keybout.model.Constants.ACTION_LEAVE_GAME
import com.adeliosys.keybout.model.Constants.ACTION_QUIT_GAME
import com.adeliosys.keybout.model.Constants.ACTION_START_GAME
import com.adeliosys.keybout.model.Constants.ACTION_START_ROUND
import com.adeliosys.keybout.model.Constants.MAX_NAME_LENGTH
import com.adeliosys.keybout.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

/**
 * Receive WebSocket messages, connects and disconnects,
 * then delegate the business processing to [PlayService].
 *
 * Also manage the validation of the user name.
 */
@Service
class PlayController : TextWebSocketHandler() {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Identified user names, used to check if a new user name is available.
     * An element is added when a user provides a valid and available name.
     * An element is removed when the connection is closed.
     * Concurrency is handled by the attribute.
     */
    private val userNames = ConcurrentHashMap.newKeySet<String>()

    @Value("\${keybout.latency:0}")
    private var latency = 0L

    @Autowired
    private lateinit var service: PlayService

    @PostConstruct
    private fun init() {
        logger.info("Using latency of {} ms", latency)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        session.userName = ""
        session.setState(ClientState.OPENED, 0, logger)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            if (latency > 0) {
                Thread.sleep(latency)
            }

            logger.trace("Received message '{}' for {}", message.payload, session.description)

            val action = Action(message.payload)

            when (session.state) {
                ClientState.OPENED -> {
                    when (action.command) {
                        ACTION_CONNECT -> {
                            if (action.checkMinimumArgumentsCount(1)) {
                                val name = action.rawArguments

                                when {
                                    // Check the name length
                                    name.length > MAX_NAME_LENGTH -> {
                                        session.sendObjectMessage(TooLongNameNotification(), logger)
                                    }
                                    // Check the name availability
                                    userNames.add(name) -> {
                                        session.userName = name
                                        service.goToLobby(session)

                                        logger.info("User '{}' joined, user count is {}", name, userNames.count())
                                    }
                                    else ->
                                        session.sendObjectMessage(UsedNameNotification(), logger)
                                }
                            } else {
                                session.sendObjectMessage(IncorrectNameNotification(), logger)
                            }
                        }
                        else -> logInvalidMessage(session, message)
                    }
                }
                ClientState.LOBBY -> {
                    when (action.command) {
                        ACTION_CREATE_GAME -> {
                            if (action.checkArgumentsCount(5)) {
                                val gameDescriptor = GameDescriptor(
                                        session.userName,
                                        action.arguments[0],
                                        try {
                                            action.arguments[1].toInt()
                                        } catch (e: NumberFormatException) {
                                            1
                                        },
                                        action.arguments[2],
                                        try {
                                            action.arguments[3].toInt()
                                        } catch (e: NumberFormatException) {
                                            10
                                        },
                                        action.arguments[4])

                                service.createGame(session, gameDescriptor)
                            }
                        }
                        ACTION_JOIN_GAME -> {
                            if (action.checkArgumentsCount(1)) {
                                try {
                                    service.joinGame(session, action.arguments[0].toLong())
                                } catch (e: NumberFormatException) {
                                    logger.warn("Invalid game in message '{}' for {}", message, session.description)
                                }
                            }
                        }
                        else -> logInvalidMessage(session, message)
                    }
                }
                ClientState.CREATED -> {
                    when (action.command) {
                        ACTION_DELETE_GAME -> service.deleteGame(session)
                        ACTION_START_GAME -> service.startGame(session)
                        else -> logInvalidMessage(session, message)
                    }
                }
                ClientState.JOINED -> {
                    when (action.command) {
                        ACTION_LEAVE_GAME -> service.leaveGame(session)
                        else -> logInvalidMessage(session, message)
                    }
                }
                ClientState.PLAYING -> {
                    when (action.command) {
                        ACTION_CLAIM_WORD -> {
                            if (action.checkArgumentsCount(1)) {
                                service.claimWord(session, action.arguments[0])
                            }
                        }
                        ACTION_START_ROUND -> service.startRound(session)
                        ACTION_QUIT_GAME -> service.goToLobby(session)
                        else -> logInvalidMessage(session, message)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Caught an exception during message processing", e)
        }
    }

    private fun logInvalidMessage(session: WebSocketSession, message: TextMessage) {
        logger.warn("Invalid message '{}' for state {} of {}", message.payload, session.state, session.description)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        service.disconnect(session)

        userNames.remove(session.userName)

        logger.debug("Closed {} with code {} and reason '{}'",
                session.description,
                status.code,
                status.reason.orEmpty())

        logger.info("User '{}' left, user count is {}", session.userName, userNames.count())
    }
}
