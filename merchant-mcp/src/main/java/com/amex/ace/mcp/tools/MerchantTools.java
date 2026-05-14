package com.amex.ace.mcp.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * All 6 MCP tool implementations.
 *
 * Each @Tool method maps directly to one TypeScript tool in the Node.js
 * reference-merchant-mcp/src/index.ts McpServer.
 *
 * Spring AI discovers these automatically via component scanning
 * and registers them with the MCP server.
 */
@Service
@Slf4j
public class MerchantTools {

    private final RestClient api;

    @Autowired
    public MerchantTools(RestClient merchantApiClient) {
        this.api = merchantApiClient;
    }

    // =========================================================================
    // Tool 1: get_categories
    // =========================================================================

    @Tool(name = "get_categories",
          description = "Get a list of all available product categories in the catalog")
    public Map<String, Object> getCategories() {
        log.debug("[MCP] get_categories called");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = api.get()
                    .uri("/products/categories")
                    .retrieve()
                    .body(Map.class);
            return resp != null ? resp : Map.of("categories", new Object[0]);
        } catch (Exception e) {
            log.error("get_categories failed", e);
            return Map.of("error", "Error getting categories: " + e.getMessage());
        }
    }

    // =========================================================================
    // Tool 2: search_catalog
    // =========================================================================

    @Tool(name = "search_catalog",
          description = "Search the product catalog with optional filters for category, "
                  + "price range, and search query")
    public Map<String, Object> searchCatalog(
            @ToolParam(description = "Search query for product name or description",
                       required = false) String query,
            @ToolParam(description = "Filter by product category",
                       required = false) String category,
            @ToolParam(description = "Minimum price filter",
                       required = false) Double minPrice,
            @ToolParam(description = "Maximum price filter",
                       required = false) Double maxPrice,
            @ToolParam(description = "Number of products to return (default: 20)",
                       required = false) Integer limit,
            @ToolParam(description = "Number of products to skip (default: 0)",
                       required = false) Integer offset) {
        log.debug("[MCP] search_catalog called: query={} category={}", query, category);
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/products/");
            if (query    != null) uriBuilder.queryParam("query",     query);
            if (category != null) uriBuilder.queryParam("category",  category);
            if (minPrice != null) uriBuilder.queryParam("min_price", minPrice);
            if (maxPrice != null) uriBuilder.queryParam("max_price", maxPrice);
            uriBuilder.queryParam("limit",  limit  != null ? limit  : 20);
            uriBuilder.queryParam("offset", offset != null ? offset : 0);

            @SuppressWarnings("unchecked")
            Map<String, Object> resp = api.get()
                    .uri(uriBuilder.build().toUriString())
                    .retrieve()
                    .body(Map.class);
            return resp != null ? resp : Map.of("products", new Object[0], "total", 0);
        } catch (Exception e) {
            log.error("search_catalog failed", e);
            return Map.of("error", "Error searching catalog: " + e.getMessage());
        }
    }

    // =========================================================================
    // Tool 3: create_cart
    // =========================================================================

    @Tool(name = "create_cart",
          description = "Create a new shopping cart and return the session ID")
    public Map<String, Object> createCart() {
        log.debug("[MCP] create_cart called");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = api.post()
                    .uri("/cart/")
                    .retrieve()
                    .body(Map.class);
            if (resp == null) return Map.of("error", "Empty response from cart creation");
            return Map.of(
                    "session_id", resp.get("session_id"),
                    "cart_id",   resp.get("id"),
                    "message",   "Cart created successfully");
        } catch (Exception e) {
            log.error("create_cart failed", e);
            return Map.of("error", "Error creating cart: " + e.getMessage());
        }
    }

    // =========================================================================
    // Tool 4: get_cart
    // =========================================================================

    @Tool(name = "get_cart",
          description = "Get the contents of a shopping cart by session ID")
    public Map<String, Object> getCart(
            @ToolParam(description = "Cart session ID") String sessionId) {
        log.debug("[MCP] get_cart called: sessionId={}", sessionId);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> cart = api.get()
                    .uri("/cart/" + sessionId)
                    .retrieve()
                    .body(Map.class);
            if (cart == null) return Map.of("error", "Cart not found");
            return Map.of("cart", cart);
        } catch (Exception e) {
            log.error("get_cart failed", e);
            return Map.of("error", "Error getting cart: " + e.getMessage());
        }
    }

    // =========================================================================
    // Tool 5: add_item_to_cart
    // =========================================================================

    @Tool(name = "add_item_to_cart",
          description = "Add a product to the shopping cart")
    public Map<String, Object> addItemToCart(
            @ToolParam(description = "Cart session ID")           String  sessionId,
            @ToolParam(description = "Product ID to add to cart") Long    productId,
            @ToolParam(description = "Quantity of the product",
                       required = false)                          Integer quantity) {
        log.debug("[MCP] add_item_to_cart: session={} product={} qty={}",
                sessionId, productId, quantity);
        try {
            Map<String, Object> body = Map.of(
                    "product_id", productId,
                    "quantity",   quantity != null ? quantity : 1);

            @SuppressWarnings("unchecked")
            Map<String, Object> cart = api.post()
                    .uri("/cart/" + sessionId + "/items")
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (cart == null) return Map.of("error", "Failed to add item to cart");
            return Map.of(
                    "message", "Item added to cart successfully",
                    "cart",    cart);
        } catch (Exception e) {
            log.error("add_item_to_cart failed", e);
            return Map.of("error", "Error adding item to cart: " + e.getMessage());
        }
    }

    // =========================================================================
    // Tool 6: checkout_cart
    // =========================================================================

    /**
     * Checkout the cart.
     *
     * In the Node.js original, this tool calls mcpServer.server.elicitInput() to
     * request card credentials from the MCP client.  In Spring AI, elicitation is
     * driven by the client side — the agent-backend's ElicitationHandler intercepts
     * the elicitation request emitted by the Spring AI MCP framework and returns the
     * stored Credentials.  The tool itself just passes the payment information it
     * receives (via the @ToolParam paymentInfo map) to the merchant-backend checkout
     * endpoint, identical to the Node.js behaviour after elicitation resolves.
     */
    @Tool(name = "checkout_cart",
          description = "Checkout the cart and create an order. "
                  + "Payment information (card_number, expiry_date, cvv) will be "
                  + "elicited from the user.")
    public Map<String, Object> checkoutCart(
            @ToolParam(description = "Cart session ID")                 String              sessionId,
            @ToolParam(description = "Customer's full name")            String              customerName,
            @ToolParam(description = "Customer's email address")        String              customerEmail,
            @ToolParam(description = "Customer's phone number",
                       required = false)                                String              customerPhone,
            @ToolParam(description = "Shipping address",
                       required = false)                                String              shippingAddress,
            @ToolParam(description = "Payment info elicited from user (card_number, expiry_date, cvv)",
                       required = false)                                Map<String, Object> paymentInfo) {
        log.debug("[MCP] checkout_cart: session={} customer={}", sessionId, customerName);
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("customer_name",  customerName);
            body.put("customer_email", customerEmail);
            if (customerPhone   != null) body.put("customer_phone",    customerPhone);
            if (shippingAddress != null) body.put("shipping_address",  shippingAddress);

            // Merge in elicited payment info (card_number, expiry_date, cvv)
            if (paymentInfo != null) {
                body.putAll(paymentInfo);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> resp = api.post()
                    .uri("/cart/" + sessionId + "/checkout")
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (resp == null) return Map.of("error", "Empty response from checkout");
            return resp;
        } catch (Exception e) {
            log.error("checkout_cart failed", e);
            return Map.of("error", "Error during checkout: " + e.getMessage());
        }
    }
}
