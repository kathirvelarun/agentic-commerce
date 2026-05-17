package com.commerce.mcp.tool;

import com.commerce.mcp.service.MerchantClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MerchantTools {

    private final MerchantClientService merchantClient;

    @Tool(name = "search_products",
          description = "Search for products in the merchant catalog. Returns a list of matching products with id, name, price, description, category, and rating. Use this to help users find products.")
    public String searchProducts(
            @ToolParam(description = "Search query to find products by name, description, or brand") String query,
            @ToolParam(description = "Optional category filter: Electronics, Clothing, Kitchen, Sports, Books, Footwear, Home") String category) {
        log.info("MCP tool: search_products query={} category={}", query, category);
        return merchantClient.searchProducts(query, category);
    }

    @Tool(name = "get_product_details",
          description = "Get detailed information about a specific product by its ID, including full description, stock availability, and pricing.")
    public String getProductDetails(
            @ToolParam(description = "The unique product ID to retrieve details for") String productId) {
        log.info("MCP tool: get_product_details productId={}", productId);
        return merchantClient.getProduct(productId);
    }

    @Tool(name = "get_categories",
          description = "Get all available product categories in the merchant catalog.")
    public String getCategories() {
        log.info("MCP tool: get_categories");
        return merchantClient.getCategories();
    }

    @Tool(name = "get_cart",
          description = "Retrieve the current shopping cart for a user, including all items, quantities, prices, and total.")
    public String getCart(
            @ToolParam(description = "The user ID whose cart to retrieve") String userId) {
        log.info("MCP tool: get_cart userId={}", userId);
        return merchantClient.getCart(userId);
    }

    @Tool(name = "add_to_cart",
          description = "Add a product to the user's shopping cart. Returns the updated cart with all items and new total.")
    public String addToCart(
            @ToolParam(description = "The user ID to add the item for") String userId,
            @ToolParam(description = "The product ID to add to cart") String productId,
            @ToolParam(description = "The quantity to add (must be positive integer)") int quantity) {
        log.info("MCP tool: add_to_cart userId={} productId={} quantity={}", userId, productId, quantity);
        return merchantClient.addToCart(userId, productId, quantity);
    }

    @Tool(name = "update_cart_item",
          description = "Update the quantity of a specific item in the user's cart. Set quantity to 0 to remove the item.")
    public String updateCartItem(
            @ToolParam(description = "The user ID whose cart to update") String userId,
            @ToolParam(description = "The cart item ID to update (from get_cart response)") String itemId,
            @ToolParam(description = "New quantity (0 to remove the item)") int quantity) {
        log.info("MCP tool: update_cart_item userId={} itemId={} quantity={}", userId, itemId, quantity);
        return merchantClient.updateCartItem(userId, itemId, quantity);
    }

    @Tool(name = "remove_from_cart",
          description = "Remove a specific item from the user's cart.")
    public String removeFromCart(
            @ToolParam(description = "The user ID whose cart to modify") String userId,
            @ToolParam(description = "The cart item ID to remove") String itemId) {
        log.info("MCP tool: remove_from_cart userId={} itemId={}", userId, itemId);
        return merchantClient.removeFromCart(userId, itemId);
    }

    @Tool(name = "checkout",
          description = "Process checkout for the user's cart using a payment card. Creates an order and clears the cart. Returns order confirmation with order ID.")
    public String checkout(
            @ToolParam(description = "The user ID placing the order") String userId,
            @ToolParam(description = "The cart ID to checkout (from get_cart response)") String cartId,
            @ToolParam(description = "Last 4 digits of the payment card") String cardLast4,
            @ToolParam(description = "Card brand (Visa, Mastercard, Amex)") String cardBrand) {
        log.info("MCP tool: checkout userId={} cartId={} card=****{}", userId, cartId, cardLast4);
        return merchantClient.checkout(userId, cartId, cardLast4, cardBrand);
    }

    @Tool(name = "get_orders",
          description = "Retrieve order history for a user, showing all past orders with status and items.")
    public String getOrders(
            @ToolParam(description = "The user ID whose order history to retrieve") String userId) {
        log.info("MCP tool: get_orders userId={}", userId);
        return merchantClient.getUserOrders(userId);
    }
}
