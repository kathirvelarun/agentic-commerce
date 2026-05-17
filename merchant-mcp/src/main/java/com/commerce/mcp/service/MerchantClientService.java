package com.commerce.mcp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MerchantClientService {

    private final RestTemplate restTemplate;

    @Value("${merchant.backend.url}")
    private String merchantBackendUrl;

    public String searchProducts(String query, String category) {
        try {
            String url = merchantBackendUrl + "/api/v1/products?";
            if (query != null && !query.isBlank()) url += "query=" + query + "&";
            if (category != null && !category.isBlank()) url += "category=" + category;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error searching products", e);
            return "{\"error\": \"Failed to search products: " + e.getMessage() + "\"}";
        }
    }

    public String getProduct(String productId) {
        try {
            String url = merchantBackendUrl + "/api/v1/products/" + productId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting product {}", productId, e);
            return "{\"error\": \"Product not found: " + productId + "\"}";
        }
    }

    public String getCategories() {
        try {
            String url = merchantBackendUrl + "/api/v1/products/categories";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "[\"Electronics\", \"Clothing\", \"Kitchen\", \"Sports\", \"Books\"]";
        }
    }

    public String getCart(String userId) {
        try {
            String url = merchantBackendUrl + "/api/v1/users/" + userId + "/cart";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting cart for user {}", userId, e);
            return "{\"error\": \"Failed to get cart: " + e.getMessage() + "\"}";
        }
    }

    public String addToCart(String userId, String productId, int quantity) {
        try {
            String url = merchantBackendUrl + "/api/v1/users/" + userId + "/cart/items";
            Map<String, Object> body = Map.of("productId", productId, "quantity", quantity);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, jsonHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error adding to cart", e);
            return "{\"error\": \"Failed to add to cart: " + e.getMessage() + "\"}";
        }
    }

    public String updateCartItem(String userId, String itemId, int quantity) {
        try {
            String url = merchantBackendUrl + "/api/v1/users/" + userId + "/cart/items/" + itemId;
            Map<String, Object> body = Map.of("quantity", quantity);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, jsonHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "{\"error\": \"Failed to update cart: " + e.getMessage() + "\"}";
        }
    }

    public String removeFromCart(String userId, String itemId) {
        try {
            String url = merchantBackendUrl + "/api/v1/users/" + userId + "/cart/items/" + itemId;
            restTemplate.delete(url);
            return "{\"message\": \"Item removed from cart\"}";
        } catch (Exception e) {
            return "{\"error\": \"Failed to remove from cart: " + e.getMessage() + "\"}";
        }
    }

    public String checkout(String userId, String cartId, String cardLast4, String cardBrand) {
        try {
            String url = merchantBackendUrl + "/api/v1/users/" + userId + "/cart/" + cartId + "/checkout";
            Map<String, Object> body = Map.of(
                "cardLast4", cardLast4,
                "cardBrand", cardBrand,
                "shippingAddress", "Default Shipping Address"
            );
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, jsonHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error processing checkout", e);
            return "{\"error\": \"Checkout failed: " + e.getMessage() + "\"}";
        }
    }

    public String getUserOrders(String userId) {
        try {
            String url = merchantBackendUrl + "/api/v1/users/" + userId + "/orders";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "[]";
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
