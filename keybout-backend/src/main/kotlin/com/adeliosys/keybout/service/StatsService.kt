package com.adeliosys.keybout.service

import com.adeliosys.keybout.api.PlayController
import com.adeliosys.keybout.model.Constants.STARTUP_DATES_LENGTH
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
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class StatsService(
    private val playController: PlayController,
    private val playService: PlayService,
    private val statsRepository: StatsRepository?
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Since the same database server is used for data of all application environments, this attribute is used
     * to differentiate them. It contains the environment name such as "dev" or "prod".
     */
    @Value("\${application.data-type:dev}")
    private lateinit var dataType: String

    /**
     * Generated ID of the persisted stats document.
     */
    private var id: String? = null

    private var uptimeMaxSeconds = 0L

    private var uptimeTotalInitialSeconds = 0L

    private var uptimeTotalSeconds = 0L

    private var startupCount = 1

    /**
     * The start date of this application instance (no need for the exact JVM start date, constructor date is good enough)
     */
    private val startupDate = Date()

    /**
     * The latest startup dates, from most recent to oldest.
     */
    private var startupDates = mutableListOf<Date>().apply { add(startupDate) }

    @PostConstruct
    private fun postConstruct() {
        logger.info(
            "{} database persistence of stats and '{}' data type",
            if (statsRepository == null) "Without" else "With",
            dataType
        )

        if (statsRepository != null) {
            var duration = -System.currentTimeMillis()
            val stats = statsRepository.findByDataType(dataType)
            duration += System.currentTimeMillis()

            if (stats == null) {
                logger.info("Found no stats in {} msec", duration)
            } else {
                logger.info("Loaded the stats in {} msec: {}", duration, stats.describe())

                id = stats.id
                playController.usersCounter.initialize(stats.users)
                playService.declaredGamesCounter.initialize(stats.declaredGames)
                playService.runningGamesCounter.initialize(stats.runningGames)
                uptimeMaxSeconds = stats.uptime.maxSeconds
                uptimeTotalInitialSeconds = stats.uptime.totalSeconds
                startupCount += stats.startupCount

                startupDates.addAll(stats.startupDates)
                if (startupDates.size > STARTUP_DATES_LENGTH) {
                    startupDates = startupDates.subList(0, STARTUP_DATES_LENGTH)
                }
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
            uptimeTotalSeconds,
            startupCount
        )
    }

    /**
     * Save the stats to the database.
     */
    @Scheduled(initialDelay = 300000L, fixedRate = 300000L)
    fun saveStats() {
        updateUptime()

        val timestamp = System.currentTimeMillis()
        statsRepository?.save(
            Stats(
                id,
                dataType,
                playController.usersCounter,
                playService.declaredGamesCounter,
                playService.runningGamesCounter,
                uptimeMaxSeconds,
                uptimeTotalSeconds,
                startupCount,
                startupDates
            )
        )?.also {
            id = it.id
            logger.info("Saved the stats in {} msec: {}", System.currentTimeMillis() - timestamp, it.describe())
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
        logger.info("The application is shutting down, saving the stats")
        saveStats()
    }
}
