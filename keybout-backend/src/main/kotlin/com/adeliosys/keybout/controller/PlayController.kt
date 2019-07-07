package com.adeliosys.keybout.controller

import com.adeliosys.keybout.controller.PlayController.Companion.NAME
import com.adeliosys.keybout.controller.PlayController.Companion.STATE
import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Constants.ACTION_CONNECT
import com.adeliosys.keybout.model.Constants.ACTION_CREATE_GAME
import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.lang.NumberFormatException
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

/**
 * Receive WebSocket messages and trigger their business processing
 * and update the client state.
 */
class PlayController : TextWebSocketHandler() {

    companion object {
        const val STATE = "state"
        const val NAME = "name"
    }

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Identified user names, used to check if a new user name is available.
     * An element is added when a user provides an valid and available name.
     * An element is removed when the connection is closed.
     */
    private val userNames = ConcurrentHashMap.newKeySet<String>()

    /**
     * All WebSocket sessions. The key is the session ID.
     * An element is added when the connection is established.
     * An element is removed when the connection is closed.
     */
    //private val sessions = ConcurrentHashMap<String, WebSocketSession>() // TODO FBE delete this

    /**
     * WebSocket sessions of users getting/creating/joining/etc games.
     * Concurrency is handled by synchronizing on this class instance.
     */
    private val gamesSessions = mutableListOf<WebSocketSession>()

    /**
     * Declared games. The key is the game ID.
     * Concurrency is handled by synchronizing on this class instance.
     */
    private val gameDescriptors = mutableMapOf<Long, GameDescriptor>()

    private val gson = Gson()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("Opened connection '{}'", session.id)
        session.setState(ClientState.UNIDENTIFIED, logger)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            logger.debug("Received message '{}' for connection '{}'", message.payload, session.id)

            val action = Action(message.payload)

            when (session.getState()) {
                ClientState.UNIDENTIFIED -> {
                    when (action.command) {
                        ACTION_CONNECT -> {
                            if (action.checkArgumentsCount(1)) {
                                val name = action.arguments[0]

                                when {
                                    // Check the name length
                                    name.length >= 32 -> {
                                        sendMessage(session, TooLongNameNotification())
                                    }
                                    // Check the name availability
                                    userNames.add(name) -> {
                                        logger.debug("Using name '{}' for connection '{}', user count is {}", name, session.id, userNames.count())

                                        session.setUserName(name)
                                        session.setState(ClientState.IDENTIFIED, logger)

                                        synchronized(this) {
                                            gamesSessions.add(session)
                                            sendMessage(session, GamesListNotification(gameDescriptors.values))
                                        }
                                    }
                                    else -> {
                                        sendMessage(session, UsedNameNotification())
                                    }
                                }
                            } else {
                                sendMessage(session, IncorrectNameNotification())
                            }
                        }
                        else -> {
                            invalidMessage(session, message)
                        }
                    }
                }
                ClientState.IDENTIFIED -> {
                    when (action.command) {
                        ACTION_CREATE_GAME -> {
                            if (action.checkArgumentsCount(4)) {
                                val gameDescriptor = GameDescriptor(
                                        session.getUserName(),
                                        action.arguments[0],
                                        try {
                                            action.arguments[1].toInt()
                                        } catch (e: NumberFormatException) {
                                            1
                                        },
                                        try {
                                            action.arguments[2].toInt()
                                        } catch (e: NumberFormatException) {
                                            10
                                        },
                                        action.arguments[3])

                                synchronized(this) {
                                    gameDescriptors[gameDescriptor.id] = gameDescriptor
                                    sendMessage(GamesListNotification(gameDescriptors.values))
                                }
                            }
                        }
                        else -> {
                            invalidMessage(session, message)
                        }
                    }
                }
                else -> {
                    // TODO FBE
                }
            }
        } catch (e: Exception) {
            logger.error("Caught an exception during message processing", e)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        if (session.getState() > ClientState.UNIDENTIFIED) {
            // If the user was in a created/joined/etc games, notify the other users
            var notify = false

            synchronized(this) {
                // Remove games created by the user (should be at most one)
                val name = session.getUserName()
                val ids = gameDescriptors.values.stream().filter { g -> name.equals(g.creator) }.map { g -> g.id }.collect(Collectors.toList())
                if (!ids.isEmpty()) {
                    notify = true
                }
                ids.forEach { i -> gameDescriptors.remove(i) }

                // Remove user from joined games (should be at most one)
                gameDescriptors.forEach {
                    if (it.value.players.remove(name)) {
                        notify = true
                    }
                }

                if (notify) {
                    sendMessage(GamesListNotification(gameDescriptors.values))
                }
            }
        }

        userNames.remove(session.getUserName())

        logger.debug("Closed connection '{}' with code {} and reason '{}', user count is {}",
                session.id,
                status.code,
                status.reason.orEmpty(),
                userNames.count())
    }

    private fun invalidMessage(session: WebSocketSession, message: TextMessage) {
        logger.warn("Invalid message '{}' for state {} and connection '{}'", message.payload, session.id, session.getState())
    }

    // Send a message to all clients
    private fun sendMessage(obj: Any) {
        val msg = gson.toJson(obj)
        gamesSessions.forEach { s -> sendStringMessage(s, msg) }
    }

    // Send a message to one client
    private fun sendMessage(session: WebSocketSession, obj: Any) {
        sendStringMessage(session, gson.toJson(obj))
    }

    private fun sendStringMessage(session: WebSocketSession, msg: String) {
        try {
            session.sendMessage(TextMessage(msg))
        } catch (e: Exception) {
            logger.error("Failed to send message to connection '{}': {}", session.id, e.toString())
        }
    }
}

// Several extensions to WebSocketSession

fun WebSocketSession.getUserName(): String = attributes[NAME] as String

fun WebSocketSession.setUserName(name: String) {
    attributes[NAME] = name
}

fun WebSocketSession.getState(): ClientState = attributes[STATE] as ClientState

fun WebSocketSession.setState(state: ClientState, logger: Logger) {
    attributes[STATE] = state
    logger.debug("Changed state to '{}' for connection '{}'", state, id)
}
