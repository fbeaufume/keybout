package com.adeliosys.keybout.util

/**
 * A thread-safe incrementable and decrementable counter keeping track of the current, max and total values.
 */
class Counter {

    private var current = 0

    private var max = 0

    private var total = 0L

    fun getCurrent() = current

    fun getMax() = max

    @Synchronized
    fun getTotal() = total

    @Synchronized
    fun increment() {
        current++
        max = maxOf(max, current)
        total++
    }

    @Synchronized
    fun decrement() {
        current--
    }
}
