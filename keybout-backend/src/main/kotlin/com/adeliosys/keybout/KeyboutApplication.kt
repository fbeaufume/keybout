package com.adeliosys.keybout

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(exclude = [MongoAutoConfiguration::class])
@EnableScheduling
class KeyboutApplication

fun main(args: Array<String>) {
    runApplication<KeyboutApplication>(*args)
}
