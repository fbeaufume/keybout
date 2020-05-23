package com.adeliosys.keybout.service

import com.adeliosys.keybout.api.PlayController
import com.adeliosys.keybout.model.Constants.STATS_ID
import com.adeliosys.keybout.model.Stats
import com.adeliosys.keybout.model.StatsDto
import com.adeliosys.keybout.repository.StatsRepository
import com.adeliosys.keybout.util.getUptimeString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
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

        // Load the stats from the DB
        statsRepository?.findById(STATS_ID)?.ifPresent { stats ->
            logger.info("Loaded stats: users = {} / {}, declaredGames = {} / {}, runningGames = {} / {}",
                    stats.users.maxCount,
                    stats.users.totalCount,
                    stats.declaredGames.maxCount,
                    stats.declaredGames.totalCount,
                    stats.runningGames.maxCount,
                    stats.runningGames.totalCount)
            playController.usersCounter.initialize(stats.users)
            playService.declaredGamesCounter.initialize(stats.declaredGames)
            playService.runningGamesCounter.initialize(stats.runningGames)
        }
    }

    fun getStats() = StatsDto(
            playController.usersCounter,
            playService.declaredGamesCounter,
            playService.runningGamesCounter,
            getUptimeString())

    // TODO FBE the save stats periodically

    // TODO FBE use a configuration parameter for the "type" (such as "dev" ou "prod")

    // TODO FBE use a different ID for the "prod" stats

    fun saveStats() {
        statsRepository?.save(Stats(
                STATS_ID,
                "dev",
                playController.usersCounter,
                playService.declaredGamesCounter,
                playService.runningGamesCounter))
    }
}
