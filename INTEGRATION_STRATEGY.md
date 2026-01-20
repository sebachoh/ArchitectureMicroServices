# E-Commerce Microservices Integration Strategy

## Executive Summary

This document provides a comprehensive analysis and implementation plan for inter-service communication in an e-commerce microservices architecture. The system consists of 5 services managing the complete order lifecycle from product browsing to order tracking.

**Branch:** `connection-among-microservices`
**Date:** January 19, 2026

---

## 1. Current State Analysis

### 1.1 Service Inventory

| Service | Port | Database | Purpose | Current Integration |
|---------|------|----------|---------|-------------------|
| **catalogue-service** | 8081 | H2 (cataloguedb) | Product management with stock | None (isolated) |
| **panier-service** | 8082 | H2 (panierdb) | Shopping cart | ✅ Calls catalogue-service |
| **paiment-service** | 8083 | H2 (no config) | Payment processing | None (isolated) |
| **tracking-service** | 8084 | H2 (trackingdb) | Order tracking | None (isolated) |
| **gateway-service** | 8080 | None | API Gateway + CORS | Routes all services |

### 1.2 Current Domain Models

#### Catalogue Service - Product Entity
```java
@Entity Product {
    Long id;
    String name;
    String description;
    double price;
    int quantity;  // ⚠️ STOCK QUANTITY - Critical for business logic
}
```

**Available Endpoints:**
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

**Initial Data:** 3 products (Empanada: 15 stock, Arepa: 20 stock, Bandeja Paisa: 10 stock)

#### Panier Service - CartItem Entity
```java
@Entity CartItem {
    Long id;
    Long productId;      // Reference to catalogue product
    int quantity;
    double unitPrice;
}
```

**Available Endpoints:**
- `POST /cart/add?productId={id}&quantity={qty}` - Add to cart
- `GET /cart` - View cart
- `DELETE /cart` - Clear cart

**Current Integration Pattern:**
```java
// CartService.addToCart() method
Map<String, Object> product = restTemplate.getForObject(
    "http://localhost:8081/api/products/" + productId, 
    Map.class
);
double price = Double.parseDouble(product.get("price").toString());
```

**Issues with Current Implementation:**
- ❌ No stock validation before adding to cart
- ❌ No error handling if catalogue service is down
- ❌ No transactional consistency
- ❌ Uses hardcoded URL instead of service discovery
- ✅ RestTemplate bean properly configured

#### Paiment Service - PaimentItem Entity
```java
@Entity PaimentItem {
    Long id;
    String cardType;
    String cardNumber;
    Double amount;
    Boolean approved;
}
```

**Available Endpoints:**
- `POST /api/payments` - Create payment
- `PUT /api/payments/{id}/process` - Process payment
- `GET /api/payments/{id}` - Get payment by ID
- `GET /api/payments` - Get all payments

**Issues:**
- ❌ No link to orders/cart
- ❌ No validation of cart total
- ❌ No communication with other services

#### Tracking Service - TrackingInfo Entity
```java
@Entity TrackingInfo {
    Long id;
    Long orderId;        // ⚠️ No Order entity exists yet!
    Long productId;
    String status;
    String currentLocation;
    LocalDateTime lastUpdate;
}
```

**Available Endpoints:**
- `POST /api/tracking` - Create tracking
- `GET /api/tracking/{id}` - Get tracking by ID
- `GET /api/tracking/order/{orderId}` - Get tracking by order
- `GET /api/tracking/product/{productId}` - Get by product
- `PUT /api/tracking/{id}?status={status}&location={loc}` - Update status
- `DELETE /api/tracking/{id}` - Delete tracking

**Issues:**
- ❌ References `orderId` but no Order entity/service exists
- ❌ No integration with cart or payment
- ❌ RestTemplate configured but unused

### 1.3 Gateway Configuration Analysis

**Gateway Routes (application.yml):**
```yaml
- catalogue-service: /api/products/** → http://localhost:8081
- panier-service: /cart/** → http://localhost:8082
- paiement-service: /api/payments/** → http://localhost:8083
- tracking-service: /api/tracking/** → http://localhost:8084
```

