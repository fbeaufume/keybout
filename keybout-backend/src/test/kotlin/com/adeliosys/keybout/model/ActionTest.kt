package com.adeliosys.keybout.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActionTest {

    @Test
    fun `empty action`() {
        val action = Action("hello")

        assertTrue(action.checkArgumentsCount(0)) { "Wrong argument count" }
        assertEquals("hello", action.command) { "Wrong command" }
        assertEquals(listOf<String>(), action.arguments) { "Wrong arguments" }
    }

    @Test
    fun `single argument action`() {
        val action = Action("hello world")

        assertTrue(action.checkArgumentsCount(1)) { "Wrong argument count" }
        assertEquals("hello", action.command) { "Wrong command" }
        assertEquals(listOf("world"), action.arguments) { "Wrong arguments" }
    }

    @Test
    fun `double argument action`() {
        val action = Action("hello big world")

        assertTrue(action.checkArgumentsCount(2)) { "Wrong argument count" }
        assertEquals("hello", action.command) { "Wrong command" }
        assertEquals(listOf("big", "world"), action.arguments) { "Wrong arguments" }
    }
}
