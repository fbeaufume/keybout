package com.adeliosys.keybout.repository

import com.adeliosys.keybout.model.Stats
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.repository.MongoRepository

@ConditionalOnProperty("spring.data.mongodb.uri")
interface StatsRepository : MongoRepository<Stats, String> {

    fun findByDataType(dataType: String): Stats?
}
