package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.Constants.MAX_GENERATOR_ATTEMPTS
import com.adeliosys.keybout.model.Difficulty
import com.adeliosys.keybout.model.Word
import org.springframework.stereotype.Service
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Provide the calculus for a round.
 */
@Service
class CalculusService {

    /**
     * Generate random operations
     */
    fun generateOperations(count: Int, difficulty: Difficulty): Pair<List<Word>, Int> {
        val words = mutableListOf<Word>()
        var attempts = 0

//        println("-----------------------------------------") // TODO

        while (words.size < count && attempts < MAX_GENERATOR_ATTEMPTS) {
            val word = when (Random.nextInt(3)) {
                0 -> {
                    // Generate an addition
                    val p = Pair(Random.nextInt(difficulty.additionLeftRange), Random.nextInt(difficulty.additionRightRange))
                    Word("${p.first + p.second}", "${p.first} + ${p.second}")
                }
                1 -> {
                    // Generate a subtraction
                    val p = Pair(Random.nextInt(difficulty.subtractionLeftRange), Random.nextInt(difficulty.subtractionRightRange))
                    Word("${p.first - p.second}", "${p.first} - ${p.second}")
                }
                else -> {
                    // Generate a multiplication
                    val p = Pair(Random.nextInt(difficulty.multiplicationLeftRange), Random.nextInt(difficulty.multiplicationRightRange))
                    Word("${p.first * p.second}", "${p.first} * ${p.second}")
                }
            }

            addIfNoConflict(word, words)
//            println("display=${word.display}     value=${word.value}     count=${words.size}     attempt=$attempts") // TODO

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
