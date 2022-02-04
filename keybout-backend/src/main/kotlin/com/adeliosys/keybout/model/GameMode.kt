package com.adeliosys.keybout.model

import com.adeliosys.keybout.service.BaseGameService
import com.adeliosys.keybout.service.CaptureGameService
import com.adeliosys.keybout.service.RaceGameService
import com.google.gson.annotations.SerializedName

enum class GameMode(public val type: Class<out BaseGameService>) {

    @SerializedName("capture")
    CAPTURE(CaptureGameService::class.java),
    @SerializedName("race")
    RACE(RaceGameService::class.java);

    companion object {
        fun getByCode(code: String) = try {
            valueOf(code.uppercase())
        } catch (e: Exception) {
            CAPTURE
        }
    }
}
