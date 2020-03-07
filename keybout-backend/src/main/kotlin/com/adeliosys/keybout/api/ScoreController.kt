package com.adeliosys.keybout.api

import com.adeliosys.keybout.model.Difficulty
import com.adeliosys.keybout.model.GameStyle
import com.adeliosys.keybout.model.Language
import com.adeliosys.keybout.model.TopScore
import com.adeliosys.keybout.service.ScoreService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ScoreController(private val scoreService: ScoreService) {

    @GetMapping("/scores")
    fun getTopScoresStats(
            @RequestParam(required = false, defaultValue = "REGULAR") style: GameStyle,
            @RequestParam(required = false, defaultValue = "EN") language: Language,
            @RequestParam(required = false, defaultValue = "NORMAL") difficulty: Difficulty
    ): List<TopScore> = scoreService.getTopScores(style, language, difficulty)
}
