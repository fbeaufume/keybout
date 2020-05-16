package com.adeliosys.keybout.service

import com.adeliosys.keybout.api.PlayController
import com.adeliosys.keybout.model.StatsDto
import com.adeliosys.keybout.util.getUptimeString
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class StatsService(
        private val playController: PlayController,
        private val playService: PlayService) {


    @PostConstruct
    private fun postConstruct() {
        // TODO FBE get the initial stats from DB and update the counters
    }

    fun getStats() = StatsDto(
            playController.usersCounter,
            playService.declaredGamesCounter,
            playService.runningGamesCounter,
            getUptimeString())

    // TODO FBE save stats periodically
}
