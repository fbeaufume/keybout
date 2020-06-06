package com.adeliosys.keybout.model

import com.adeliosys.keybout.util.Counter
import com.adeliosys.keybout.util.getUptimeString
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

// TODO FBE add creation date
@Document(collection = "keybout_stats")
@TypeAlias("Stats")
class Stats(
        @Id var id: String? = null,
        var dataType: String = "",
        var users: Measure = Measure(),
        var declaredGames: Measure = Measure(),
        var runningGames: Measure = Measure(),
        var uptime: Uptime = Uptime(),
        var startupCount: Int = 0,
        val lastUpdate: Date = Date()) {
    constructor(
            id: String?,
            dataType: String,
            usersCounter: Counter,
            declaredGamesCounter: Counter,
            runningGamesCounter: Counter,
            maxSeconds: Long,
            totalSeconds: Long,
            startupCount: Int) :
            this(id, dataType, Measure(usersCounter), Measure(declaredGamesCounter), Measure(runningGamesCounter), Uptime(maxSeconds, totalSeconds), startupCount, Date())

    fun describe() = "users=${users.max}/${users.total}, declared=${declaredGames.max}/${declaredGames.total}, running=${runningGames.max}/${runningGames.total}, uptime=${uptime.maxSeconds}/${uptime.totalSeconds}, startups=${startupCount}"
}

class Measure(var max: Int = 0, var total: Int = 0) {
    constructor(counter: Counter) : this(counter.getMax(), counter.getTotal())
}

class Uptime(val maxSeconds: Long = 0, val totalSeconds: Long = 0) {
    var max: String = ""
    var total: String = ""

    init {
        max = getUptimeString(maxSeconds)
        total = getUptimeString(totalSeconds)
    }
}

/**
 * DTO used by the REST API.
 */
class StatsDto(usersCounter: Counter, declaredGamesCounter: Counter, runningGamesCounter: Counter, uptimeCurrent: Long, uptimeMax: Long, uptimeTotal: Long) {
    val users = MeasureDto(usersCounter)
    val declaredGames = MeasureDto(declaredGamesCounter)
    val runningGames = MeasureDto(runningGamesCounter)
    val uptime = UptimeDto(getUptimeString(uptimeCurrent), getUptimeString(uptimeMax), getUptimeString(uptimeTotal))
}

class MeasureDto(counter: Counter) {
    val current = counter.getCurrent()
    val max = counter.getMax()
    val total = counter.getTotal()
}

class UptimeDto(val current: String, val max: String, val total: String)
