package com.example.panier.repository;

import com.example.panier.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by customer email
    List<Order> findByCustomerEmail(String customerEmail);
    
    // Find orders by status
    List<Order> findByStatus(String status);
}
