package com.adeliosys.keybout.service

import com.adeliosys.keybout.api.PlayController
import com.adeliosys.keybout.model.StatsDto
import com.adeliosys.keybout.repository.StatsRepository
import com.adeliosys.keybout.util.getUptimeString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class StatsService(
        private val playController: PlayController,
        private val playService: PlayService,
        private val statsRepository: StatsRepository?) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    private fun postConstruct() {
        logger.info("{} database persistence of stats", if (statsRepository == null) "Without" else "With")

        // TODO FBE get the initial stats from DB and update the counters
    }

    fun getStats() = StatsDto(
            playController.usersCounter,
            playService.declaredGamesCounter,
            playService.runningGamesCounter,
            getUptimeString())

    // TODO FBE save stats periodically
}