**CORS Configuration:** Properly configured for frontend (http://localhost:5173)

---

## 2. Business Process Analysis

### 2.1 E-Commerce Customer Journey

```
1. BROWSE → User views products (Catalogue)
2. SELECT → User adds products to cart (Panier ←→ Catalogue)
3. CHECKOUT → User reviews cart and proceeds to payment (Panier → Paiment)
4. PAY → Payment is processed (Paiment)
5. CONFIRM → Order is created and stock updated (Panier → Catalogue + Tracking)
6. TRACK → User tracks delivery status (Tracking ←→ Catalogue)
7. CANCEL (optional) → Order canceled, stock restored (Tracking → Panier → Catalogue)
```

### 2.2 Missing Concept: **Order Entity**

Currently, the system lacks a central **Order** concept. The tracking service references `orderId`, but no order is created. 

**Two Design Options:**

**Option A: Order as part of Panier Service** (Recommended for simplicity)
- Panier service manages both cart and orders
- When checkout happens, cart converts to order
- OrderId is generated in panier-service

**Option B: Separate Order Service**
- New microservice dedicated to orders
- More scalable but adds complexity

**Decision:** Use **Option A** - extend panier-service with Order entity

---

## 3. Detailed Business Flows

### Flow 1: Add to Cart (Enhanced)

**Current State:** Partially implemented
**Required Enhancements:** Stock validation

```
Sequence Diagram:
┌─────────┐         ┌────────┐         ┌──────────┐
│ Frontend│         │ Panier │         │ Catalogue│
└────┬────┘         └───┬────┘         └────┬─────┘
     │                  │                    │
     │ POST /cart/add   │                    │
     │ productId=1      │                    │
     │ quantity=2       │                    │
     ├─────────────────>│                    │
     │                  │                    │
     │                  │ GET /products/{id} │
     │                  ├───────────────────>│
     │                  │                    │
     │                  │ Product{quantity:15}│
     │                  │<───────────────────┤
     │                  │                    │
     │                  │ Check: 15 >= 2 ✓  │
     │                  │                    │
     │                  │ Save CartItem      │
     │   200 OK         │                    │
     │<─────────────────┤                    │
     │ CartItem created │                    │
```

**Business Rules:**
1. Validate product exists in catalogue
2. Check available stock >= requested quantity
3. Get current price from catalogue
4. Add to cart (or increment if already exists)
5. Return error if insufficient stock

**Error Scenarios:**
- Product not found → 404 Not Found
- Insufficient stock → 400 Bad Request with message
- Catalogue service down → 503 Service Unavailable

---

### Flow 2: Checkout Process (NEW - Complex)

**Current State:** Not implemented
**Required:** Complete checkout with payment, order creation, stock update, tracking

```
Sequence Diagram:
┌─────────┐    ┌────────┐    ┌──────────┐    ┌─────────┐    ┌──────────┐
│ Frontend│    │ Panier │    │ Catalogue│    │ Paiment │    │ Tracking │
└────┬────┘    └───┬────┘    └────┬─────┘    └────┬────┘    └────┬─────┘
     │             │              │               │              │
     │ POST /cart/checkout        │               │              │
     │ paymentData│              │               │              │
     ├────────────>│              │               │              │
     │             │              │               │              │
     │             │ 1. Validate stock for all items           │
     │             │  GET /products/{id}          │              │
     │             ├─────────────>│               │              │
     │             │  Product{quantity}           │              │
     │             │<─────────────┤               │              │
     │             │              │               │              │
     │             │ 2. Process Payment           │              │
     │             │  POST /payments              │              │
     │             ├──────────────────────────────>│              │
     │             │  PaimentItem{approved:true}  │              │
     │             │<──────────────────────────────┤              │
     │             │              │               │              │
     │             │ 3. Deduct stock              │              │
     │             │  PUT /products/{id}          │              │
     │             │  {quantity: oldQty - boughtQty}             │
     │             ├─────────────>│               │              │
     │             │  Updated Product             │              │
     │             │<─────────────┤               │              │
     │             │              │               │              │
     │             │ 4. Create Order (DB)         │              │
     │             │              │               │              │
     │             │ 5. Create Tracking           │              │
     │             │  POST /tracking              │              │
     │             │  {orderId, items}            │              │
     │             ├──────────────────────────────────────────────>│
     │             │  TrackingInfo{status:PENDING}│              │
     │             │<──────────────────────────────────────────────┤
     │             │              │               │              │
     │             │ 6. Clear Cart                │              │
     │             │              │               │              │
     │  200 OK     │              │               │              │
     │  OrderDTO   │              │               │              │
     │<────────────┤              │               │              │
```

**Business Rules:**
1. **Atomicity**: All operations must succeed or none (consider transactions)
2. Validate all cart items still have sufficient stock
3. Calculate total amount
4. Process payment
5. Deduct stock from catalogue for each item
6. Create Order entity in panier database
7. Create tracking entry for the order
8. Clear the cart
9. Return order confirmation with tracking ID

**Rollback Strategy:**
- If payment fails → Return error, no stock deduction
- If stock deduction fails → Refund payment, return error
- If tracking creation fails → Log warning but complete order (tracking is not critical)

**Error Scenarios:**
- Stock changed between add-to-cart and checkout → 409 Conflict
- Payment declined → 402 Payment Required
- Service unavailable → 503 with retry mechanism

---

### Flow 3: Track Order with Product Details (Enhancement)

**Current State:** Tracking service isolated
**Enhancement:** Enrich tracking response with product details

```
Sequence Diagram:
┌─────────┐         ┌──────────┐         ┌──────────┐
│ Frontend│         │ Tracking │         │ Catalogue│
└────┬────┘         └────┬─────┘         └────┬─────┘
     │                   │                     │
     │ GET /tracking/order/{orderId}          │
     ├──────────────────>│                     │
     │                   │                     │
     │                   │ TrackingInfo from DB│
     │                   │                     │
     │                   │ GET /products/{productId}
     │                   ├────────────────────>│
     │                   │  Product details    │
     │                   │<────────────────────┤
     │                   │                     │
     │   200 OK          │                     │
     │   TrackingDTO     │                     │
     │   + ProductInfo   │                     │
     │<──────────────────┤                     │
```

**Enhancement:** Return enriched tracking info with product names, images, etc.

---

### Flow 4: Cancel Order (NEW)

**Current State:** Not implemented
**Complexity:** High - requires coordinated rollback

```
Sequence Diagram:
┌─────────┐    ┌──────────┐    ┌────────┐    ┌──────────┐    ┌─────────┐
│ Frontend│    │ Tracking │    │ Panier │    │ Catalogue│    │ Paiment │
└────┬────┘    └────┬─────┘    └───┬────┘    └────┬─────┘    └────┬────┘
     │              │               │              │               │
     │ PUT /tracking/{id}/cancel    │              │               │
     ├─────────────>│               │              │               │
     │              │               │              │               │
     │              │ Check status (must be PENDING/PREPARING)    │
     │              │               │              │               │
     │              │ GET /orders/{orderId}        │               │
     │              ├──────────────>│              │               │
     │              │ Order details │              │               │
     │              │<──────────────┤              │               │
     │              │               │              │               │
     │              │ Restore stock │              │               │
     │              ├──────────────────────────────>│               │
     │              │               │              │               │
     │              │ Refund payment│              │               │
     │              ├──────────────────────────────────────────────>│
     │              │               │              │               │
     │              │ Update status: CANCELLED     │               │
     │              │               │              │               │
     │   200 OK     │               │              │               │
     │<─────────────┤               │              │               │
```

**Business Rules:**
1. Only allow cancellation if status is PENDING or PREPARING
2. Cannot cancel SHIPPED or DELIVERED orders
3. Restore stock for each product
4. Initiate refund process
5. Update tracking status to CANCELLED

---

## 4. Implementation Plan

### Phase 1: Enhance Catalogue Service (Stock Management)

**New Methods in ProductService:**

```java
// Method 1: Check stock availability
public boolean hasStock(Long productId, int requiredQuantity) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found"));
    return product.getQuantity() >= requiredQuantity;
}

// Method 2: Reserve stock (optimistic locking recommended)
public void deductStock(Long productId, int quantity) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found"));
    
    if (product.getQuantity() < quantity) {
        throw new InsufficientStockException(
            "Not enough stock. Available: " + product.getQuantity()
        );
    }
    
    product.setQuantity(product.getQuantity() - quantity);
    productRepository.save(product);
}

// Method 3: Restore stock (for cancellations)
public void restoreStock(Long productId, int quantity) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found"));
    
    product.setQuantity(product.getQuantity() + quantity);
    productRepository.save(product);
}
```

**New Controller Endpoints:**

```java
@PutMapping("/{id}/stock/deduct")
public ResponseEntity<Void> deductStock(
    @PathVariable Long id, 
    @RequestParam int quantity
) {
    productService.deductStock(id, quantity);
    return ResponseEntity.ok().build();
}

@PutMapping("/{id}/stock/restore")
public ResponseEntity<Void> restoreStock(
    @PathVariable Long id, 
    @RequestParam int quantity
) {
    productService.restoreStock(id, quantity);
    return ResponseEntity.ok().build();
}

@GetMapping("/{id}/stock/check")
public ResponseEntity<Boolean> checkStock(
    @PathVariable Long id, 
    @RequestParam int quantity
) {
    boolean hasStock = productService.hasStock(id, quantity);
    return ResponseEntity.ok(hasStock);
}
```

**New Exception Class:**

```java
package com.example.catalogue.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
```

**Files to Create:**
- `catalogue-service/src/main/java/com/example/catalogue/exception/InsufficientStockException.java`

**Files to Modify:**
- `catalogue-service/src/main/java/com/example/catalogue/service/ProductService.java`
- `catalogue-service/src/main/java/com/example/catalogue/controller/ProductController.java`

---

### Phase 2: Enhance Panier Service (Add Order Entity)

**New Entity - Order:**

```java
package com.example.panier.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();
    
    private Double totalAmount;
    private String status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private Long paymentId;
    private Long trackingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors, Getters, Setters
}
```

**New Entity - OrderItem:**

```java
package com.example.panier.entity;

import jakarta.persistence.*;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long productId;
    private String productName; // Denormalized for history
    private int quantity;
    private double unitPrice;
    private double subtotal;
    
    // Constructors, Getters, Setters
}
```

**New DTOs:**

```java
package com.example.panier.dto;

public class CheckoutRequest {
    private String cardType;
    private String cardNumber;
    private String shippingAddress;
    
    // Getters, Setters
}
```

```java
package com.example.panier.dto;

import java.util.List;

public class CheckoutResponse {
    private Long orderId;
    private Double totalAmount;
    private Long paymentId;
    private Long trackingId;
    private String status;
    private List<OrderItemDTO> items;
    
    // Getters, Setters
}
```

```java
package com.example.panier.dto;

public class OrderItemDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    
    // Getters, Setters
}
```

**Enhanced CartService:**

```java
package com.example.panier.service;

import com.example.panier.entity.*;
import com.example.panier.dto.*;
import com.example.panier.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RestTemplate restTemplate;

    private final String CATALOGUE_URL = "http://localhost:8081/api/products/";
    private final String PAYMENT_URL = "http://localhost:8083/api/payments";
    private final String TRACKING_URL = "http://localhost:8084/api/tracking";

    /**
     * Enhanced: Add to cart with stock validation
     */
    public CartItem addToCart(Long productId, Integer quantity) {
        try {
            // 1. Get product details from catalogue
            Map<String, Object> product = restTemplate.getForObject(
                CATALOGUE_URL + productId, 
                Map.class
            );

            if (product == null) {
                throw new RuntimeException("Product not found in catalogue");
            }

            // 2. Extract product details
            double price = Double.parseDouble(product.get("price").toString());
            int availableStock = Integer.parseInt(product.get("quantity").toString());

            // 3. Check stock availability
            if (availableStock < quantity) {
                throw new InsufficientStockException(
                    "Insufficient stock. Available: " + availableStock + 
                    ", Requested: " + quantity
                );
            }

            // 4. Add or update cart item
            CartItem item = cartItemRepository.findByProductId(productId)
                    .orElse(new CartItem(null, productId, 0, price));

            int newQuantity = item.getQuantity() + quantity;
            
            // 5. Validate total quantity doesn't exceed stock
            if (newQuantity > availableStock) {
                throw new InsufficientStockException(
                    "Cannot add " + quantity + " more. You already have " + 
                    item.getQuantity() + " in cart. Available: " + availableStock
                );
            }

            item.setQuantity(newQuantity);
            item.setUnitPrice(price);

            return cartItemRepository.save(item);
            
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Product not found: " + productId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to communicate with catalogue service: " + 
                e.getMessage());
        }
    }

    public List<CartItem> getCart() {
        return cartItemRepository.findAll();
    }

    public void clearCart() {
        cartItemRepository.deleteAll();
    }

    /**
     * NEW: Checkout process
     */
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        // 1. Get current cart
        List<CartItem> cartItems = cartItemRepository.findAll();
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 2. Validate stock for all items
        for (CartItem item : cartItems) {
            Map<String, Object> product = restTemplate.getForObject(
                CATALOGUE_URL + item.getProductId(), 
                Map.class
            );
            
            int availableStock = Integer.parseInt(product.get("quantity").toString());
            if (availableStock < item.getQuantity()) {
                throw new RuntimeException(
                    "Stock changed for product " + item.getProductId() + 
                    ". Available: " + availableStock + 
                    ", Required: " + item.getQuantity()
                );
            }
        }

        // 3. Calculate total
        double total = cartItems.stream()
            .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
            .sum();

        // 4. Process payment
        Map<String, Object> paymentRequest = Map.of(
            "cardType", request.getCardType(),
            "cardNumber", request.getCardNumber(),
            "amount", total,
            "approved", false
        );
        
        Map<String, Object> paymentResponse = restTemplate.postForObject(
            PAYMENT_URL, 
            paymentRequest, 
            Map.class
        );
        
        Long paymentId = Long.parseLong(paymentResponse.get("id").toString());
        
        // Process the payment
        restTemplate.put(
            PAYMENT_URL + "/" + paymentId + "/process", 
            null
        );

        // 5. Deduct stock for each item
        for (CartItem item : cartItems) {
            try {
                restTemplate.put(
                    CATALOGUE_URL + item.getProductId() + 
                    "/stock/deduct?quantity=" + item.getQuantity(),
                    null
                );
            } catch (Exception e) {
                // Rollback: refund payment
                throw new RuntimeException("Failed to deduct stock: " + e.getMessage());
            }
        }

        // 6. Create order
        Order order = new Order();
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setPaymentId(paymentId);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // Create order items with product names
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            Map<String, Object> product = restTemplate.getForObject(
                CATALOGUE_URL + cartItem.getProductId(), 
                Map.class
            );
            
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(product.get("name").toString());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setSubtotal(cartItem.getUnitPrice() * cartItem.getQuantity());
            return orderItem;
        }).collect(Collectors.toList());
        
        order.setItems(orderItems);
        order = orderRepository.save(order);

        // 7. Create tracking
        for (OrderItem orderItem : orderItems) {
            Map<String, Object> trackingRequest = Map.of(
                "orderId", order.getId(),
                "productId", orderItem.getProductId(),
                "status", "PENDING",
                "currentLocation", "Warehouse",
                "lastUpdate", LocalDateTime.now().toString()
            );
            
            try {
                Map<String, Object> trackingResponse = restTemplate.postForObject(
                    TRACKING_URL,
                    trackingRequest,
                    Map.class
                );
                
                Long trackingId = Long.parseLong(trackingResponse.get("id").toString());
                order.setTrackingId(trackingId);
                orderRepository.save(order);
            } catch (Exception e) {
                // Log warning but don't fail - tracking is not critical
                System.err.println("Warning: Failed to create tracking: " + e.getMessage());
            }
        }

        // 8. Clear cart
        cartItemRepository.deleteAll();

        // 9. Build response
        CheckoutResponse response = new CheckoutResponse();
        response.setOrderId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentId(order.getPaymentId());
        response.setTrackingId(order.getTrackingId());
        response.setStatus(order.getStatus());
        response.setItems(orderItems.stream().map(item -> {
            OrderItemDTO dto = new OrderItemDTO();
            dto.setProductId(item.getProductId());
            dto.setProductName(item.getProductName());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setSubtotal(item.getSubtotal());
            return dto;
        }).collect(Collectors.toList()));
        
        return response;
    }

    /**
     * NEW: Get order by ID
     */
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    /**
     * NEW: Get all orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
```

**New Exception:**

```java
package com.example.panier.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
```

**Enhanced CartController:**

```java
package com.example.panier.controller;

import com.example.panier.entity.CartItem;
import com.example.panier.entity.Order;
import com.example.panier.dto.CheckoutRequest;
import com.example.panier.dto.CheckoutResponse;
import com.example.panier.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public CartItem addToCart(
        @RequestParam Long productId, 
        @RequestParam Integer quantity
    ) {
        return cartService.addToCart(productId, quantity);
    }

    @GetMapping
    public List<CartItem> getCart() {
        return cartService.getCart();
    }

    @DeleteMapping
    public String clearCart() {
        cartService.clearCart();
        return "Cart cleared successfully";
    }

    /**
     * NEW: Checkout endpoint
     */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
        @RequestBody CheckoutRequest request
    ) {
        CheckoutResponse response = cartService.checkout(request);
        return ResponseEntity.ok(response);
    }

    /**
     * NEW: Get order by ID
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        Order order = cartService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * NEW: Get all orders
     */
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = cartService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}
```

**New Repository:**

```java
package com.example.panier.repository;

import com.example.panier.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
```

**Files to Create:**
- `panier-service/src/main/java/com/example/panier/entity/Order.java`
- `panier-service/src/main/java/com/example/panier/entity/OrderItem.java`
- `panier-service/src/main/java/com/example/panier/dto/CheckoutRequest.java`
- `panier-service/src/main/java/com/example/panier/dto/CheckoutResponse.java`
- `panier-service/src/main/java/com/example/panier/dto/OrderItemDTO.java`
- `panier-service/src/main/java/com/example/panier/repository/OrderRepository.java`
- `panier-service/src/main/java/com/example/panier/exception/InsufficientStockException.java`

**Files to Modify:**
- `panier-service/src/main/java/com/example/panier/service/CartService.java`
- `panier-service/src/main/java/com/example/panier/controller/CartController.java`

---

### Phase 3: Enhance Tracking Service (Product Enrichment)

**New DTO - EnrichedTrackingDTO:**

```java
package com.example.tracking.dto;

import java.time.LocalDateTime;

public class EnrichedTrackingDTO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String status;
    private String currentLocation;
    private LocalDateTime lastUpdate;
    
    // Enriched product data
    private String productName;
    private String productDescription;
    private double productPrice;
    
    // Constructors, Getters, Setters
}
```

**Enhanced TrackingService:**

```java
package com.example.tracking.service;

import com.example.tracking.entity.TrackingInfo;
import com.example.tracking.dto.EnrichedTrackingDTO;
import com.example.tracking.repository.TrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrackingService {

    @Autowired
    private TrackingRepository trackingRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final String CATALOGUE_URL = "http://localhost:8081/api/products/";
    private final String PANIER_URL = "http://localhost:8082/cart/";

    public TrackingInfo createTracking(TrackingInfo trackingInfo) {
        trackingInfo.setLastUpdate(LocalDateTime.now());
        return trackingRepository.save(trackingInfo);
    }

    public TrackingInfo updateStatus(Long id, String newStatus, String location) {
        TrackingInfo tracking = trackingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tracking not found with id: " + id));
        
        tracking.setStatus(newStatus);
        tracking.setCurrentLocation(location);
        tracking.setLastUpdate(LocalDateTime.now());
        
        return trackingRepository.save(tracking);
    }

    /**
     * NEW: Get enriched tracking by order ID
     */
    public EnrichedTrackingDTO getEnrichedTrackingByOrderId(Long orderId) {
        TrackingInfo tracking = trackingRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Tracking not found for order: " + orderId));
        
        return enrichTrackingInfo(tracking);
    }

    /**
     * NEW: Get all enriched trackings for a product
     */
    public List<EnrichedTrackingDTO> getEnrichedTrackingsByProductId(Long productId) {
        List<TrackingInfo> trackings = trackingRepository.findByProductId(productId);
        return trackings.stream()
            .map(this::enrichTrackingInfo)
            .collect(Collectors.toList());
    }

    /**
     * Helper method to enrich tracking with product details
     */
    private EnrichedTrackingDTO enrichTrackingInfo(TrackingInfo tracking) {
        EnrichedTrackingDTO dto = new EnrichedTrackingDTO();
        dto.setId(tracking.getId());
        dto.setOrderId(tracking.getOrderId());
        dto.setProductId(tracking.getProductId());
        dto.setStatus(tracking.getStatus());
        dto.setCurrentLocation(tracking.getCurrentLocation());
        dto.setLastUpdate(tracking.getLastUpdate());
        
        // Fetch product details from catalogue
        try {
            Map<String, Object> product = restTemplate.getForObject(
                CATALOGUE_URL + tracking.getProductId(),
                Map.class
            );
            
            if (product != null) {
                dto.setProductName(product.get("name").toString());
                dto.setProductDescription(product.get("description").toString());
                dto.setProductPrice(Double.parseDouble(product.get("price").toString()));
            }
        } catch (Exception e) {
            // Log error but continue with basic tracking info
            System.err.println("Warning: Could not fetch product details: " + e.getMessage());
        }
        
        return dto;
    }

    /**
     * NEW: Cancel order
     */
    public TrackingInfo cancelOrder(Long trackingId) {
        TrackingInfo tracking = trackingRepository.findById(trackingId)
            .orElseThrow(() -> new RuntimeException("Tracking not found: " + trackingId));
        
        // Only allow cancellation for certain statuses
        if (!tracking.getStatus().equals("PENDING") && 
            !tracking.getStatus().equals("PREPARING")) {
            throw new RuntimeException(
                "Cannot cancel order in status: " + tracking.getStatus()
            );
        }
        
        // Get order details from panier service
        try {
            Map<String, Object> order = restTemplate.getForObject(
                PANIER_URL + "orders/" + tracking.getOrderId(),
                Map.class
            );
            
            // Extract order items
            List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
            
            // Restore stock for each item
            for (Map<String, Object> item : items) {
                Long productId = Long.parseLong(item.get("productId").toString());
                int quantity = Integer.parseInt(item.get("quantity").toString());
                
                restTemplate.put(
                    CATALOGUE_URL + productId + "/stock/restore?quantity=" + quantity,
                    null
                );
            }
            
            // Note: In a real system, also handle payment refund here
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to cancel order: " + e.getMessage());
        }
        
        // Update tracking status
        tracking.setStatus("CANCELLED");
        tracking.setLastUpdate(LocalDateTime.now());
        
        return trackingRepository.save(tracking);
    }

    // Original methods
    public Optional<TrackingInfo> getTrackingByOrderId(Long orderId) {
        return trackingRepository.findByOrderId(orderId);
    }

    public List<TrackingInfo> getTrackingByProductId(Long productId) {
        return trackingRepository.findByProductId(productId);
    }

    public List<TrackingInfo> getAllTrackings() {
        return trackingRepository.findAll();
    }

    public Optional<TrackingInfo> getTrackingById(Long id) {
        return trackingRepository.findById(id);
    }

    public void deleteTracking(Long id) {
        trackingRepository.deleteById(id);
    }
}
```

**Enhanced TrackingController:**

```java
package com.example.tracking.controller;

import com.example.tracking.entity.TrackingInfo;
import com.example.tracking.dto.EnrichedTrackingDTO;
import com.example.tracking.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    @GetMapping
    public List<TrackingInfo> getAllTrackings() {
        return trackingService.getAllTrackings();
    }

    @GetMapping("/{id}")
    public TrackingInfo getTrackingById(@PathVariable Long id) {
        return trackingService.getTrackingById(id)
                .orElseThrow(() -> new RuntimeException("Tracking not found with id: " + id));
    }

    @GetMapping("/order/{orderId}")
    public TrackingInfo getTrackingByOrderId(@PathVariable Long orderId) {
        return trackingService.getTrackingByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Tracking not found for order: " + orderId));
    }

    /**
     * NEW: Get enriched tracking by order ID
     */
    @GetMapping("/order/{orderId}/enriched")
    public ResponseEntity<EnrichedTrackingDTO> getEnrichedTrackingByOrderId(
        @PathVariable Long orderId
    ) {
        EnrichedTrackingDTO dto = trackingService.getEnrichedTrackingByOrderId(orderId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/product/{productId}")
    public List<TrackingInfo> getTrackingByProductId(@PathVariable Long productId) {
        return trackingService.getTrackingByProductId(productId);
    }

    /**
     * NEW: Get enriched trackings by product ID
     */
    @GetMapping("/product/{productId}/enriched")
    public ResponseEntity<List<EnrichedTrackingDTO>> getEnrichedTrackingsByProductId(
        @PathVariable Long productId
    ) {
        List<EnrichedTrackingDTO> dtos = trackingService.getEnrichedTrackingsByProductId(productId);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public TrackingInfo createTracking(@Valid @RequestBody TrackingInfo trackingInfo) {
        return trackingService.createTracking(trackingInfo);
    }

    @PutMapping("/{id}")
    public TrackingInfo updateStatus(
        @PathVariable Long id, 
        @RequestParam String status,
        @RequestParam(required = false) String location
    ) {
        return trackingService.updateStatus(id, status, location);
    }

    /**
     * NEW: Cancel order
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<TrackingInfo> cancelOrder(@PathVariable Long id) {
        TrackingInfo tracking = trackingService.cancelOrder(id);
        return ResponseEntity.ok(tracking);
    }

    @DeleteMapping("/{id}")
    public void deleteTracking(@PathVariable Long id) {
        trackingService.deleteTracking(id);
    }
}
```

**Files to Create:**
- `tracking-service/src/main/java/com/example/tracking/dto/EnrichedTrackingDTO.java`

**Files to Modify:**
- `tracking-service/src/main/java/com/example/tracking/service/TrackingService.java`
- `tracking-service/src/main/java/com/example/tracking/controller/TrackingController.java`

---

### Phase 4: Paiment Service (Minor Enhancement)

The payment service is mostly complete but could benefit from:

**Additional Fields in PaimentItem:**

```java
@Entity
public class PaimentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String cardType;
    private String cardNumber;
    private Double amount;
    private Boolean approved;
    
    // NEW FIELDS
    private Long orderId;  // Link to order
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String transactionId;  // For refunds
    
    // Getters, Setters
}
```

**Files to Modify:**
- `paiment-service/src/main/java/com/example/paiment_service/entity/PaimentItem.java`

---

## 5. Error Handling Strategy

### 5.1 Exception Hierarchy

```java
// Base exception
public class ServiceIntegrationException extends RuntimeException {
    private String serviceName;
    
    public ServiceIntegrationException(String serviceName, String message) {
        super("Service " + serviceName + " error: " + message);
        this.serviceName = serviceName;
    }
}

// Specific exceptions
public class InsufficientStockException extends RuntimeException { }
public class ServiceUnavailableException extends ServiceIntegrationException { }
public class PaymentFailedException extends ServiceIntegrationException { }
```

### 5.2 RestTemplate Error Handling

**Add to each service that uses RestTemplate:**

```java
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(5))
        .errorHandler(new RestTemplateErrorHandler())
        .build();
}

public class RestTemplateErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() || 
               response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().is5xxServerError()) {
            throw new ServiceUnavailableException("External service unavailable");
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new ResourceNotFoundException("Resource not found");
        }
    }
}
```

### 5.3 Global Exception Handler (for each service)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
        InsufficientStockException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(
        ServiceUnavailableException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "A required service is temporarily unavailable. Please try again later.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred: " + ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    
    // Constructor, Getters, Setters
}
```

---

## 6. Configuration Improvements

### 6.1 Externalize Service URLs

Instead of hardcoding URLs, use application.properties:

**panier-service/application.properties:**
```properties
spring.application.name=panier-service
server.port=8082

# Service URLs
services.catalogue.url=http://localhost:8081
services.payment.url=http://localhost:8083
services.tracking.url=http://localhost:8084

# Database
spring.datasource.url=jdbc:h2:mem:panierdb
spring.datasource.driverClassName=org.h2.Driver
spring.h2.console.enabled=true
```

**In Java code:**
```java
@Value("${services.catalogue.url}")
private String catalogueUrl;

@Value("${services.payment.url}")
private String paymentUrl;

@Value("${services.tracking.url}")
private String trackingUrl;
```

### 6.2 Enable Transaction Management

**Add to PanierApplication:**
```java
@EnableTransactionManagement
@SpringBootApplication
public class PanierApplication {
    // ...
}
```

---

## 7. Testing Strategy

### 7.1 Unit Tests

**Example: CartService Test**

```java
@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    
    @Mock
    private CartItemRepository cartItemRepository;
    
    @Mock
    private RestTemplate restTemplate;
    
    @InjectMocks
    private CartService cartService;
    
    @Test
    void addToCart_WhenStockAvailable_ShouldAddItem() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 2;
        
        Map<String, Object> product = Map.of(
            "id", 1L,
            "name", "Empanada",
            "price", 3.00,
            "quantity", 15
        );
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(product);
        when(cartItemRepository.findByProductId(productId))
            .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        CartItem result = cartService.addToCart(productId, quantity);
        
        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(quantity, result.getQuantity());
        assertEquals(3.00, result.getUnitPrice());
    }
    
    @Test
    void addToCart_WhenInsufficientStock_ShouldThrowException() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 20;
        
        Map<String, Object> product = Map.of(
            "id", 1L,
            "name", "Empanada",
            "price", 3.00,
            "quantity", 10  // Only 10 available
        );
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(product);
        
        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> {
            cartService.addToCart(productId, quantity);
        });
    }
}
```

### 7.2 Integration Tests

**Example: Checkout Integration Test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CheckoutIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    private String baseUrl;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }
    
    @Test
    void checkout_FullFlow_ShouldCompleteSuccessfully() {
        // 1. Add items to cart
        restTemplate.postForEntity(
            baseUrl + "/cart/add?productId=1&quantity=2",
            null,
            CartItem.class
        );
        
        // 2. Checkout
        CheckoutRequest request = new CheckoutRequest();
        request.setCardType("VISA");
        request.setCardNumber("4111111111111111");
        
        ResponseEntity<CheckoutResponse> response = restTemplate.postForEntity(
            baseUrl + "/cart/checkout",
            request,
            CheckoutResponse.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getOrderId());
        assertNotNull(response.getBody().getTrackingId());
    }
}
```

