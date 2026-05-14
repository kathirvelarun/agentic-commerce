package com.amex.ace.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// ─── Product ──────────────────────────────────────────────────────────────────

public sealed interface MerchantDtos permits
        MerchantDtos.ProductResponse,
        MerchantDtos.ProductListResponse,
        MerchantDtos.CartItemCreate,
        MerchantDtos.CartItemUpdate,
        MerchantDtos.CartItemResponse,
        MerchantDtos.CartResponse,
        MerchantDtos.OrderItemResponse,
        MerchantDtos.OrderResponse,
        MerchantDtos.OrderListResponse,
        MerchantDtos.MessageResponse {

    record ProductResponse(
            Long id,
            String name,
            String description,
            Double price,
            String category,
            String imageUrl,
            Integer stockQuantity,
            LocalDateTime createdAt
    ) implements MerchantDtos {}

    record ProductListResponse(
            List<ProductResponse> products,
            long total,
            int limit,
            int offset
    ) implements MerchantDtos {}

    // ─── Cart ─────────────────────────────────────────────────────────────────

    record CartItemCreate(
            @NotNull Long productId,
            @Min(1)  int quantity
    ) implements MerchantDtos {}

    record CartItemUpdate(
            @Min(0) int quantity
    ) implements MerchantDtos {}

    record CartItemResponse(
            Long id,
            Long productId,
            String productName,
            Double price,
            Integer quantity,
            Double total
    ) implements MerchantDtos {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record CartResponse(
            Long id,
            String sessionId,
            List<CartItemResponse> items,
            Double subtotal,
            Double tax,
            Double shipping,
            Double totalAmount
    ) implements MerchantDtos {}

    // ─── Order ────────────────────────────────────────────────────────────────

    record OrderItemResponse(
            Long id,
            Long productId,
            String productName,
            Integer quantity,
            Double price,
            Double unitPrice,
            Double totalPrice,
            Map<String, Object> product
    ) implements MerchantDtos {}

    record OrderResponse(
            Long id,
            String orderNumber,
            String customerName,
            String customerEmail,
            Double totalAmount,
            String status,
            List<OrderItemResponse> items,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) implements MerchantDtos {}

    record OrderListResponse(
            List<OrderResponse> orders,
            long total
    ) implements MerchantDtos {}

    // ─── Generic ──────────────────────────────────────────────────────────────

    record MessageResponse(String message) implements MerchantDtos {}
}
