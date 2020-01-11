package com.adeliosys.keybout.api

import com.adeliosys.keybout.service.PlayService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.management.ManagementFactory

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

    private fun getUptimeString(): String {
        val uptimeInSec = ManagementFactory.getRuntimeMXBean().uptime / 1000
        return uptimeInSec.let {
            when {
                it > 86400 -> "${it / 86400}d ${(it / 3600).rem(24)}h ${(it / 60).rem(60)}m ${it.rem(60)}s"
                it > 3600 -> "${(it / 3600).rem(24)}h ${(it / 60).rem(60)}m ${it.rem(60)}s"
                it > 60 -> "${(it / 60).rem(60)}m ${it.rem(60)}s"
                else -> "${it.rem(60)}s"
            }
        }
    }
}
