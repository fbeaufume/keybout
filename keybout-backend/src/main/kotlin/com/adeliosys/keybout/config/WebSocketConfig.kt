package com.adeliosys.keybout.config

import com.adeliosys.keybout.api.PlayController
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(private val playController: PlayController) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(playController, "/api/websocket")
            .setAllowedOrigins("http://localhost:4200","http://localhost:8080", "https://keybout.herokuapp.com")
            .withSockJS()
    }
}
