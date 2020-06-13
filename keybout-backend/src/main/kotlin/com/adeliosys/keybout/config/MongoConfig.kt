package com.adeliosys.keybout.config

import com.mongodb.MongoClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

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
class MongoConfig : MongoAutoConfiguration()
