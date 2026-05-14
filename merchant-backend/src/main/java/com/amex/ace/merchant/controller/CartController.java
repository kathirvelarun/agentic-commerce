package com.amex.ace.merchant.controller;

import com.amex.ace.merchant.dto.MerchantDtos.*;
import com.amex.ace.merchant.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Cart endpoints — /api/cart/*.
 * Maps Python reference-merchant-backend/app/routes/cart.py.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** POST /api/cart/ — create a new cart. */
    @PostMapping("/")
    public CartResponse createCart() {
        return cartService.createCart();
    }

    /** GET /api/cart/{sessionId} — get cart by session ID. */
    @GetMapping("/{sessionId}")
    public CartResponse getCart(@PathVariable String sessionId) {
        return cartService.getCart(sessionId);
    }

    /** POST /api/cart/{sessionId}/items — add item to cart. */
    @PostMapping("/{sessionId}/items")
    public CartResponse addItem(@PathVariable String sessionId,
                                @Valid @RequestBody CartItemCreate req) {
        return cartService.addItem(sessionId, req);
    }

    /** PUT /api/cart/{sessionId}/items/{productId} — update item quantity. */
    @PutMapping("/{sessionId}/items/{productId}")
    public CartResponse updateItem(@PathVariable String sessionId,
                                   @PathVariable Long productId,
                                   @Valid @RequestBody CartItemUpdate req) {
        return cartService.updateItem(sessionId, productId, req);
    }

    /** DELETE /api/cart/{sessionId}/items/{productId} — remove item. */
    @DeleteMapping("/{sessionId}/items/{productId}")
    public MessageResponse removeItem(@PathVariable String sessionId,
                                      @PathVariable Long productId) {
        return cartService.removeItem(sessionId, productId);
    }

    /** DELETE /api/cart/{sessionId} — clear cart. */
    @DeleteMapping("/{sessionId}")
    public MessageResponse clearCart(@PathVariable String sessionId) {
        return cartService.clearCart(sessionId);
    }

    /** POST /api/cart/{sessionId}/checkout — checkout and create order. */
    @PostMapping("/{sessionId}/checkout")
    public Map<String, Object> checkout(@PathVariable String sessionId,
                                        @RequestBody Map<String, Object> checkoutData) {
        return cartService.checkout(sessionId, checkoutData);
    }
}
