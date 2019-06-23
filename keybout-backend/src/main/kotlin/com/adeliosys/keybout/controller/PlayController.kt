package com.adeliosys.keybout.controller

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

/**
 * Receive WebSocket messages and trigger their business processing
 * and update the client state.
 */
class PlayController : TextWebSocketHandler() {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Identified user names, used to check if a new user name is available
     */
    private val userNames = ConcurrentHashMap.newKeySet<String>()

    /**
     * Identified user sessions, to send them the declared games.
     */
    private val userSessions = ConcurrentHashMap<String, WebSocketSession>()

    /**
     * Declared games.
     */
    private val gameDescriptors = ConcurrentHashMap<Long, GameDescriptor>()

    private val gson = Gson()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("Opened connection '{}'", session.id)
        updateState(session, ClientState.UNIDENTIFIED)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.info("Received message '{}' for connection '{}'", message.payload, session.id)

        val action = Action(message.payload)

        when (getState(session)) {
            ClientState.UNIDENTIFIED -> {
                when (action.command) {
                    ACTION_CONNECT -> {
                        if (action.checkArgumentsCount(1)) {
                            val name = action.arguments[0]

                            if (name.isEmpty()) {
                                sendMessage(session, IncorrectNameNotification())
                            } else {
                                if (userNames.add(name)) {
                                    session.attributes[NAME] = name
                                    updateState(session, ClientState.IDENTIFIED)
                                    userSessions[name] = session

                                    sendMessage(session, GamesListNotification(gameDescriptors.values))
                                } else {
                                    sendMessage(session, UsedNameNotification())
                                }
                            }
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
                                    session.attributes[NAME] as String,
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

                            gameDescriptors[gameDescriptor.id] = gameDescriptor

                            sendMessage(GamesListNotification(gameDescriptors.values))
                        }
                    }
                    else -> {
                        invalidMessage(session, message)
                    }
                }
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.info("Closed connection '{}' with code {} and reason '{}'",
                session.id,
                status.code,
                status.reason.orEmpty())
    }

    private fun getState(session: WebSocketSession): ClientState {
        return session.attributes[STATE] as ClientState
    }

    private fun updateState(session: WebSocketSession, state: ClientState) {
        logger.info("Changed state to '{}' for connection '{}'", state, session.id)
        session.attributes[STATE] = state
    }

    private fun invalidMessage(session: WebSocketSession, message: TextMessage) {
        logger.warn("Invalid message '{}' for state {} and connection '{}'", message.payload, session.id, getState(session))
    }

    // Send a message to one client
    private fun sendMessage(session: WebSocketSession, obj: Any) {
        session.sendMessage(TextMessage(gson.toJson(obj)))
    }

    // Send a message to all clients
    private fun sendMessage(obj: Any) {
        userSessions.forEachValue(1) { s -> s.sendMessage(TextMessage(gson.toJson(obj))) }
    }

    companion object {
        const val STATE = "state"
        const val NAME = "name"
    }
}
