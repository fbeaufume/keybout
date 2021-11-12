package com.adeliosys.keybout.repository

import com.adeliosys.keybout.model.TopScoresDocument
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.repository.MongoRepository

@ConditionalOnProperty("spring.data.mongodb.uri")
interface ScoreRepository : MongoRepository<TopScoresDocument, String> {

    fun findByDataType(dataType: String): TopScoresDocument?
}
