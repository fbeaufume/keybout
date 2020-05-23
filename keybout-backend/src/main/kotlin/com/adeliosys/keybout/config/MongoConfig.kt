package com.adeliosys.keybout.config

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

/**
 * Custom MongoDB configuration class inspired by [org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration].
 * Enables MongoDB support only if "spring.data.mongodb.uri" property is defined,
 * instead of connecting to localhost:27017 by default.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MongoClient::class)
@EnableConfigurationProperties(MongoProperties::class)
@ConditionalOnMissingBean(type = ["org.springframework.data.mongodb.MongoDbFactory"])
@ConditionalOnProperty("spring.data.mongodb.uri")
// TODO FBE simplify this
class MongoConfig {

    @Bean
    @ConditionalOnMissingBean(type = ["com.mongodb.MongoClient", "com.mongodb.client.MongoClient"])
    fun mongo(properties: MongoProperties?, options: ObjectProvider<MongoClientOptions?>, environment: Environment?): MongoClient? {
        return MongoClientFactory(properties, environment).createMongoClient(options.ifAvailable)
    }
}
