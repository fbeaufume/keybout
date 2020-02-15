package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.Constants.MAX_GENERATOR_ATTEMPTS
import com.adeliosys.keybout.model.Difficulty
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CalculusServiceTest {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val service = CalculusService()

    @TestFactory
    fun generateWords(): List<DynamicTest> {
        val tests = mutableListOf<DynamicTest>()
        Difficulty.values().forEach { difficulty ->
            tests.add(DynamicTest.dynamicTest("generate $difficulty") {
                generateOperations(difficulty)
            })
        }
        return tests
    }

    private fun generateOperations(difficulty: Difficulty) {
        val count = 80 // Number of operations to generate

        val (operations, attempts) = service.generateOperations(count, difficulty)

        logger.info("Generated ${operations.size} operations in $attempts attempts for $difficulty difficulty")

        // Check the number of operations
        assertEquals(count, operations.size) {
            "Operations count of $count is incorrect"
        }

        // Check the number of attempts
        Assertions.assertTrue(attempts <= MAX_GENERATOR_ATTEMPTS / 2) {
            "Attempts count of $attempts is too high"
        }

        // Check the result of the operation
        operations.forEach {
            val parts = it.display.split(" ")
            assertEquals(3, parts.size) {
                "Incorrect display '${it.display}'"
            }

            val left = toInt(parts[0], it.display)
            val right = toInt(parts[2], it.display)

            val operator: (Int, Int) -> Int = when (val part = parts[1]) {
                "+" -> { a, b -> a + b }
                "-" -> { a, b -> a - b }
                "*" -> { a, b -> a * b }
                else -> {
                    fail("Unknown operator '$part'")
                }
            }

            val result = operator.invoke(left, right)
            assertEquals(it.value, "$result")
        }

        checkConflicts(operations)
    }

    private fun toInt(string: String, display: String): Int {
        return try {
            string.toInt()
        } catch (e: Exception) {
            fail("'$string' from '$display' is not a number")
        }
    }
}
