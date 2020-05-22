package com.adeliosys.keybout

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [MongoAutoConfiguration::class])
class KeyboutApplication

fun main(args: Array<String>) {
    runApplication<KeyboutApplication>(*args)
}
