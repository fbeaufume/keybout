package com.adeliosys.keybout.note

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
class NoteController {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    val map = mutableMapOf(
            1 to Note(1, "Message 1"),
            2 to Note(2, "Message 2"),
            3 to Note(3, "Message 3"),
            4 to Note(4, "Message 4"),
            5 to Note(5, "Message 5"))

    @GetMapping("/api/notes")
    fun getNotes(@RequestParam(required = false) message: String?): Collection<Note> {
        return if (message == null) {
            map.values
        }
        else {
            map.values.filter { n -> n.message.contains(message) }
        }
    }

    @GetMapping("/api/notes/{id}")
    fun getNote(@PathVariable id: Int): Note? {
        return map[id]
    }

    @PostMapping
    fun createNote(@RequestBody note: Note): Note {
        note.id = map.size + 1
        updateNote(note)
        return note
    }

    @PutMapping
    fun updateNote(@RequestBody note: Note) {
        map[note.id] = note
    }

    @DeleteMapping("/api/notes/{id}")
    fun deleteNote(@PathVariable id: Int) {
        map.remove(id)
    }
}
