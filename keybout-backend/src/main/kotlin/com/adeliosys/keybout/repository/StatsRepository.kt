package com.adeliosys.keybout.repository

import com.adeliosys.keybout.model.StatsDocument
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.repository.MongoRepository

@ConditionalOnProperty("spring.data.mongodb.uri")
interface StatsRepository : MongoRepository<StatsDocument, String> {

    fun findByEnvironmentName(environmentName: String): StatsDocument?
}
