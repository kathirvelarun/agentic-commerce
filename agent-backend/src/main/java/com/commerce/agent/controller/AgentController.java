package com.commerce.agent.controller;

import com.commerce.agent.dto.AgentDtos.*;
import com.commerce.agent.service.FlowLogService;
import com.commerce.agent.service.ShoppingAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class AgentController {

    private final ShoppingAgentService agentService;
    private final FlowLogService flowLogService;
    private final ToolCallback[] mcpToolCallbacks;

    public AgentController(
            ShoppingAgentService agentService,
            FlowLogService flowLogService,
            @Qualifier("resolvedMcpToolCallbacks") ToolCallback[] mcpToolCallbacks) {
        this.agentService = agentService;
        this.flowLogService = flowLogService;
        this.mcpToolCallbacks = mcpToolCallbacks;
    }

    // ==================== CHAT ENDPOINTS ====================

    /**
     * POST /api/v1/chat
     * Send a message to the AI shopping agent
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        return ResponseEntity.ok(agentService.chat(request));
    }

    /**
     * GET /api/v1/chat/{sessionId}/history
     * Get conversation history for a session
     */
    @GetMapping("/chat/{sessionId}/history")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(agentService.getHistory(sessionId));
    }

    /**
     * DELETE /api/v1/chat/{sessionId}
     * Clear a chat session
     */
    @DeleteMapping("/chat/{sessionId}")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId) {
        agentService.clearSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Session cleared", "sessionId", sessionId));
    }

    // ==================== CARD MANAGEMENT ENDPOINTS ====================

    /**
     * GET /api/v1/users/{userId}/cards
     * Get all payment cards for a user
     */
    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<List<CardDto>> getUserCards(@PathVariable String userId) {
        return ResponseEntity.ok(agentService.getUserCards(userId));
    }

    /**
     * POST /api/v1/users/{userId}/cards
     * Add a new payment card
     */
    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<CardDto> addCard(
            @PathVariable String userId,
            @RequestBody AddCardRequest request) {
        return ResponseEntity.ok(agentService.addCard(userId, request));
    }

    /**
     * DELETE /api/v1/users/{userId}/cards/{cardId}
     * Delete a payment card
     */
    @DeleteMapping("/users/{userId}/cards/{cardId}")
    public ResponseEntity<Map<String, String>> deleteCard(
            @PathVariable String userId,
            @PathVariable String cardId) {
        agentService.deleteCard(userId, cardId);
        return ResponseEntity.ok(Map.of("message", "Card deleted"));
    }

    /**
     * PUT /api/v1/users/{userId}/cards/{cardId}/default
     * Set a card as default payment method
     */
    @PutMapping("/users/{userId}/cards/{cardId}/default")
    public ResponseEntity<CardDto> setDefaultCard(
            @PathVariable String userId,
            @PathVariable String cardId) {
        return ResponseEntity.ok(agentService.setDefaultCard(userId, cardId));
    }

    /**
     * PUT /api/v1/users/{userId}/cards/{cardId}/activate
     * Activate a card after passkey verification
     */
    @PutMapping("/users/{userId}/cards/{cardId}/activate")
    public ResponseEntity<CardDto> activateCard(
            @PathVariable String userId,
            @PathVariable String cardId) {
        return ResponseEntity.ok(agentService.activateCard(userId, cardId));
    }

    // ==================== FLOW LOGS ====================

    /** GET /api/v1/logs — all logs (latest first, max 200) */
    @GetMapping("/logs")
    public ResponseEntity<List<FlowLogDto>> getAllLogs() {
        return ResponseEntity.ok(flowLogService.getAll());
    }

    /** DELETE /api/v1/logs — delete all logs */
    @DeleteMapping("/logs")
    public ResponseEntity<Map<String, String>> deleteAllLogs() {
        flowLogService.deleteAll();
        return ResponseEntity.ok(Map.of("message", "All logs deleted"));
    }

    /** GET /api/v1/logs/{sessionId} — logs for a specific session */
    @GetMapping("/logs/{sessionId}")
    public ResponseEntity<List<FlowLogDto>> getSessionLogs(@PathVariable String sessionId) {
        return ResponseEntity.ok(flowLogService.getBySession(sessionId));
    }

    /** DELETE /api/v1/logs/{sessionId} — delete all logs for a session */
    @DeleteMapping("/logs/{sessionId}")
    public ResponseEntity<Map<String, String>> deleteSessionLogs(@PathVariable String sessionId) {
        flowLogService.deleteBySession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Logs deleted", "sessionId", sessionId));
    }

    // ==================== HEALTH & DIAGNOSTICS ====================

    /** GET /api/v1/health — basic health check */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "agent-backend",
                "port", 8000,
                "mcpToolsLoaded", mcpToolCallbacks.length,
                "mcpTools", Arrays.stream(mcpToolCallbacks)
                        .map(cb -> cb.getToolDefinition().name())
                        .toList()
        ));
    }

    /** GET /api/v1/mcp/tools — list all registered MCP tools (diagnostic) */
    @GetMapping("/mcp/tools")
    public ResponseEntity<Map<String, Object>> mcpTools() {
        var tools = Arrays.stream(mcpToolCallbacks)
                .map(cb -> Map.of(
                        "name", cb.getToolDefinition().name(),
                        "description", cb.getToolDefinition().description()
                ))
                .toList();
        return ResponseEntity.ok(Map.of(
                "count", tools.size(),
                "tools", tools
        ));
    }
}
