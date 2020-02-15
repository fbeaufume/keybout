package com.adeliosys.keybout.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WordTest {

    @Test
    fun `no conflict`() {
        assertFalse(Word("abc", "").conflictsWith("def"))
    }

    @Test
    fun `conflict 1`() {
        assertTrue(Word("abc", "").conflictsWith("ab"))
    }

    @Test
    fun `conflict 2`() {
        assertTrue(Word("abc", "").conflictsWith("abc"))
    }

    @Test
    fun `conflict 3`() {
        assertTrue(Word("abc", "").conflictsWith("abcd"))
    }
}
