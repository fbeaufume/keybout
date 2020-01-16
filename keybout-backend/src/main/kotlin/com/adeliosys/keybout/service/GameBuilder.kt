package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.GameDescriptor
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession

/**
 * Create instances of game service.
 */
@Service
class GameBuilder(private val applicationContext: ApplicationContext) {

    fun buildGame(descriptor: GameDescriptor, players: MutableList<WebSocketSession>): BaseGameService {
        val type:Class<out BaseGameService> = if (descriptor.mode == "race") RaceGameService::class.java else CaptureGameService::class.java

        return applicationContext.getBean(type).apply {
            initializeGame(descriptor, players)
            startCountdown()
        }
    }
}
