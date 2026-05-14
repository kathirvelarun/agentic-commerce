package com.amex.ace.agent.controller;

import com.amex.ace.agent.dto.chat.ChatRequest;
import com.amex.ace.agent.dto.chat.ChatResponse;
import com.amex.ace.agent.service.AgentService;
import com.amex.ace.agent.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Chat endpoints.
 * Maps Python src/api/routes/chat.py router.
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final AgentService agentService;

    @Autowired
    public ChatController(ChatService chatService, AgentService agentService) {
        this.chatService  = chatService;
        this.agentService = agentService;
    }

    /** POST /chat — process a user message. */
    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.processMessage(request.message(), request.products());
    }

    /** POST /chat/reset — reset the conversation thread. */
    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetChat() {
        agentService.resetThread();
    }
}
