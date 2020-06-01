package com.adeliosys.keybout.util

import com.adeliosys.keybout.model.Measure

/**
 * A thread-safe incrementable and decrementable counter keeping track of the current, max and total values.
 */
class Counter {

    private var current = 0

    private var max = 0

    private var total = 0

    fun getCurrent() = current

    fun getMax() = max

    @Synchronized
    fun initialize(measure: Measure) {
        max = measure.max
        total = measure.total
    }

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
