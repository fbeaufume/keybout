package com.adeliosys.keybout.model

import com.adeliosys.keybout.util.Counter
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

// TODO FBE add a last update date
@Document(collection = "keybout_stats")
@TypeAlias("Stats")
class Stats(
        @Id var id: String,
        var type: String,
        var users: StatsItem,
        var declaredGames: StatsItem,
        var runningGames: StatsItem) {
    constructor() : this("", "", StatsItem(), StatsItem(), StatsItem())

    constructor(
            id: String,
            type: String,
            usersCounter: Counter,
            declaredGamesCounter: Counter,
            runningGamesCounter: Counter) :
            this(id, type, StatsItem(usersCounter), StatsItem(declaredGamesCounter), StatsItem(runningGamesCounter))
}

class StatsItem(val maxCount: Int, val totalCount: Int) {
    constructor() : this(0, 0)

    constructor(counter: Counter) : this(counter.getMax(), counter.getTotal())
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