### 7.3 Manual Testing with cURL

**Test 1: Add to Cart**
```bash
curl -X POST "http://localhost:8080/cart/add?productId=1&quantity=2"
```

**Test 2: View Cart**
```bash
curl http://localhost:8080/cart
```

**Test 3: Checkout**
```bash
curl -X POST http://localhost:8080/cart/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "cardType": "VISA",
    "cardNumber": "4111111111111111",
    "shippingAddress": "123 Main St"
  }'
```

**Test 4: Track Order**
```bash
curl http://localhost:8080/api/tracking/order/1/enriched
```

**Test 5: Cancel Order**
```bash
curl -X PUT http://localhost:8080/api/tracking/1/cancel
```

---

## 8. Implementation Checklist

### Phase 1: Catalogue Service ✓
- [ ] Create `InsufficientStockException`
- [ ] Add stock management methods to `ProductService`
- [ ] Add stock endpoints to `ProductController`
- [ ] Test stock deduction and restoration

### Phase 2: Panier Service ✓
- [ ] Create `Order` entity
- [ ] Create `OrderItem` entity
- [ ] Create DTOs (`CheckoutRequest`, `CheckoutResponse`, `OrderItemDTO`)
- [ ] Create `OrderRepository`
- [ ] Create `InsufficientStockException`
- [ ] Update `CartService` with enhanced `addToCart()`
- [ ] Add `checkout()` method to `CartService`
- [ ] Add order management methods
- [ ] Update `CartController` with checkout endpoint
- [ ] Add order endpoints to `CartController`
- [ ] Update `application.properties` with service URLs

