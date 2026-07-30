package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.Constants.MAX_GENERATOR_ATTEMPTS
import com.adeliosys.keybout.model.Difficulty
import com.adeliosys.keybout.model.Word
import org.springframework.stereotype.Service
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.math.pow

/**
 * Provide the calculus for a round.
 */
@Service
class CalculusService {

    /**
     * Generate random numbers
     */
    public fun generateNumbers(count: Int, difficulty: Difficulty): Pair<List<Word>, Int> {
        val words = mutableListOf<Word>()
        var attempts = 0

        while (words.size < count && attempts < MAX_GENERATOR_ATTEMPTS) {
            val numberLenght = Random.nextInt(difficulty.numberRange)
            val number = Random.nextInt(10.toDouble().pow(numberLenght - 1).toInt()..(10.toDouble().pow(numberLenght) - 1).toInt())
            val word = Word("$number", "$number")

            // val numberLenght = Random.nextInt(difficulty.numberRange) - 1
            // var digits = mutableListOf<Int>(Random.nextInt(1..9))
            // for (i in 0..numberLenght) {
            //     digits.add(Random.nextInt(0..9))
            // }
            // val p = digits.joinToString(prefix = "", postfix = "", separator = "")
            // val word = Word("$p", "$p")

            addIfNoConflict(word, words)

            attempts++
        }

        return Pair(words, attempts)
    }

    /**
     * Generate random operations
     */
    fun generateOperations(count: Int, difficulty: Difficulty): Pair<List<Word>, Int> {
        val words = mutableListOf<Word>()
        var attempts = 0

        while (words.size < count && attempts < MAX_GENERATOR_ATTEMPTS) {
            val word = when (Random.nextInt(2)) {
                0 -> {
                    // Generate an addition
                    val p = Pair(Random.nextInt(difficulty.additionRanges.first), Random.nextInt(difficulty.additionRanges.second))
                    Word("${p.first + p.second}", "${p.first} + ${p.second}")
                }
                else -> {
                    // Generate a subtraction
                    val p = Pair(Random.nextInt(difficulty.subtractionRanges.first), Random.nextInt(difficulty.subtractionRanges.second))
                    Word("${p.first - p.second}", "${p.first} - ${p.second}")
                }
            }

            addIfNoConflict(word, words)

            attempts++
        }

        return Pair(words, attempts)
    }

    /**
     * Add a word to the list of words, only if the word does not conflict with any other words.
     */
    private fun addIfNoConflict(word: Word, words: MutableList<Word>) {
        var conflict = false

        for (tempWord in words) {
            if (tempWord.conflictsWith(word.value)) {
                conflict = true
                break
            }
        }

        if (!conflict) {
            words.add(word)
        }
    }
}
