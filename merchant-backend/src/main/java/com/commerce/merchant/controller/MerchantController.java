package com.commerce.merchant.controller;

import com.commerce.merchant.dto.MerchantDtos.*;
import com.commerce.merchant.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MerchantController {

    private final ProductService productService;
    private final CartOrderService cartOrderService;

    // ==================== PRODUCT CATALOG ====================

    /** GET /api/v1/products - List all products with optional search */
    @GetMapping("/products")
    public ResponseEntity<ProductSearchResponse> getProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(productService.searchProducts(query, ""));
    }

    /** GET /api/v1/products/{id} - Get product by ID */
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    /** GET /api/v1/products/categories - Get all categories */
    @GetMapping("/products/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productService.getCategories());
    }

    /** POST /api/v1/products - Create a new product */
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    // ==================== SHOPPING CART ====================

    /** GET /api/v1/users/{userId}/cart - Get or create active cart */
    @GetMapping("/users/{userId}/cart")
    public ResponseEntity<CartResponse> getCart(@PathVariable String userId) {
        return ResponseEntity.ok(cartOrderService.getOrCreateCart(userId));
    }

    /** POST /api/v1/users/{userId}/cart/items - Add item to cart */
    @PostMapping("/users/{userId}/cart/items")
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable String userId,
            @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartOrderService.addToCart(userId, request));
    }

    /** PUT /api/v1/users/{userId}/cart/items/{itemId} - Update cart item quantity */
    @PutMapping("/users/{userId}/cart/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable String userId,
            @PathVariable String itemId,
            @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartOrderService.updateCartItem(userId, itemId, request));
    }

    /** DELETE /api/v1/users/{userId}/cart/items/{itemId} - Remove item from cart */
    @DeleteMapping("/users/{userId}/cart/items/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable String userId,
            @PathVariable String itemId) {
        return ResponseEntity.ok(cartOrderService.removeFromCart(userId, itemId));
    }

    /** DELETE /api/v1/users/{userId}/cart - Clear entire cart */
    @DeleteMapping("/users/{userId}/cart")
    public ResponseEntity<CartResponse> clearCart(@PathVariable String userId) {
        return ResponseEntity.ok(cartOrderService.clearCart(userId));
    }

    // ==================== CHECKOUT & ORDERS ====================

    /** POST /api/v1/users/{userId}/cart/{cartId}/checkout - Process checkout */
    @PostMapping("/users/{userId}/cart/{cartId}/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @PathVariable String userId,
            @PathVariable String cartId,
            @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(cartOrderService.checkout(userId, cartId, request));
    }

    /** GET /api/v1/users/{userId}/orders - Get user order history */
    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable String userId) {
        return ResponseEntity.ok(cartOrderService.getUserOrders(userId));
    }

    /** GET /api/v1/orders/{orderId} - Get specific order */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(cartOrderService.getOrder(orderId));
    }

    // ==================== HEALTH ====================
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "merchant-backend", "port", 8001));
    }
}
