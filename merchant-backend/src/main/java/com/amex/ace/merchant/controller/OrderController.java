package com.amex.ace.merchant.controller;

import com.amex.ace.merchant.dto.MerchantDtos.*;
import com.amex.ace.merchant.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Order endpoints — /api/orders/*.
 * Maps Python reference-merchant-backend/app/routes/orders.py.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** GET /api/orders/?customer_email=&status=&limit=&offset= */
    @GetMapping("/")
    public OrderListResponse getOrders(
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0")  int offset) {
        return orderService.getOrders(customerEmail, status, limit, offset);
    }

    /** GET /api/orders/{id} */
    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    /** GET /api/orders/number/{orderNumber} */
    @GetMapping("/number/{orderNumber}")
    public OrderResponse getOrderByNumber(@PathVariable String orderNumber) {
        return orderService.getOrderByNumber(orderNumber);
    }

    /** PUT /api/orders/{id}/status?status= */
    @PutMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @RequestParam String status) {
        return orderService.updateStatus(id, status);
    }

    /** DELETE /api/orders/{id} — cancel order. */
    @DeleteMapping("/{id}")
    public MessageResponse cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }
}