### Phase 3: Tracking Service ✓
- [ ] Create `EnrichedTrackingDTO`
- [ ] Add enrichment methods to `TrackingService`
- [ ] Add cancellation method to `TrackingService`
- [ ] Add enriched endpoints to `TrackingController`
- [ ] Add cancellation endpoint
- [ ] Update `application.properties` with service URLs

### Phase 4: Paiment Service (Optional)
- [ ] Add orderId field to `PaimentItem`
- [ ] Add timestamp fields

### Phase 5: Error Handling (All Services)
- [ ] Create exception classes
- [ ] Add `GlobalExceptionHandler`
- [ ] Configure `RestTemplate` error handling
- [ ] Add timeouts

### Phase 6: Testing
- [ ] Write unit tests for each service
- [ ] Write integration tests for checkout flow
- [ ] Manual testing with cURL/Postman
- [ ] Test error scenarios

### Phase 7: Documentation
- [ ] Update API documentation
- [ ] Document error codes
- [ ] Create postman collection

---

## 9. API Summary

### 9.1 New Endpoints

#### Catalogue Service
- `PUT /api/products/{id}/stock/deduct?quantity={qty}` - Deduct stock
- `PUT /api/products/{id}/stock/restore?quantity={qty}` - Restore stock
- `GET /api/products/{id}/stock/check?quantity={qty}` - Check stock availability

