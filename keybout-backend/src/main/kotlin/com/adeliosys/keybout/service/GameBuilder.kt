package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.GameDescriptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession

/**
 * Create instances of game service.
 */
@Service
class GameBuilder {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    fun buildGame(descriptor: GameDescriptor, players: MutableList<WebSocketSession>): GameService {
        return applicationContext.getBean(GameService::class.java).apply {
            initializeGame(descriptor, players)
            initializeRound()
        }
    }
}