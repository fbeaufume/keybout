package com.adeliosys.keybout.service

import com.adeliosys.keybout.api.PlayController
import com.adeliosys.keybout.model.Constants.DATA_SAVE_PERIOD
import com.adeliosys.keybout.model.Constants.STARTUP_DATES_LENGTH
import com.adeliosys.keybout.model.StatsDocument
import com.adeliosys.keybout.model.StatsDto
import com.adeliosys.keybout.repository.StatsRepository
import com.adeliosys.keybout.util.getUptimeSeconds
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
) : DatabaseAwareService() {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * ID of the persistent stats document.
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

    /**
     * Load the previous stats from the database.
     */
    @PostConstruct
    private fun postConstruct() {
        logger.info(
            "Database persistence of stats is {} and environment name is '{}'",
            if (statsRepository == null) "disabled" else "enabled",
            environmentName
        )

        if (statsRepository != null) {
            try {
                var duration = -System.currentTimeMillis()
                val stats = statsRepository.findByEnvironmentName(environmentName)
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

                dataLoadSucceeded = true
            } catch (e: Exception) {
                logger.error("Failed to load the stats: {}", e.toString())
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
    @Scheduled(initialDelay = DATA_SAVE_PERIOD, fixedRate = DATA_SAVE_PERIOD)
    fun saveStats() {
        if (statsRepository != null && dataLoadSucceeded) {
            try {
                updateUptime()

                val timestamp = System.currentTimeMillis()
                statsRepository.save(
                    StatsDocument(
                        id,
                        environmentName,
                        playController.usersCounter,
                        playService.declaredGamesCounter,
                        playService.runningGamesCounter,
                        uptimeMaxSeconds,
                        uptimeTotalSeconds,
                        startupCount,
                        startupDates
                    )
                ).also {
                    id = it.id
                    logger.info("Saved the stats in {} msec: {}", System.currentTimeMillis() - timestamp, it.describe())
                }
            } catch (e: Exception) {
                logger.error("Failed to save the stats: {}", e.toString())
            }
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
