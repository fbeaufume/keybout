package com.adeliosys.keybout.api

import com.adeliosys.keybout.service.PlayService
import com.adeliosys.keybout.util.getUptimeString
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class StatsController(
        private val playController: PlayController,
        private val playService: PlayService) {

    @GetMapping("/stats")
    fun getStats(): Map<String, Any> = mapOf(
            "users" to mapOf(
                    "current-count" to playController.usersCounter.getCurrent(),
                    "max-count" to playController.usersCounter.getMax(),
                    "total-count" to playController.usersCounter.getTotal()),
            "declared-games" to mapOf(
                    "current-count" to playService.declaredGamesCounter.getCurrent(),
                    "max-count" to playService.declaredGamesCounter.getMax(),
                    "total-count" to playService.declaredGamesCounter.getTotal()),
            "running-games" to mapOf(
                    "current-count" to playService.runningGamesCounter.getCurrent(),
                    "max-count" to playService.runningGamesCounter.getMax(),
                    "total-count" to playService.runningGamesCounter.getTotal()),
            "uptime" to getUptimeString())
}
