package com.adeliosys.keybout.util

import java.lang.management.ManagementFactory

/**
 * Return the current uptime in seconds.
 */
fun getUptimeSeconds() = ManagementFactory.getRuntimeMXBean().uptime / 1000

/**
 * Return a formatted uptime such as "7h 23m 37s".
 */
// TODO FBE return "1m 06s" instead of "1m 6s"
fun getUptimeString(uptimeInSec: Long): String =
    uptimeInSec.let {
        when {
            it >= 86400 -> "${it / 86400}d ${it.rem(86400) / 3600}h ${it.rem(3600) / 60}m ${it.rem(60)}s"
            it >= 3600 -> "${it / 3600}h ${it.rem(3600) / 60}m ${it.rem(60)}s"
            it >= 60 -> "${it / 60}m ${it.rem(60)}s"
            else -> "${it}s"
        }
    }
