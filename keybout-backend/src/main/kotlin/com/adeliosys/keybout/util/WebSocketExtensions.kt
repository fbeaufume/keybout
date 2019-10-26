package com.adeliosys.keybout.util

import com.adeliosys.keybout.model.ClientState
import com.adeliosys.keybout.model.Constants.GSON
import org.slf4j.Logger
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.sockjs.transport.session.PollingSockJsSession
import org.springframework.web.socket.sockjs.transport.session.StreamingSockJsSession
import org.springframework.web.socket.sockjs.transport.session.WebSocketServerSockJsSession

// WebSocketSession extensions :
// - Business attributes: server side state, user name, game ID (0 means no actual game)
// - Sender methods

const val STATE = "state"
const val NAME = "name"
const val GAME_ID = "GAME_ID"

val WebSocketSession.description: String
    get() = "'$id' ('$userName')"

var WebSocketSession.userName: String
    get() = attributes[NAME] as String
    set(value) {
        attributes[NAME] = value
    }

val WebSocketSession.state: ClientState
    get() = attributes[STATE] as ClientState

fun WebSocketSession.setState(state: ClientState, gameId: Long, logger: Logger) {
    attributes[STATE] = state
    attributes[GAME_ID] = gameId
    logger.debug("Changed state to '{}' and game #{} for {}{}{}",
            state,
            gameId,
            description,
            if (state == ClientState.OPENED) " with '${getTransportName()}' transport" else "")
}

val WebSocketSession.gameId: Long
    get() = attributes[GAME_ID] as Long

fun WebSocketSession.getTransportName(): String = when (this) {
    is WebSocketServerSockJsSession -> "WebSocket"
    is StreamingSockJsSession -> "Streaming"
    is PollingSockJsSession -> "Polling"
    else -> "unknown"
}

fun WebSocketSession.sendObjectMessage(obj: Any, logger: Logger) {
    sendStringMessage(GSON.toJson(obj), logger)
}

fun WebSocketSession.sendStringMessage(msg: String, logger: Logger) {
    try {
        sendMessage(TextMessage(msg))
    } catch (e: Exception) {
        val shortenedMsg = if (msg.length > 40) msg.substring(0, 40) + "..." else msg
        logger.error("Failed to send message '{}' to {}: {}", shortenedMsg, description, e.toString())
    }
}
