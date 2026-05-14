package com.amex.ace.agent.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amex.ace.agent.dto.commerce.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI agent service — Spring AI ChatClient wrapper.
 *
 * Replaces Python src/services/agent.py:
 *   - agent (LangChain agent) → chatClient (Spring AI ChatClient)
 *   - current_thread_id       → currentThreadId (volatile field)
 *   - current_credentials     → currentCredentials (volatile field)
 *   - send_message()          → sendUserMessage() / sendSystemMessage()
 *   - reset_thread()          → resetThread()
 *   - store_credentials()     → storeCredentials()  [via ElicitationHandler]
 *   - clear_credentials()     → clearCredentials()
 *
 * Note: volatile fields are safe for the single-user reference implementation.
 * For multi-user production deployment, scope this bean to @SessionScope.
 */
@Service
@Slf4j
public class AgentService {

    private final ChatClient chatClient;
    private final ElicitationHandler elicitationHandler;
    private final ObjectMapper objectMapper;

    private volatile String currentThreadId = UUID.randomUUID().toString();

    @Autowired
    public AgentService(ChatClient chatClient,
                        ElicitationHandler elicitationHandler,
                        ObjectMapper objectMapper) {
        this.chatClient          = chatClient;
        this.elicitationHandler  = elicitationHandler;
        this.objectMapper        = objectMapper;
    }

    /**
     * Sends a user message to the LLM agent and returns the parsed JSON response.
     * Equivalent to Python: await send_message(HumanMessage(content=message))
     */
    public Map<String, Object> sendUserMessage(String formattedMessage) {
        // For POC, return mock response instead of calling real AI
        log.info("Mock AI call for user message: {}", formattedMessage);
        return getMockResponse();
    }

    /**
     * Sends a system message (e.g. "COMPLETE CHECKOUT") to the LLM agent.
     * Equivalent to Python: await send_message(SystemMessage(content=message))
     */
    public Map<String, Object> sendSystemMessage(String content) {
        // For POC, return mock response instead of calling real AI
        log.info("Mock AI call for system message: {}", content);
        return getMockCheckoutResponse();
    }

    private Map<String, Object> getMockResponse() {
        return Map.of(
                "message", "Hello! I'm your AI shopping assistant. I can help you find products and complete purchases. What would you like to shop for today?",
                "products", List.of(
                        Map.of("name", "Wireless Headphones", "price", "99.99", "image", "https://example.com/headphones.jpg", "sku", "HP001", "description", "High-quality wireless headphones"),
                        Map.of("name", "Smart Watch", "price", "199.99", "image", "https://example.com/watch.jpg", "sku", "SW001", "description", "Feature-rich smart watch")
                ),
                "order_summary", Map.of(
                        "merchant_name", "Mock Store",
                        "overall_amount", "$299.98"
                )
        );
    }

    private Map<String, Object> getMockCheckoutResponse() {
        return Map.of(
                "message", "Checkout completed successfully! Your order has been processed.",
                "purchase_summary", Map.of(
                        "merchant", "Mock Store",
                        "overall_amount", "$299.98",
                        "order_id", "ORD-MOCK-12345",
                        "tracking_code", "TRK-MOCK-67890"
                )
        );
    }

    /** Resets the conversation thread — equivalent to Python reset_thread(). */
    public void resetThread() {
        currentThreadId = UUID.randomUUID().toString();
        clearCredentials();
        log.info("Conversation thread reset. New thread ID: {}", currentThreadId);
    }

    /** Stores credentials for MCP elicitation — equivalent to Python store_credentials(). */
    public void storeCredentials(Credentials credentials) {
        elicitationHandler.setCredentials(credentials);
    }

    /** Clears stored credentials — equivalent to Python clear_credentials(). */
    public void clearCredentials() {
        elicitationHandler.clearCredentials();
    }

    // -------------------------------------------------------------------------

    private Map<String, Object> parseResponse(String raw) {
        try {
            // Strip markdown code fences if present
            String cleaned = raw == null ? "{}" : raw
                    .replaceAll("(?s)```json\\s*", "")
                    .replace("```", "")
                    .trim();
            return objectMapper.readValue(cleaned, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse agent response as JSON: {}", raw, e);
            throw new RuntimeException("Agent returned non-JSON response", e);
        }
    }
}
