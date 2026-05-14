package com.amex.ace.merchant.service;

import com.amex.ace.merchant.dto.MerchantDtos.*;
import com.amex.ace.merchant.entity.Order;
import com.amex.ace.merchant.entity.Product;
import com.amex.ace.merchant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Order read/update service.
 * Translates Python reference-merchant-backend/app/routes/orders.py.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderListResponse getOrders(String customerEmail, String status,
                                       int limit, int offset) {
        var pageable = PageRequest.of(offset / limit, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> page;
        if (customerEmail != null && status != null) {
            page = orderRepository.findByCustomerEmailAndStatus(customerEmail, status, pageable);
        } else if (customerEmail != null) {
            page = orderRepository.findByCustomerEmail(customerEmail, pageable);
        } else if (status != null) {
            page = orderRepository.findByStatus(status, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }
        List<OrderResponse> orders = page.getContent().stream().map(this::toDto).toList();
        return new OrderListResponse(orders, page.getTotalElements());
    }

    public OrderResponse getOrder(Long id) {
        return orderRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        List<String> valid = List.of("pending", "confirmed", "shipped", "delivered", "cancelled");
        if (!valid.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status. Must be one of: " + String.join(", ", valid));
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));
        order.setStatus(status);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public MessageResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));
        if (!List.of("pending", "confirmed").contains(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order cannot be cancelled. Only pending or confirmed orders can be cancelled.");
        }
        order.setStatus("cancelled");
        orderRepository.save(order);
        return new MessageResponse("Order " + order.getOrderNumber()
                + " has been cancelled successfully");
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private OrderResponse toDto(Order o) {
        List<OrderItemResponse> items = o.getItems().stream().map(oi -> {
            Product p = oi.getProduct();
            return new OrderItemResponse(
                    oi.getId(),
                    p.getId(),
                    p.getName(),
                    oi.getQuantity(),
                    oi.getPrice(),
                    oi.getPrice(),
                    oi.getPrice() * oi.getQuantity(),
                    Map.of("id",          p.getId(),
                           "name",        p.getName(),
                           "price",       p.getPrice(),
                           "image_url",   p.getImageUrl() != null ? p.getImageUrl() : "",
                           "description", p.getDescription() != null ? p.getDescription() : ""));
        }).toList();
        return new OrderResponse(o.getId(), o.getOrderNumber(), o.getCustomerName(),
                o.getCustomerEmail(), o.getTotalAmount(), o.getStatus(),
                items, o.getCreatedAt(), o.getUpdatedAt());
    }
}
