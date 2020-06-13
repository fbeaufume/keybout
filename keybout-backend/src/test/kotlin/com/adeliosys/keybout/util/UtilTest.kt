package com.adeliosys.keybout.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UtilTest {

    @Test
    fun `seconds uptime`() {
        Assertions.assertEquals("21s", getUptimeString(21))
    }

    @Test
    fun `minutes uptime`() {
        Assertions.assertEquals("37m 21s", getUptimeString(2241))
    }

    @Test
    fun `minutes uptime short`() {
        Assertions.assertEquals("7m 01s", getUptimeString(421))
    }

    @Test
    fun `hours uptime`() {
        Assertions.assertEquals("16h 37m 21s", getUptimeString(59841))
    }

    @Test
    fun `hours uptime short`() {
        Assertions.assertEquals("1h 07m 06s", getUptimeString(4026))
    }

    @Test
    fun `days uptime`() {
        Assertions.assertEquals("43d 16h 37m 21s", getUptimeString(3775041))
    }

    @Test
    fun `days uptime short`() {
        Assertions.assertEquals("43d 06h 07m 08s", getUptimeString(3737228))
    }
}
