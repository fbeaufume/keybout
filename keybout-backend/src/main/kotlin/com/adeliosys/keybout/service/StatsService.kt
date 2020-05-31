package com.adeliosys.keybout.service

import com.adeliosys.keybout.api.PlayController
import com.adeliosys.keybout.model.Stats
import com.adeliosys.keybout.model.StatsDto
import com.adeliosys.keybout.repository.StatsRepository
import com.adeliosys.keybout.util.getUptimeString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
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

    @PostConstruct
    private fun postConstruct() {
        logger.info("{} database persistence of stats and '{}' data type", if (statsRepository == null) "Without" else "With", dataType)
        loadStats()
    }

    /**
     * Load the stats from the DB or generate blank stats.
     */
    fun loadStats() {
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
            }
        }
    }

    /**
     * Return the current stats DTO for the REST API.
     */
    fun getStats() = StatsDto(
            playController.usersCounter,
            playService.declaredGamesCounter,
            playService.runningGamesCounter,
            getUptimeString())

    /**
     * Save the stats to the database.
     */
    @Scheduled(initialDelay = 300000L, fixedRate = 300000L)
    fun saveStats() {
        statsRepository?.save(Stats(
                id,
                dataType,
                playController.usersCounter,
                playService.declaredGamesCounter,
                playService.runningGamesCounter))
                ?.also {
                    id = it.id
                    logger.info("Saved the stats: {}", it.describe())
                }
    }

    @PreDestroy
    fun preDestroy() {
        logger.info("The application is shutting down")
        saveStats()
    }
}
