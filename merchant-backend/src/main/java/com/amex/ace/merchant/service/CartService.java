package com.amex.ace.merchant.service;

import com.amex.ace.merchant.dto.MerchantDtos.*;
import com.amex.ace.merchant.entity.*;
import com.amex.ace.merchant.repository.*;
import com.amex.ace.merchant.entity.*;
import com.amex.ace.merchant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Cart and checkout service.
 * Translates Python reference-merchant-backend/app/routes/cart.py.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private static final double TAX_RATE = 0.08;
    private static final double SHIPPING = 9.99;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    // ─── Create cart ─────────────────────────────────────────────────────────

    @Transactional
    public CartResponse createCart() {
        Cart cart = cartRepository.save(Cart.builder()
                .sessionId(UUID.randomUUID().toString())
                .build());
        return toCartResponse(cart);
    }

    // ─── Get cart ─────────────────────────────────────────────────────────────

    public CartResponse getCart(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cart not found"));
        return toCartResponse(cart);
    }

    // ─── Add item ─────────────────────────────────────────────────────────────

    @Transactional
    public CartResponse addItem(String sessionId, CartItemCreate req) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().sessionId(sessionId).build()));

        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        Optional<CartItem> existing = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + req.quantity());
            cartItemRepository.save(item);
        } else {
            cartItemRepository.save(CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(req.quantity())
                    .build());
        }

        // Reload to get fresh items list
        Cart refreshed = cartRepository.findBySessionId(sessionId).orElseThrow();
        return toCartResponse(refreshed);
    }

    // ─── Update item ──────────────────────────────────────────────────────────

    @Transactional
    public CartResponse updateItem(String sessionId, Long productId, CartItemUpdate req) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item not found in cart"));

        if (req.quantity() <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(req.quantity());
            cartItemRepository.save(item);
        }

        Cart refreshed = cartRepository.findBySessionId(sessionId).orElseThrow();
        return toCartResponse(refreshed);
    }

    // ─── Remove item ──────────────────────────────────────────────────────────

    @Transactional
    public MessageResponse removeItem(String sessionId, Long productId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item not found in cart"));

        cartItemRepository.delete(item);
        return new MessageResponse("Item removed from cart successfully");
    }

    // ─── Clear cart ───────────────────────────────────────────────────────────

    @Transactional
    public MessageResponse clearCart(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cart not found"));
        cart.getItems().clear();
        cartRepository.save(cart);
        return new MessageResponse("Cart cleared successfully");
    }

    // ─── Checkout ─────────────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> checkout(String sessionId, Map<String, Object> checkoutData) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        // Calculate totals
        double subtotal = cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity()).sum();
        double tax      = Math.round(subtotal * TAX_RATE * 100.0) / 100.0;
        double shipping = SHIPPING;
        double total    = Math.round((subtotal + tax + shipping) * 100.0) / 100.0;

        String customerEmail = (String) checkoutData.get("customer_email");
        String customerName  = (String) checkoutData.get("customer_name");

        if (customerEmail == null || customerName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Customer email and name are required");
        }

        // Extract payment data
        String cardNumber  = (String) checkoutData.get("card_number");
        String expiryDate  = (String) checkoutData.get("expiry_date");
        String cvv         = (String) checkoutData.get("cvv");

        // Fall back to mock card if no payment data provided (demo / MCP agent path)
        if (cardNumber == null && expiryDate == null && cvv == null) {
            cardNumber = "4111111111111111";
            expiryDate = "12/25";
            cvv        = "123";
        }

        // Process payment
        Map<String, Object> paymentResult = processPayment(cardNumber, expiryDate, cvv);
        if (!(Boolean) paymentResult.get("success")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment failed: " + paymentResult.get("error"));
        }

        // Build shipping address string
        Object rawAddr = checkoutData.get("shipping_address");
        String shippingAddr;
        if (rawAddr instanceof String s) {
            shippingAddr = s;
        } else if (rawAddr instanceof Map<?, ?> m) {
            shippingAddr = "%s, %s, %s %s, %s".formatted(
                    m.get("street"), m.get("city"), m.get("state"),
                    m.get("zip"), m.get("country"));
        } else {
            shippingAddr = "No shipping address provided";
        }

        // Determine payment method
        Object pmRaw = checkoutData.get("payment_method");
        String paymentMethod = pmRaw instanceof Map<?, ?> pmMap
                ? (String) pmMap.get("type") : (pmRaw != null ? pmRaw.toString() : "credit_card");

        // Create order
        Order order = orderRepository.save(Order.builder()
                .orderNumber(generateOrderNumber())
                .customerEmail(customerEmail)
                .customerName(customerName)
                .totalAmount(total)
                .status("confirmed")
                .shippingAddress(shippingAddr)
                .phone((String) checkoutData.get("customer_phone"))
                .specialInstructions((String) checkoutData.get("special_instructions"))
                .billingAddress(shippingAddr)
                .paymentMethod(paymentMethod)
                .billingDifferent(Boolean.TRUE.equals(checkoutData.get("billing_different")))
                .cardLastFour((String) paymentResult.get("lastFour"))
                .cardBrand((String) paymentResult.get("cardBrand"))
                .paymentStatus("processed")
                .build());

        // Create order items and clear cart
        List<Map<String, Object>> orderItemsList = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = orderItemRepository.save(OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .quantity(ci.getQuantity())
                    .price(ci.getProduct().getPrice())
                    .build());

            Product p = ci.getProduct();
            orderItemsList.add(Map.of(
                    "id",           oi.getId(),
                    "product_id",   p.getId(),
                    "product_name", p.getName(),
                    "quantity",     ci.getQuantity(),
                    "price",        p.getPrice(),
                    "unit_price",   p.getPrice(),
                    "total_price",  p.getPrice() * ci.getQuantity(),
                    "product",      Map.of(
                            "id",          p.getId(),
                            "name",        p.getName(),
                            "price",       p.getPrice(),
                            "image_url",   p.getImageUrl() != null ? p.getImageUrl() : "",
                            "description", p.getDescription() != null ? p.getDescription() : "")));
        }

        // Clear the cart
        cart.getItems().clear();
        cartRepository.save(cart);

        String trackingNumber = "TRK" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 10).toUpperCase();

        return Map.of(
                "message", "Order created and payment processed successfully",
                "order", Map.ofEntries(
                        Map.entry("id", order.getId()),
                        Map.entry("order_number", order.getOrderNumber()),
                        Map.entry("customer_name", order.getCustomerName()),
                        Map.entry("customer_email", order.getCustomerEmail()),
                        Map.entry("total_amount", order.getTotalAmount()),
                        Map.entry("subtotal", subtotal),
                        Map.entry("tax_amount", tax),
                        Map.entry("shipping_cost", shipping),
                        Map.entry("status", order.getStatus()),
                        Map.entry("created_at", order.getCreatedAt().toString()),
                        Map.entry("items", orderItemsList)),
                "payment", Map.of(
                        "method",          paymentMethod,
                        "transaction_id",  paymentResult.get("transactionId"),
                        "amount_charged",  order.getTotalAmount(),
                        "status",          "processed",
                        "card_brand",      order.getCardBrand(),
                        "last_four",       order.getCardLastFour()),
                "fulfillment", Map.of(
                        "tracking_number",    trackingNumber,
                        "shipping_carrier",   "Standard Shipping",
                        "estimated_delivery", "5-7 business days",
                        "status",             "processing"));
    }

    // ─── Payment helpers (mock) ───────────────────────────────────────────────

    private Map<String, Object> processPayment(String cardNumber, String expiryDate,
                                               String cvv) {
        if (!validateCardNumber(cardNumber)) {
            return Map.of("success", false, "error", "Invalid card number");
        }
        if (!validateExpiry(expiryDate)) {
            return Map.of("success", false, "error", "Invalid or expired card");
        }
        if (cvv == null || cvv.length() < 3 || cvv.length() > 4) {
            return Map.of("success", false, "error", "Invalid CVV");
        }
        String clean = cardNumber.replaceAll("\\D", "");
        return Map.of(
                "success",       true,
                "transactionId", "txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12),
                "cardBrand",     detectCardBrand(clean),
                "lastFour",      clean.substring(clean.length() - 4));
    }

    private String detectCardBrand(String digits) {
        if (digits.startsWith("4"))                                  return "Visa";
        if (digits.matches("^(51|52|53|54|55|2).*"))                 return "Mastercard";
        if (digits.startsWith("34") || digits.startsWith("37"))      return "American Express";
        if (digits.startsWith("6"))                                  return "Discover";
        return "Unknown";
    }

    private boolean validateCardNumber(String raw) {
        if (raw == null) return false;
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() < 13 || digits.length() > 19) return false;
        // Luhn check
        int sum = 0;
        boolean alt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alt) { n *= 2; if (n > 9) n -= 9; }
            sum += n;
            alt = !alt;
        }
        return sum % 10 == 0;
    }

    private boolean validateExpiry(String expiry) {
        if (expiry == null || !expiry.contains("/")) return false;
        try {
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0].trim());
            int year  = Integer.parseInt(parts[1].trim());
            if (year < 100) year += 2000;
            if (month < 1 || month > 12) return false;
            LocalDateTime now = LocalDateTime.now();
            return year > now.getYear()
                    || (year == now.getYear() && month >= now.getMonthValue());
        } catch (Exception e) {
            return false;
        }
    }

    private String generateOrderNumber() {
        String ts     = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD-" + ts + "-" + suffix;
    }

    // ─── Cart response builder ────────────────────────────────────────────────

    private CartResponse toCartResponse(Cart cart) {
        double subtotal = cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity()).sum();
        double tax      = Math.round(subtotal * TAX_RATE * 100.0) / 100.0;
        double total    = Math.round((subtotal + tax + SHIPPING) * 100.0) / 100.0;

        List<CartItemResponse> items = cart.getItems().stream().map(ci ->
                new CartItemResponse(
                        ci.getId(),
                        ci.getProduct().getId(),
                        ci.getProduct().getName(),
                        ci.getProduct().getPrice(),
                        ci.getQuantity(),
                        ci.getProduct().getPrice() * ci.getQuantity()))
                .toList();

        return new CartResponse(cart.getId(), cart.getSessionId(), items,
                subtotal, tax, SHIPPING, total);
    }
}
