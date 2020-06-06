package com.adeliosys.keybout.service

import com.adeliosys.keybout.api.PlayController
import com.adeliosys.keybout.model.Stats
import com.adeliosys.keybout.model.StatsDto
import com.adeliosys.keybout.repository.StatsRepository
import com.adeliosys.keybout.util.getUptimeSeconds
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.lang.Long.max
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class StatsService(
        private val playController: PlayController,
        private val playService: PlayService,
        private val statsRepository: StatsRepository?) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Value("\${application.data-type:dev}")
    private lateinit var dataType: String

    /**
     * Generated ID of the persisted stats document.
     */
    private var id: String? = null

    // TODO FBE move these 3 attributes to the Uptime class, with uptimeTotalInitialSeconds not being persisted
    private var uptimeMaxSeconds = 0L

    private var uptimeTotalInitialSeconds = 0L

    private var uptimeTotalSeconds = 0L

    private var startupCount = 1

    @PostConstruct
    private fun postConstruct() {
        logger.info("{} database persistence of stats and '{}' data type", if (statsRepository == null) "Without" else "With", dataType)

        if (statsRepository != null) {
            val stats = statsRepository.findByDataType(dataType)
            if (stats == null) {
                logger.info("Found no stats")
            } else {
                id = stats.id
                logger.info("Loaded the stats: {}", stats.describe())
                playController.usersCounter.initialize(stats.users)
                playService.declaredGamesCounter.initialize(stats.declaredGames)
                playService.runningGamesCounter.initialize(stats.runningGames)
                uptimeMaxSeconds = stats.uptime.maxSeconds
                uptimeTotalInitialSeconds = stats.uptime.totalSeconds
                startupCount += stats.startupCount
            }
        }
    }

    /**
     * Return the current stats DTO for the REST API.
     */
    fun getStats(): StatsDto {
        updateUptime()

        return StatsDto(
                playController.usersCounter,
                playService.declaredGamesCounter,
                playService.runningGamesCounter,
                getUptimeSeconds(),
                uptimeMaxSeconds,
                uptimeTotalSeconds)
    }

    /**
     * Save the stats to the database.
     */
    @Scheduled(initialDelay = 300000L, fixedRate = 300000L)
    fun saveStats() {
        updateUptime()

        statsRepository?.save(Stats(
                id,
                dataType,
                playController.usersCounter,
                playService.declaredGamesCounter,
                playService.runningGamesCounter,
                uptimeMaxSeconds,
                uptimeTotalSeconds,
                startupCount))
                ?.also {
                    id = it.id
                    logger.info("Saved the stats: {}", it.describe())
                }
    }

    /**
     * Update the uptime measures.
     */
    @Synchronized
    private fun updateUptime() {
        val uptimeSeconds = getUptimeSeconds()

        // Update the max uptime
        uptimeMaxSeconds = max(uptimeMaxSeconds, uptimeSeconds)

        // Update the total uptime
        uptimeTotalSeconds = uptimeTotalInitialSeconds + uptimeSeconds
    }

    @PreDestroy
    fun preDestroy() {
        logger.info("The application is shutting down")
        saveStats()
    }
}
