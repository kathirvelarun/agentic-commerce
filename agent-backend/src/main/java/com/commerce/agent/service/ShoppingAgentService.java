package com.commerce.agent.service;

import com.commerce.agent.dto.AgentDtos.*;
import com.commerce.agent.model.AgentCard;
import com.commerce.agent.model.AgentCardRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ShoppingAgentService {

    private final ChatClient chatClient;
    private final AgentCardRepository cardRepository;
    private final ToolCallback[] mcpToolCallbacks;
    private final ObjectMapper objectMapper;
    private final FlowLogService flowLogService;

    private final Map<String, List<Message>> sessions = new ConcurrentHashMap<>();

    /**
     * Build the ChatClient eagerly with the system prompt.
     * Inject ToolCallback[] by the bean name defined in AgentConfig —
     * this avoids the ToolCallbackProvider compile-time issue and the
     * duplicate-bean conflict with McpToolCallbackAutoConfiguration.
     */
    public ShoppingAgentService(
            ChatClient.Builder chatClientBuilder,
            AgentCardRepository cardRepository,
            @Qualifier("resolvedMcpToolCallbacks") ToolCallback[] mcpToolCallbacks,
            ObjectMapper objectMapper,
            FlowLogService flowLogService) {
        this.cardRepository = cardRepository;
        this.mcpToolCallbacks = mcpToolCallbacks;
        this.objectMapper = objectMapper;
        this.flowLogService = flowLogService;
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
        log.info("ShoppingAgentService initialized with {} MCP tool callbacks", mcpToolCallbacks.length);
        for (ToolCallback cb : mcpToolCallbacks) {
            log.info("  Registered MCP tool: {}", cb.getToolDefinition().name());
        }
    }

    // ── System prompt ────────────────────────────────────────────────────────
    private static final String SYSTEM_PROMPT = """
            You are an intelligent AI shopping assistant for AMEX Commerce.
            Your goal is to help users discover products, manage their cart, and complete purchases.

            You MUST use the available MCP tools to answer product queries — never invent product data.

            Available tools:
            - search_products(query, category): Search the product catalog. ALWAYS call this when user asks for products.
            - get_product_details(productId): Get details of a specific product.
            - get_categories(): List all product categories.
            - add_to_cart(userId, productId, quantity): Add a product to the cart.
            - get_cart(userId): Get the user's current cart.
            - update_cart_item(userId, itemId, quantity): Update cart item quantity.
            - remove_from_cart(userId, itemId): Remove an item from the cart.
            - checkout(userId, cartId, cardLast4, cardBrand): Process payment and place order.
            - get_orders(userId): Get order history.

            CRITICAL RULES:
            1. When a user asks to find/show/recommend products → call search_products immediately.
            2. After getting tool results, format your response as JSON:
               {"message": "conversational text here", "products": [...array from tool result...]}
            3. For non-product responses (cart, orders, chat) → return plain text.
            4. Never say you cannot search — you have the search_products tool. Use it.
            5. Always pass the userId from context when calling cart/order tools.
            """;

    private static final OpenAiChatOptions CHAT_OPTIONS = OpenAiChatOptions.builder()
            .model("gpt-4o-mini")
            .temperature(0.3)   // lower temp = more reliable tool use
            .build();

    // ── Main chat ────────────────────────────────────────────────────────────
    public ChatResponse chat(ChatRequest request) {
        String sessionId = request.sessionId() != null
                ? request.sessionId()
                : UUID.randomUUID().toString();

        String userId = request.userId() != null ? request.userId() : "user-demo-001";
        String requestId = UUID.randomUUID().toString();
        List<Message> history = sessions.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // ═══════════════════════════════════════════════════════════════════════
        // STEP 1: UI → AGENT BACKEND (Request Received)
        // ═══════════════════════════════════════════════════════════════════════
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║ STEP 1: UI → AGENT BACKEND (Request Received)                 ║");
        log.info("╚════════════════════════════════════════════════════════════════╝");
        log.info("   SessionId: {} | UserId: {} | Message: {}", sessionId, userId, request.message());

        flowLogService.save(sessionId, requestId, 1,
                "UI → AGENT BACKEND (Request Received)", "COMPLETED",
                "UserId: " + userId + " | Message: " + truncate(request.message(), 100)
                        + " | History: " + history.size() + " msgs",
                0L);

        String enrichedMessage = request.message() + "\n[userId: " + userId + "]";
        history.add(new UserMessage(enrichedMessage));

        // ═══════════════════════════════════════════════════════════════════════
        // STEP 2: AGENT BACKEND → LLM (OpenAI)
        // ═══════════════════════════════════════════════════════════════════════
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║ STEP 2: AGENT BACKEND → LLM (OpenAI)                          ║");
        log.info("╚════════════════════════════════════════════════════════════════╝");
        log.info("   Model: gpt-4o-mini | MCP Tools: {} | History: {} msgs",
                mcpToolCallbacks.length, history.size() - 1);

        flowLogService.save(sessionId, requestId, 2,
                "AGENT BACKEND → LLM (OpenAI)", "COMPLETED",
                "Model: gpt-4o-mini | Temp: 0.3 | MCP tools attached: " + mcpToolCallbacks.length
                        + " | History: " + (history.size() - 1) + " msgs",
                0L);

        String rawResponse;
        long startTime = System.currentTimeMillis();
        try {
            log.info("   Calling OpenAI (may trigger MCP tool round-trips)...");

            rawResponse = chatClient.prompt()
                    .messages(history.subList(0, Math.max(0, history.size() - 1)))
                    .user(enrichedMessage)
                    .toolCallbacks(mcpToolCallbacks)
                    .options(CHAT_OPTIONS)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            String toolsUsed = detectToolsUsed(rawResponse, request.message());
            boolean hasMcpCalls = !toolsUsed.equals("none");

            // ═══════════════════════════════════════════════════════════════════════
            // STEP 3: LLM → MCP (Tool Dispatch)
            // ═══════════════════════════════════════════════════════════════════════
            log.info("╔════════════════════════════════════════════════════════════════╗");
            log.info("║ STEP 3: LLM → MCP (Tool Dispatch)                             ║");
            log.info("╚════════════════════════════════════════════════════════════════╝");
            log.info("   Tools invoked: {} | Route: OpenAI → Spring AI → AsyncMcpToolCallbackProvider → merchant-mcp:8002", toolsUsed);

            flowLogService.save(sessionId, requestId, 3,
                    "LLM → MCP (Tool Dispatch)", "COMPLETED",
                    hasMcpCalls
                            ? "Tools invoked: " + toolsUsed + " | Route: OpenAI → Spring AI → merchant-mcp:8002"
                            : "No tool call — LLM answered directly from context",
                    null);

            // ═══════════════════════════════════════════════════════════════════════
            // STEP 4: MCP → MERCHANT API (Result)
            // ═══════════════════════════════════════════════════════════════════════
            ParsedResponse parsed = parse(rawResponse);
            log.info("╔════════════════════════════════════════════════════════════════╗");
            log.info("║ STEP 4: MCP → MERCHANT API (Tool Execution Result)            ║");
            log.info("╚════════════════════════════════════════════════════════════════╝");
            log.info("   Duration: {}ms | Response: {} | Products: {}",
                    duration,
                    rawResponse.startsWith("{") ? "JSON" : "Plain Text",
                    parsed.products().size());

            flowLogService.save(sessionId, requestId, 4,
                    "MCP → MERCHANT API (Tool Execution Result)", "COMPLETED",
                    "Total duration: " + duration + "ms"
                            + " | Response type: " + (rawResponse.startsWith("{") ? "JSON" : "Plain Text")
                            + " | Products returned: " + parsed.products().size()
                            + (hasMcpCalls ? " | Route: merchant-mcp:8002 → merchant-backend:8001" : ""),
                    duration);

            history.add(new AssistantMessage(parsed.message()));
            sessions.put(sessionId, history);

            return new ChatResponse(
                    sessionId, parsed.message(), "assistant",
                    LocalDateTime.now(), parsed.products(), null, null,
                    detectIntent(request.message()));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String errMsg = getRootCause(e);
            log.error("╔════════════════════════════════════════════════════════════════╗");
            log.error("║ ERROR in LLM/MCP Communication: {}  ║", e.getClass().getSimpleName());
            log.error("╚════════════════════════════════════════════════════════════════╝");

            flowLogService.save(sessionId, requestId, 3,
                    "LLM → MCP (Tool Dispatch)", "ERROR",
                    e.getClass().getSimpleName() + ": " + truncate(errMsg, 200), duration);

            flowLogService.save(sessionId, requestId, 4,
                    "MCP → MERCHANT API (Tool Execution Result)", "ERROR",
                    isTimeoutError(e)
                            ? "TIMEOUT — ensure merchant-mcp:8002 and merchant-backend:8001 are running"
                            : "Failed: " + truncate(errMsg, 200),
                    duration);

            rawResponse = isTimeoutError(e)
                    ? "⚠️ Connection timed out. Ensure merchant-backend (8001) and merchant-mcp (8002) are running."
                    : "⚠️ Error: " + errMsg;

            ParsedResponse parsed = parse(rawResponse);
            history.add(new AssistantMessage(parsed.message()));
            sessions.put(sessionId, history);

            return new ChatResponse(
                    sessionId, parsed.message(), "assistant",
                    LocalDateTime.now(), parsed.products(), null, null,
                    detectIntent(request.message()));
        }
    }

    private String detectToolsUsed(String response, String userMessage) {
        // 1. JSON response with products → search_products was called
        String trimmed = response.trim();
        if (trimmed.startsWith("{")) {
            try {
                JsonNode root = objectMapper.readTree(trimmed);
                if (root.has("products") && root.get("products").isArray()
                        && root.get("products").size() > 0) {
                    return "search_products";
                }
            } catch (Exception ignored) {}
        }

        // 2. Infer from response text patterns
        String lower = response.toLowerCase();
        if (lower.contains("added to your cart") || lower.contains("item added")) return "add_to_cart";
        if (lower.contains("removed from") && lower.contains("cart"))            return "remove_from_cart";
        if (lower.contains("your cart") && lower.contains("item"))               return "get_cart";
        if (lower.contains("order placed") || lower.contains("order confirmed")) return "checkout";
        if (lower.contains("order history") || lower.contains("your orders"))    return "get_orders";
        if (lower.contains("categories") && lower.contains("available"))         return "get_categories";

        // 3. Fallback: map user intent to likely tool
        String intent = detectIntent(userMessage);
        return switch (intent) {
            case "SEARCH"   -> "search_products";
            case "CART"     -> userMessage.toLowerCase().contains("add") ? "add_to_cart" : "get_cart";
            case "CHECKOUT" -> "checkout";
            case "ORDERS"   -> "get_orders";
            default         -> "none";
        };
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    // ── Response parser ──────────────────────────────────────────────────────
    private ParsedResponse parse(String raw) {
        if (raw == null || raw.isBlank()) return new ParsedResponse("Please try again.", List.of());

        String s = raw.trim();
        // Strip markdown code fences
        if (s.startsWith("```")) {
            s = s.replaceAll("(?s)^```(?:json)?\\s*", "").replaceAll("```\\s*$", "").trim();
        }
        // Try JSON parse
        if (s.startsWith("{")) {
            try {
                JsonNode root = objectMapper.readTree(s);
                String msg = root.has("message") ? root.get("message").asText() : s;
                List<ProductDto> products = root.has("products") && root.get("products").isArray()
                        ? parseProducts(root.get("products")) : List.of();
                return new ParsedResponse(msg, products);
            } catch (Exception e) {
                log.debug("Not JSON, treating as plain text");
            }
        }
        return new ParsedResponse(s, List.of());
    }

    private List<ProductDto> parseProducts(JsonNode arr) {
        List<ProductDto> list = new ArrayList<>();
        for (JsonNode n : arr) {
            try {
                list.add(new ProductDto(
                        text(n, "id"), text(n, "name"), text(n, "description"),
                        bd(n, "price"), text(n, "category"), text(n, "imageUrl"),
                        n.has("stock") ? n.get("stock").asInt(0) : 0,
                        n.has("rating") ? n.get("rating").asDouble(0) : 0.0,
                        text(n, "brand"), text(n, "tags")
                ));
            } catch (Exception e) { log.warn("Skipping malformed product node"); }
        }
        return list;
    }

    private record ParsedResponse(String message, List<ProductDto> products) {}

    // ── Helpers ──────────────────────────────────────────────────────────────
    private String text(JsonNode n, String f) { return n.has(f) && !n.get(f).isNull() ? n.get(f).asText() : null; }
    private BigDecimal bd(JsonNode n, String f) { return n.has(f) ? BigDecimal.valueOf(n.get(f).asDouble()) : BigDecimal.ZERO; }

    private boolean isTimeoutError(Throwable t) {
        while (t != null) {
            if (t instanceof java.util.concurrent.TimeoutException) return true;
            String m = t.getMessage();
            if (m != null && (m.contains("timeout") || m.contains("TimeoutException"))) return true;
            t = t.getCause();
        }
        return false;
    }

    private String getRootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }

    private String detectIntent(String msg) {
        String l = msg.toLowerCase();
        if (l.contains("checkout") || l.contains("buy") || l.contains("purchase")) return "CHECKOUT";
        if (l.contains("cart") || l.contains("add")) return "CART";
        if (l.contains("search") || l.contains("find") || l.contains("show") || l.contains("recommend")) return "SEARCH";
        if (l.contains("order")) return "ORDERS";
        return "GENERAL";
    }

    public List<ChatMessage> getHistory(String sessionId) {
        return sessions.getOrDefault(sessionId, List.of()).stream()
                .map(m -> new ChatMessage(m instanceof UserMessage ? "user" : "assistant", m.getText(), LocalDateTime.now()))
                .toList();
    }

    public void clearSession(String sessionId) { sessions.remove(sessionId); }

    // ── Card management ──────────────────────────────────────────────────────
    public CardDto addCard(String userId, AddCardRequest req) {
        String clean = req.cardNumber().replaceAll("\\s", "");
        boolean hasDefault = cardRepository.existsByUserIdAndIsDefaultTrue(userId);
        return mapCard(cardRepository.save(AgentCard.builder()
                .userId(userId).last4(clean.substring(clean.length() - 4))
                .brand(brand(clean)).expiryMonth(req.expiryMonth())
                .expiryYear(req.expiryYear()).cardholderName(req.cardholderName())
                .isDefault(!hasDefault).build()));
    }

    public List<CardDto> getUserCards(String userId) {
        return cardRepository.findByUserId(userId).stream().map(this::mapCard).toList();
    }

    public void deleteCard(String userId, String cardId) {
        AgentCard c = cardRepository.findById(cardId)
                .filter(card -> card.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Card not found"));
        cardRepository.delete(c);
    }

    public CardDto setDefaultCard(String userId, String cardId) {
        cardRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(c -> { c.setDefault(false); cardRepository.save(c); });
        AgentCard c = cardRepository.findById(cardId)
                .filter(card -> card.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Card not found"));
        c.setDefault(true);
        return mapCard(cardRepository.save(c));
    }

    private String brand(String n) {
        if (n.startsWith("4")) return "Visa";
        if (n.startsWith("5")) return "Mastercard";
        if (n.startsWith("3")) return "Amex";
        return "Unknown";
    }

    private CardDto mapCard(AgentCard c) {
        return new CardDto(c.getId(), c.getLast4(), c.getBrand(),
                c.getExpiryMonth(), c.getExpiryYear(), c.getCardholderName(), c.isDefault());
    }
}
