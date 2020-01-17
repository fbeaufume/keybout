package com.adeliosys.keybout.service

import com.adeliosys.keybout.model.Constants.FIRST_AWARD
import com.adeliosys.keybout.model.Constants.LAST_AWARD
import com.adeliosys.keybout.model.Constants.LONGEST_AWARD
import com.adeliosys.keybout.model.Score
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.lang.Integer.max

/**
 * Awards management service.
 * Concurrency is managed by the caller.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class AwardService {

    // The latest user who won a word, used to track the "first" awards
    var latestUserName: String? = null

    // Length of the longest word, used to track the "longest" award
    var longestLength = 0

    // Used to prevent the "longest" award to be given multiple times
    var isLongestAvailable = true

    /**
     * Initialize this service at the beginning of each round.
     */
    fun initializeRound(words: List<String>) {
        isLongestAvailable = true

        latestUserName = null

        longestLength = 0
        words.forEach { longestLength = max(longestLength, it.length) }
    }

    /**
     * Give awards when the conditions are met.
     * This method is called after a player won a word.
     */
    fun checkAwards(score: Score, word: String, lastWord: Boolean) {
        if (latestUserName == null) {
            score.addAward(FIRST_AWARD)
        }

        if (isLongestAvailable && word.length == longestLength) {
            score.addAward(LONGEST_AWARD)
            isLongestAvailable = false
        }

        if (lastWord) {
            score.addAward(LAST_AWARD)
        }

        latestUserName = score.userName
    }
}
