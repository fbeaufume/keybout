package com.adeliosys.keybout.model

import com.adeliosys.keybout.util.Counter
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "keybout_stats")
class Stats(@Id var id: String, var name: String, var users: StatsItem, var declaredGames: StatsItem, var runningGames: StatsItem) {
    constructor() : this("", "", StatsItem(), StatsItem(), StatsItem())
}

class StatsItem(val currentCount: Int, val maxCount: Int, val totalCount: Int) {
    constructor() : this(0, 0, 0)
}

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
