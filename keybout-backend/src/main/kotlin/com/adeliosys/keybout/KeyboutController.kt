package com.adeliosys.keybout

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KeyboutController {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    val map = mapOf(
        "1" to Note("1", "Title 1", "Description 1"),
        "2" to Note("2", "Title 2", "Description 2"),
        "3" to Note("3", "Title 3", "Description 3"))

    @GetMapping("/api/hello")
    fun sayHello(@RequestParam(value = "name", defaultValue = "world") name: String): String {
        return "Hello $name!"
    }

    @GetMapping("/api/notes")
    fun getNotes(): Collection<Note> {
        return map.values
    }

    @GetMapping("/api/notes/{id}")
    fun getNote(@PathVariable id: String): Note? {
        return map.get(id)
    }
}