#### Panier Service
- `POST /cart/checkout` - Checkout cart (returns order + tracking)
- `GET /cart/orders` - Get all orders
- `GET /cart/orders/{orderId}` - Get order by ID

#### Tracking Service
- `GET /api/tracking/order/{orderId}/enriched` - Get tracking with product details
- `GET /api/tracking/product/{productId}/enriched` - Get all trackings for product with details
- `PUT /api/tracking/{id}/cancel` - Cancel order (restores stock)

### 9.2 Complete API Flow

```
USER JOURNEY:
1. GET /api/products → Browse products
2. POST /cart/add?productId=1&quantity=2 → Add to cart (validates stock)
3. GET /cart → View cart
4. POST /cart/checkout → Complete purchase
   ↳ Creates order
   ↳ Processes payment
   ↳ Deducts stock
   ↳ Creates tracking
   ↳ Returns orderId + trackingId
5. GET /api/tracking/order/{orderId}/enriched → Track delivery
6. PUT /api/tracking/{id}/cancel → Cancel if needed
```

---

## 10. Architecture Diagram

```
┌─────────────┐
│  Frontend   │ (Port 5173)
│   React     │
└──────┬──────┘
       │
       │ HTTP/REST
       │
┌──────▼──────┐
│   Gateway   │ (Port 8080)
│   Service   │ CORS + Routing
└──────┬──────┘
       │
       ├───────────────┬────────────────┬────────────────┐
       │               │                │                │
┌──────▼──────┐ ┌─────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
│  Catalogue  │ │   Panier   │ │  Paiment    │ │  Tracking   │
│  Service    │ │  Service   │ │  Service    │ │  Service    │
│  (8081)     │ │  (8082)    │ │  (8083)     │ │  (8084)     │
└──────┬──────┘ └─────┬──────┘ └──────┬──────┘ └──────┬──────┘
       │               │                │                │
       │ H2 DB         │ H2 DB          │ H2 DB          │ H2 DB
       │               │                │                │
       │               │◄───────────────┘                │
       │               │                                 │
       │◄──────────────┴─────────────────────────────────┘
       │
       │ RestTemplate Calls:
       │ • Panier → Catalogue (validate stock, get price, deduct/restore)
       │ • Panier → Paiment (process payment)
       │ • Panier → Tracking (create tracking)
       │ • Tracking → Catalogue (enrich product details)
       │ • Tracking → Panier (get order for cancellation)
```

