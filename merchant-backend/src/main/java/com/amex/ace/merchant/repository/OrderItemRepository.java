package com.amex.ace.merchant.repository;
import com.amex.ace.merchant.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
