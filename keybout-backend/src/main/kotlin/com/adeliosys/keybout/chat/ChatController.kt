package com.adeliosys.keybout.chat

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.springframework.web.util.HtmlUtils

@Controller
class ChatController {
    @MessageMapping("/message")
    @SendTo("/topic/chat")
    fun greeting(message: String): String {
        return HtmlUtils.htmlEscape(message)
    }
}