---

## 11. Key Decisions & Trade-offs

### 11.1 Synchronous vs Asynchronous Communication

**Decision:** Use synchronous REST calls (RestTemplate)

**Rationale:**
- ✅ Simpler to implement for learning project
- ✅ Immediate feedback on failures
- ✅ Easier to debug
- ❌ Lower scalability
- ❌ Services tightly coupled

**Future Improvement:** Consider message queues (RabbitMQ/Kafka) for:
- Order processing (async)
- Stock updates
- Payment confirmations

### 11.2 Order Management Location

**Decision:** Orders in Panier Service (not separate service)

**Rationale:**
- ✅ Simpler architecture
- ✅ Natural transition: cart → order
- ✅ Fewer network calls
- ❌ Panier service has multiple responsibilities
- ❌ Harder to scale independently

### 11.3 Transaction Management

**Decision:** Application-level transactions (no distributed transactions)

**Rationale:**
- ✅ Sufficient for MVP
- ✅ Avoids complexity of 2PC/Saga
- ❌ Risk of inconsistency if services fail mid-transaction

**Mitigation:**
- Order of operations matters (payment before stock deduction)
- Implement compensating transactions (cancel order)
- Log all operations for audit trail

### 11.4 Stock Locking

**Decision:** No stock reservation during cart phase

