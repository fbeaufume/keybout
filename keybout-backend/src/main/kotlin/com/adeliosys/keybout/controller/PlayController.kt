package com.adeliosys.keybout.controller

import com.adeliosys.keybout.model.*
import com.adeliosys.keybout.model.Constants.ACTION_CONNECT
import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
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
    private val users = ConcurrentHashMap.newKeySet<String>()

    /**
     * Declared games.
     */
    private val games = ConcurrentHashMap<Int, GameDescriptor>()

    private val gson = Gson()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("Opened connection '{}'", session.id)
        updateState(session, ClientState.UNIDENTIFIED)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.info("Received message '{}' for connection '{}'", message.payload, session.id)

        val action = Action(message.payload)
        if (action.command == ACTION_CONNECT) {
            val name = action.arguments.getOrElse(0) { "" }

            if (name.isEmpty()) {
                sendMessage(session, IncorrectNameNotification())
            } else {
                if (users.add(name)) {
                    session.attributes[NAME] = name
                    updateState(session, ClientState.IDENTIFIED)
                    sendMessage(session, GamesListNotification()) /// TODO FBE send real list
                } else {
                    sendMessage(session, UsedNameNotification())
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

    private fun updateState(session: WebSocketSession, state: ClientState) {
        logger.info("Changed state to '{}' for connection '{}'", state, session.id)
        session.attributes[STATE] = state
    }

    private fun sendMessage(session: WebSocketSession, obj: Any) {
        session.sendMessage(TextMessage(gson.toJson(obj)))
    }

    companion object {
        const val STATE = "state"
        const val NAME = "name"
    }
}
