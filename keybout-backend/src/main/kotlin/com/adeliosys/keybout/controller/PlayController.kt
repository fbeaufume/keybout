package com.adeliosys.keybout.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class PlayController : TextWebSocketHandler() {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("Opened connection {}", session.id)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.info("For connection {}, received message '{}'", session.id, message)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.info("Closed connection {} with code {} and reason '{}'", session.id, status.code, status.reason)
    }
}