**Rationale:**
- ✅ Simpler implementation
- ✅ No timeout/cleanup logic needed
- ❌ Stock may become unavailable between add-to-cart and checkout

**Mitigation:**
- Validate stock again during checkout
- Return clear error if stock insufficient

---

## 12. Future Enhancements

### 12.1 Service Discovery
- Replace hardcoded URLs with Eureka/Consul
- Enable dynamic service registration

### 12.2 Circuit Breaker
- Add Resilience4j for fault tolerance
- Graceful degradation when services down

### 12.3 API Gateway Enhancements
- Rate limiting
- Authentication/Authorization
- Request logging

### 12.4 Event-Driven Architecture
- Emit events for order completion
- Async processing for non-critical operations
- Event sourcing for audit trail

### 12.5 Monitoring
- Add Spring Boot Actuator
- Prometheus metrics
- Distributed tracing (Zipkin/Jaeger)

---

## 13. Conclusion

This implementation plan provides a complete e-commerce flow with proper inter-service communication. The architecture is RESTful, maintainable, and follows microservices best practices while keeping complexity manageable for a learning project.

**Key Takeaways:**
1. ✅ Services communicate via REST APIs
2. ✅ Business logic spans multiple services (checkout workflow)
3. ✅ Error handling at multiple levels
4. ✅ Proper separation of concerns
5. ✅ Room for future scalability improvements

**Next Steps:**
1. Implement Phase 1 (Catalogue enhancements)
2. Implement Phase 2 (Panier with orders)
3. Implement Phase 3 (Tracking enrichment)
4. Test each integration point
5. Document with Swagger/OpenAPI
6. Deploy and monitor

---

**Document Version:** 1.0
**Last Updated:** January 19, 2026
**Author:** GitHub Copilot
**Branch:** connection-among-microservices
