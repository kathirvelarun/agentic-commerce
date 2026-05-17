package com.commerce.merchant.service;

import com.commerce.merchant.dto.MerchantDtos.*;
import com.commerce.merchant.model.*;
import com.commerce.merchant.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CartOrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    // ========== CART ==========

    public CartResponse getOrCreateCart(String userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().userId(userId).status("ACTIVE").build()
                ));
        return toCartResponse(cart);
    }

    public CartResponse addToCart(String userId, AddToCartRequest request) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().userId(userId).status("ACTIVE").build()
                ));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.productId()));

        if (product.getStock() < request.quantity()) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        // Check if product already in cart
        cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.productId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.quantity()),
                        () -> cart.getItems().add(CartItem.builder()
                                .cart(cart)
                                .productId(product.getId())
                                .productName(product.getName())
                                .quantity(request.quantity())
                                .price(product.getPrice())
                                .imageUrl(product.getImageUrl())
                                .build())
                );

        return toCartResponse(cartRepository.save(cart));
    }

    public CartResponse updateCartItem(String userId, String itemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("No active cart"));

        cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    if (request.quantity() <= 0) {
                        cart.getItems().remove(item);
                    } else {
                        item.setQuantity(request.quantity());
                    }
                });

        return toCartResponse(cartRepository.save(cart));
    }

    public CartResponse removeFromCart(String userId, String itemId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("No active cart"));

        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        return toCartResponse(cartRepository.save(cart));
    }

    public CartResponse clearCart(String userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("No active cart"));
        cart.getItems().clear();
        return toCartResponse(cartRepository.save(cart));
    }

    // ========== CHECKOUT ==========

    public OrderResponse checkout(String userId, String cartId, CheckoutRequest request) {
        Cart cart = cartRepository.findById(cartId)
                .filter(c -> c.getUserId().equals(userId) && c.getStatus().equals("ACTIVE"))
                .orElseThrow(() -> new RuntimeException("Cart not found or not active"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Serialize items to JSON for order
        String itemsJson;
        try {
            itemsJson = objectMapper.writeValueAsString(
                    cart.getItems().stream().map(i -> new CartItemResponse(
                            i.getId(), i.getProductId(), i.getProductName(),
                            i.getQuantity(), i.getPrice(),
                            i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())),
                            i.getImageUrl()
                    )).toList()
            );
        } catch (JsonProcessingException e) {
            itemsJson = "[]";
        }

        // Create order
        Order order = Order.builder()
                .userId(userId)
                .cartId(cartId)
                .itemsJson(itemsJson)
                .total(cart.getTotal())
                .cardLast4(request.cardLast4())
                .cardBrand(request.cardBrand())
                .shippingAddress(request.shippingAddress())
                .build();

        Order saved = orderRepository.save(order);

        // Mark cart as checked out
        cart.setStatus("CHECKED_OUT");
        cartRepository.save(cart);

        return toOrderResponse(saved, cart);
    }

    public List<OrderResponse> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(o -> {
                    Cart cart = cartRepository.findById(o.getCartId()).orElse(null);
                    return toOrderResponse(o, cart);
                })
                .toList();
    }

    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        Cart cart = cartRepository.findById(order.getCartId()).orElse(null);
        return toOrderResponse(order, cart);
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getId(), i.getProductId(), i.getProductName(),
                        i.getQuantity(), i.getPrice(),
                        i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())),
                        i.getImageUrl()
                )).toList();
        return new CartResponse(
                cart.getId(), cart.getUserId(), items,
                cart.getTotal(), items.size(), cart.getStatus()
        );
    }

    private OrderResponse toOrderResponse(Order order, Cart cart) {
        List<CartItemResponse> items = cart != null ? cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getId(), i.getProductId(), i.getProductName(),
                        i.getQuantity(), i.getPrice(),
                        i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())),
                        i.getImageUrl()
                )).toList() : List.of();

        return new OrderResponse(
                order.getId(), order.getUserId(), order.getCartId(),
                items, order.getTotal(), order.getStatus(),
                order.getCardLast4(), order.getCardBrand(), order.getCreatedAt()
        );
    }
}
