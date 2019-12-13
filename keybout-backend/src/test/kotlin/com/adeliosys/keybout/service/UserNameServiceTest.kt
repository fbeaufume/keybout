package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.TooLongNameNotification
import com.adeliosys.keybout.model.TooShortNameNotification
import com.adeliosys.keybout.model.UsedNameNotification
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserNameServiceTest {

    private val service = UserNameService()

    @Test
    fun `empty name`() {
        assertTrue(service.registerUserName("") is TooShortNameNotification)
    }

    @Test
    fun `too short name`() {
        assertTrue(service.registerUserName("A") is TooShortNameNotification)
    }

    @Test
    fun `not too short name`() {
        assertNull(service.registerUserName("AA"))
    }

    @Test
    fun `too long name`() {
        assertTrue(service.registerUserName("12345678901234567") is TooLongNameNotification)
    }

    @Test
    fun `not too long name`() {
        assertNull(service.registerUserName("1234567890123456"))
    }

    @Test
    fun `used name`() {
        assertNull(service.registerUserName("ABC"))
        assertTrue(service.registerUserName("ABC") is UsedNameNotification)
    }
}
