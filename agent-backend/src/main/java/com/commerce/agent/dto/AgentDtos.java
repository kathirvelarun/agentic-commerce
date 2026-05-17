package com.commerce.agent.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AgentDtos {

    // Chat DTOs
    public record ChatRequest(
        String sessionId,
        String message,
        String userId
    ) {}

    public record ChatResponse(
        String sessionId,
        String message,
        String role,
        LocalDateTime timestamp,
        List<ProductDto> products,
        CartDto cart,
        OrderDto order,
        String intent
    ) {}

    public record ChatMessage(
        String role,
        String content,
        LocalDateTime timestamp
    ) {}

    // Card DTOs
    public record CardDto(
        String id,
        String last4,
        String brand,
        String expiryMonth,
        String expiryYear,
        String cardholderName,
        boolean isDefault
    ) {}

    public record AddCardRequest(
        String cardNumber,
        String expiryMonth,
        String expiryYear,
        String cvv,
        String cardholderName
    ) {}

    // Product DTOs
    public record ProductDto(
        String id,
        String name,
        String description,
        BigDecimal price,
        String category,
        String imageUrl,
        int stock,
        double rating,
        String brand,
        String tags
    ) {}

    // Cart DTOs
    public record CartDto(
        String cartId,
        String userId,
        List<CartItemDto> items,
        BigDecimal total,
        int itemCount
    ) {}

    public record CartItemDto(
        String productId,
        String productName,
        int quantity,
        BigDecimal price,
        BigDecimal subtotal,
        String imageUrl
    ) {}

    public record AddToCartRequest(
        String userId,
        String productId,
        int quantity
    ) {}

    // Order DTOs
    public record OrderDto(
        String orderId,
        String userId,
        List<CartItemDto> items,
        BigDecimal total,
        String status,
        String cardLast4,
        LocalDateTime createdAt
    ) {}

    public record CheckoutRequest(
        String userId,
        String cartId,
        String cardId
    ) {}

    // Flow Log DTOs
    public record FlowLogDto(
        String id,
        String sessionId,
        String requestId,
        int stepNumber,
        String stepLabel,
        String status,
        String detail,
        Long durationMs,
        LocalDateTime createdAt
    ) {}

    // Agent State
    public record AgentState(
        String sessionId,
        String userId,
        List<ChatMessage> history,
        String currentIntent,
        ProductDto[] discoveredProducts,
        CartDto currentCart
    ) {}
}
