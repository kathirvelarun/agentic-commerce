package com.amex.ace.merchant.repository;
import com.amex.ace.merchant.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByCustomerEmailAndStatus(String email, String status, Pageable p);
    Page<Order> findByCustomerEmail(String email, Pageable p);
    Page<Order> findByStatus(String status, Pageable p);
}
