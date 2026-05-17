package com.commerce.merchant.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MerchantDtos {

    // Product
    public record ProductResponse(
            String id, String name, String description, BigDecimal price,
            String category, String imageUrl, Integer stock, Double rating,
            String brand, String tags
    ) {}

    public record ProductRequest(
            String name, String description, BigDecimal price,
            String category, String imageUrl, Integer stock,
            String brand, String tags
    ) {}

    // Cart
    public record CartResponse(
            String id, String userId, List<CartItemResponse> items,
            BigDecimal total, int itemCount, String status
    ) {}

    public record CartItemResponse(
            String id, String productId, String productName,
            Integer quantity, BigDecimal price, BigDecimal subtotal, String imageUrl
    ) {}

    public record AddToCartRequest(String productId, Integer quantity) {}

    public record UpdateCartItemRequest(Integer quantity) {}

    // Order
    public record OrderResponse(
            String id, String userId, String cartId,
            List<CartItemResponse> items, BigDecimal total,
            String status, String cardLast4, String cardBrand,
            LocalDateTime createdAt
    ) {}

    public record CheckoutRequest(
            String cardLast4, String cardBrand, String shippingAddress
    ) {}

    // Search
    public record ProductSearchResponse(
            List<ProductResponse> products, int total, String query
    ) {}
}
