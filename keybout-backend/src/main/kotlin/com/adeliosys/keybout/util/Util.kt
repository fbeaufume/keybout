package com.adeliosys.keybout.util

import java.lang.management.ManagementFactory

/**
 * Return the current uptime in seconds.
 */
fun getUptimeSeconds() = ManagementFactory.getRuntimeMXBean().uptime / 1000

/**
 * Return a formatted uptime such as "7h 03m 42s".
 */
fun getUptimeString(uptimeInSec: Long): String =
    uptimeInSec.let {
        when {
            it >= 86400 -> "${it / 86400}d ${pad(it.rem(86400) / 3600)}h ${pad(it.rem(3600) / 60)}m ${pad(it.rem(60))}s"
            it >= 3600 -> "${it / 3600}h ${pad(it.rem(3600) / 60)}m ${pad(it.rem(60))}s"
            it >= 60 -> "${it / 60}m ${pad(it.rem(60))}s"
            else -> "${it}s"
        }
    }

/**
 * Prepend single digit longs with a zero, i.e. 6 -> "06".
 */
fun pad(i: Long) = i.toString().padStart(2, '0')
