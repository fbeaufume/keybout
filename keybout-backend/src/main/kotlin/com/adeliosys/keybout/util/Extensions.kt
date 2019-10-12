package com.adeliosys.keybout.util

import com.adeliosys.keybout.model.ClientState
import org.slf4j.Logger
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.sockjs.transport.session.PollingSockJsSession
import org.springframework.web.socket.sockjs.transport.session.StreamingSockJsSession
import org.springframework.web.socket.sockjs.transport.session.WebSocketServerSockJsSession

// WebSocketSession extensions

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

fun WebSocketSession.setState(logger: Logger, state: ClientState, gameId: Long, userCount: Int = 0) {
    attributes[STATE] = state
    attributes[GAME_ID] = gameId
    logger.debug("Changed state to '{}' and game #{} for {}{}{}",
            state,
            gameId,
            description,
            if (state == ClientState.OPENED) " with '${getTransportName()}' transport" else "",
            if (userCount > 0) ", user count is $userCount" else "")
}

val WebSocketSession.gameId: Long
    get() = attributes[GAME_ID] as Long

fun WebSocketSession.getTransportName(): String = when (this) {
    is WebSocketServerSockJsSession -> "WebSocket"
    is StreamingSockJsSession -> "Streaming"
    is PollingSockJsSession -> "Polling"
    else -> "unknown"
}
