package com.adeliosys.keybout.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WordEffectTest {

    @Test
    fun none() {
        assertEquals("notebook", WordEffect.NONE.transform("notebook"))
    }

    @Test
    fun hidden() {
        val result = WordEffect.HIDDEN.transform("cat")
        assertTrue(result == "_at" || result == "c_t" || result == "ca_") { "Incorrect result '$result'" }
    }

    @Test
    fun reverse() {
        assertEquals("koobeton", WordEffect.REVERSE.transform("notebook"))
    }

    @Test
    fun shuffle() {
        val result = WordEffect.SHUFFLE.transform("range")
        assertEquals(5, result.length)
        assertTrue(result.contains("r") && result.contains("a")
                && result.contains("n") && result.contains("g") && result.contains("e")) {
            "Incorrect result '$result'"
        }
    }
}
