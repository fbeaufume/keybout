package com.adeliosys.keybout.api

import com.adeliosys.keybout.model.Difficulty
import com.adeliosys.keybout.model.GameStyle
import com.adeliosys.keybout.model.Language
import com.adeliosys.keybout.model.TopScoresDto
import com.adeliosys.keybout.service.ScoreService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ScoreController(private val scoreService: ScoreService) {

    /**
     * Return the top scores for a given style and language, or a given style, language and difficulty.
     */
    @GetMapping("/scores")
    fun getTopScoresStats(
        @RequestParam(required = false, defaultValue = "") style: String,
        @RequestParam(required = false, defaultValue = "") language: String,
        @RequestParam(required = false, defaultValue = "") difficulty: String
    ): List<TopScoresDto> {
        val s = GameStyle.getByCode(style)
        val l = Language.getByCode(language, s)

        return if (difficulty.isEmpty()) {
            scoreService.getTopScores(s, l)
        } else {
            scoreService.getTopScores(s, l, Difficulty.getByCode(difficulty))
        }
    }

    /**
     * Force an immediate save of the top scores.
     */
    @GetMapping("/scores/save")
    fun saveStats() {
        scoreService.saveTopScores()
    }
}
