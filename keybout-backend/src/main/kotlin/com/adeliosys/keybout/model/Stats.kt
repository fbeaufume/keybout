package com.adeliosys.keybout.model

import com.adeliosys.keybout.util.Counter

//@Document(collection = "keybout_stats")
//class Stats (@Id val id:String?, val name:String)

/**
 * DTO used by the REST API.
 */
class StatsDto(usersCounter: Counter, declaredGamesCounter: Counter, runningGamesCounter: Counter, val uptime: String) {
    val users = StatsItemDto(usersCounter)
    val declaredGames = StatsItemDto(declaredGamesCounter)
    val runningGames = StatsItemDto(runningGamesCounter)
}

class StatsItemDto(counter: Counter) {
    val currentCount = counter.getCurrent()
    val maxCount = counter.getMax()
    val totalCount = counter.getTotal()
}
