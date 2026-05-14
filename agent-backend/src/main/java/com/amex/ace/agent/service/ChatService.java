package com.amex.ace.agent.service;

import com.amex.ace.agent.dto.chat.ChatResponse;
import com.amex.ace.agent.dto.chat.OrderSummary;
import com.amex.ace.agent.dto.chat.Product;
import com.amex.ace.agent.dto.chat.ProductInput;
import com.amex.ace.agent.dto.chat.*;
import com.amex.ace.agent.dto.commerce.AgenticCheckoutResponse;
import com.amex.ace.agent.dto.commerce.Credentials;
import com.amex.ace.agent.dto.commerce.PurchaseSummary;
import com.amex.ace.agent.repository.CardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Chat service — orchestrates user messages through the AI agent.
 * Direct translation of Python src/services/chat.py ChatService.
 */
@Service
@Slf4j
public class ChatService {

    private final AgentService agentService;
    private final CardRepository cardRepository;

    @Autowired
    public ChatService(AgentService agentService, CardRepository cardRepository) {
        this.agentService     = agentService;
        this.cardRepository   = cardRepository;
    }

    /**
     * Processes a user chat message through the AI agent.
     * Equivalent to Python: async def process_message(message, products)
     */
    public ChatResponse processMessage(String message, List<ProductInput> products) {
        try {
            String formattedMessage = """
                    User Message: %s
                    Selected Products: %s
                    """.formatted(message, products != null ? products.toString() : "[]");

            Map<String, Object> responseJson = agentService.sendUserMessage(formattedMessage);
            Map<?, ?> orderSummaryMap = (Map<?, ?>) responseJson.get("order_summary");

            if (orderSummaryMap != null) {
                // Check for active cards before presenting order summary
                boolean hasActiveCards = !cardRepository.findByStatus("ACTIVE").isEmpty();
                /*if (!hasActiveCards) {
                    return new ChatResponse(
                            "You have no active cards to complete the purchase. "
                            + "Please add a card and try again.");
                }*/
            }

            OrderSummary orderSummary = null;
            if (orderSummaryMap != null) {
                orderSummary = new OrderSummary(
                        (String) orderSummaryMap.get("merchant_name"),
                        (String) orderSummaryMap.get("overall_amount"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawProducts =
                    (List<Map<String, Object>>) responseJson.get("products");
            List<Product> products2 = rawProducts == null ? List.of() : rawProducts.stream()
                    .map(p -> new Product(
                            (String) p.get("name"),
                            String.valueOf(p.get("price")),
                            (String) p.get("image"),
                            (String) p.get("sku"),
                            (String) p.get("description")))
                    .toList();

            return new ChatResponse(
                    (String) responseJson.get("message"),
                    products2,
                    orderSummary);

        } catch (Exception e) {
            log.error("Error processing message", e);
            return new ChatResponse(
                    "An error occurred while processing your message. Please try again later.");
        }
    }

    /**
     * Completes checkout by sending a system message "COMPLETE CHECKOUT" to the agent.
     * Equivalent to Python: async def complete_checkout(credentials)
     */
    public AgenticCheckoutResponse completeCheckout(Credentials credentials) {
        try {
            agentService.storeCredentials(credentials);
            Map<String, Object> responseJson = agentService.sendSystemMessage("COMPLETE CHECKOUT");

            PurchaseSummary purchaseSummary = null;
            @SuppressWarnings("unchecked")
            Map<String, Object> psMap = (Map<String, Object>) responseJson.get("purchase_summary");
            if (psMap != null) {
                purchaseSummary = new PurchaseSummary(
                        (String) psMap.get("merchant"),
                        (String) psMap.get("overall_amount"),
                        (String) psMap.get("order_id"),
                        (String) psMap.get("tracking_code"));
            }
            return new AgenticCheckoutResponse(
                    (String) responseJson.get("message"),
                    purchaseSummary);

        } catch (Exception e) {
            log.error("Error completing checkout", e);
            throw new RuntimeException(
                    "An error occurred while completing the checkout. Please try again later.", e);
        } finally {
            agentService.clearCredentials();
        }
    }
}
