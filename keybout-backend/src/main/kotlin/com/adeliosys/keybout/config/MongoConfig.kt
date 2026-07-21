package com.adeliosys.keybout.config

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
 * Enable MongoDB support only if "spring.mongodb.uri" property is defined
 * instead of connecting to localhost:27017 by default.
 */
@Configuration
@ConditionalOnProperty("spring.mongodb.uri")
@EnableMongoRepositories
class MongoConfig {

    @Bean
    fun mongoClient(
        @Value("\${spring.mongodb.uri}") uri: String
    ): MongoClient = MongoClients.create(uri)

    @Bean
    fun mongoDatabaseFactory(
        mongoClient: MongoClient,
        @Value("\${spring.mongodb.uri}") uri: String
    ): org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory =
        org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory(
            mongoClient, ConnectionString(uri).database!!
        )
}
