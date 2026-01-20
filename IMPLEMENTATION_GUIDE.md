# Quick Implementation Guide

This guide provides step-by-step instructions to implement the inter-service communication. Follow these steps in order.

## Prerequisites

- All services running on their respective ports
- Git branch: `connection-among-microservices`

## Step 1: Enhance Catalogue Service (30 mins)

### 1.1 Create Exception Class

Create: `catalogue-service/src/main/java/com/example/catalogue/exception/InsufficientStockException.java`

```java
package com.example.catalogue.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
```

### 1.2 Add Stock Methods to ProductService

Add these methods to `ProductService.java`:

```java
// Add after existing methods

public boolean hasStock(Long productId, int requiredQuantity) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    return product.getQuantity() >= requiredQuantity;
}

public void deductStock(Long productId, int quantity) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    
    if (product.getQuantity() < quantity) {
        throw new InsufficientStockException(
            "Not enough stock. Available: " + product.getQuantity() + 
            ", Requested: " + quantity
        );
    }
    
    product.setQuantity(product.getQuantity() - quantity);
    productRepository.save(product);
}

public void restoreStock(Long productId, int quantity) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    
    product.setQuantity(product.getQuantity() + quantity);
    productRepository.save(product);
}
```

### 1.3 Add Stock Endpoints to ProductController

Add these methods to `ProductController.java`:

```java
// Add after existing methods

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

**Test:**
```bash
# Test stock check
curl "http://localhost:8081/api/products/1/stock/check?quantity=5"

# Test stock deduction
curl -X PUT "http://localhost:8081/api/products/1/stock/deduct?quantity=2"

# Test stock restoration
curl -X PUT "http://localhost:8081/api/products/1/stock/restore?quantity=2"
```

---

## Step 2: Enhance Panier Service - Part A: Entities (45 mins)

### 2.1 Create Order Entity

Create: `panier-service/src/main/java/com/example/panier/entity/Order.java`

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
    private String status;
    private Long paymentId;
    private Long trackingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Order() {}
    
    public Order(Long id, List<OrderItem> items, Double totalAmount, String status, 
                 Long paymentId, Long trackingId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentId = paymentId;
        this.trackingId = trackingId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    
    public Long getTrackingId() { return trackingId; }
    public void setTrackingId(Long trackingId) { this.trackingId = trackingId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

### 2.2 Create OrderItem Entity

Create: `panier-service/src/main/java/com/example/panier/entity/OrderItem.java`

```java
package com.example.panier.entity;

import jakarta.persistence.*;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    
    public OrderItem() {}
    
    public OrderItem(Long id, Long productId, String productName, int quantity, 
                     double unitPrice, double subtotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
```

### 2.3 Create DTOs

Create: `panier-service/src/main/java/com/example/panier/dto/` directory first

Then create these three files:

**CheckoutRequest.java:**
```java
package com.example.panier.dto;

public class CheckoutRequest {
    private String cardType;
    private String cardNumber;
    private String shippingAddress;
    
    public CheckoutRequest() {}
    
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}
```

**OrderItemDTO.java:**
```java
package com.example.panier.dto;

public class OrderItemDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    
    public OrderItemDTO() {}
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
```

**CheckoutResponse.java:**
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
    
    public CheckoutResponse() {}
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    
    public Long getTrackingId() { return trackingId; }
    public void setTrackingId(Long trackingId) { this.trackingId = trackingId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}
```

### 2.4 Create Repositories

Create: `panier-service/src/main/java/com/example/panier/repository/OrderRepository.java`

```java
package com.example.panier.repository;

import com.example.panier.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
```

### 2.5 Create Exception

Create: `panier-service/src/main/java/com/example/panier/exception/InsufficientStockException.java`

```java
package com.example.panier.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
```

---

## Step 3: Enhance Panier Service - Part B: Business Logic (1 hour)

Replace the entire content of `CartService.java` with this enhanced version:

```java
package com.example.panier.service;

import com.example.panier.entity.*;
import com.example.panier.dto.*;
import com.example.panier.exception.InsufficientStockException;
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

    public CartItem addToCart(Long productId, Integer quantity) {
        try {
            Map<String, Object> product = restTemplate.getForObject(
                CATALOGUE_URL + productId, 
                Map.class
            );

            if (product == null) {
                throw new RuntimeException("Product not found in catalogue");
            }

            double price = Double.parseDouble(product.get("price").toString());
            int availableStock = Integer.parseInt(product.get("quantity").toString());

            if (availableStock < quantity) {
                throw new InsufficientStockException(
                    "Insufficient stock. Available: " + availableStock + 
                    ", Requested: " + quantity
                );
            }

            CartItem item = cartItemRepository.findByProductId(productId)
                    .orElse(new CartItem(null, productId, 0, price));

            int newQuantity = item.getQuantity() + quantity;
            
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

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findAll();
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Validate stock
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

        // Calculate total
        double total = cartItems.stream()
            .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
            .sum();

        // Process payment
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
        
        restTemplate.put(
            PAYMENT_URL + "/" + paymentId + "/process", 
            null
        );

        // Deduct stock
        for (CartItem item : cartItems) {
            try {
                restTemplate.put(
                    CATALOGUE_URL + item.getProductId() + 
                    "/stock/deduct?quantity=" + item.getQuantity(),
                    null
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to deduct stock: " + e.getMessage());
            }
        }

        // Create order
        Order order = new Order();
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setPaymentId(paymentId);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
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

        // Create tracking
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
                System.err.println("Warning: Failed to create tracking: " + e.getMessage());
            }
        }

        cartItemRepository.deleteAll();

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

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
```

Now update `CartController.java` by adding these methods:

```java
// Add these imports at the top
import com.example.panier.entity.Order;
import com.example.panier.dto.CheckoutRequest;
import com.example.panier.dto.CheckoutResponse;
import org.springframework.http.ResponseEntity;

// Add these methods to the class

@PostMapping("/checkout")
public ResponseEntity<CheckoutResponse> checkout(
    @RequestBody CheckoutRequest request
) {
    CheckoutResponse response = cartService.checkout(request);
    return ResponseEntity.ok(response);
}

@GetMapping("/orders/{orderId}")
public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
    Order order = cartService.getOrder(orderId);
    return ResponseEntity.ok(order);
}

@GetMapping("/orders")
public ResponseEntity<List<Order>> getAllOrders() {
    List<Order> orders = cartService.getAllOrders();
    return ResponseEntity.ok(orders);
}
```

**Test:**
```bash
# Add to cart
curl -X POST "http://localhost:8082/cart/add?productId=1&quantity=2"

# View cart
curl http://localhost:8082/cart

# Checkout
curl -X POST http://localhost:8082/cart/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "cardType": "VISA",
    "cardNumber": "4111111111111111",
    "shippingAddress": "123 Main St"
  }'

# View orders
curl http://localhost:8082/cart/orders
```

---

## Step 4: Enhance Tracking Service (30 mins)

### 4.1 Create DTO

Create: `tracking-service/src/main/java/com/example/tracking/dto/` directory

Create: `tracking-service/src/main/java/com/example/tracking/dto/EnrichedTrackingDTO.java`

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
    private String productName;
    private String productDescription;
    private double productPrice;
    
    public EnrichedTrackingDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    
    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
}
```

### 4.2 Add Methods to TrackingService

Add these methods to `TrackingService.java`:

```java
// Add these imports at the top
import com.example.tracking.dto.EnrichedTrackingDTO;
import java.util.stream.Collectors;

// Add these fields
private final String CATALOGUE_URL = "http://localhost:8081/api/products/";
private final String PANIER_URL = "http://localhost:8082/cart/";

// Add these methods

public EnrichedTrackingDTO getEnrichedTrackingByOrderId(Long orderId) {
    TrackingInfo tracking = trackingRepository.findByOrderId(orderId)
        .orElseThrow(() -> new RuntimeException("Tracking not found for order: " + orderId));
    
    return enrichTrackingInfo(tracking);
}

public List<EnrichedTrackingDTO> getEnrichedTrackingsByProductId(Long productId) {
    List<TrackingInfo> trackings = trackingRepository.findByProductId(productId);
    return trackings.stream()
        .map(this::enrichTrackingInfo)
        .collect(Collectors.toList());
}

private EnrichedTrackingDTO enrichTrackingInfo(TrackingInfo tracking) {
    EnrichedTrackingDTO dto = new EnrichedTrackingDTO();
    dto.setId(tracking.getId());
    dto.setOrderId(tracking.getOrderId());
    dto.setProductId(tracking.getProductId());
    dto.setStatus(tracking.getStatus());
    dto.setCurrentLocation(tracking.getCurrentLocation());
    dto.setLastUpdate(tracking.getLastUpdate());
    
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
        System.err.println("Warning: Could not fetch product details: " + e.getMessage());
    }
    
    return dto;
}

public TrackingInfo cancelOrder(Long trackingId) {
    TrackingInfo tracking = trackingRepository.findById(trackingId)
        .orElseThrow(() -> new RuntimeException("Tracking not found: " + trackingId));
    
    if (!tracking.getStatus().equals("PENDING") && 
        !tracking.getStatus().equals("PREPARING")) {
        throw new RuntimeException(
            "Cannot cancel order in status: " + tracking.getStatus()
        );
    }
    
    try {
        Map<String, Object> order = restTemplate.getForObject(
            PANIER_URL + "orders/" + tracking.getOrderId(),
            Map.class
        );
        
        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
        
        for (Map<String, Object> item : items) {
            Long productId = Long.parseLong(item.get("productId").toString());
            int quantity = Integer.parseInt(item.get("quantity").toString());
            
            restTemplate.put(
                CATALOGUE_URL + productId + "/stock/restore?quantity=" + quantity,
                null
            );
        }
        
    } catch (Exception e) {
        throw new RuntimeException("Failed to cancel order: " + e.getMessage());
    }
    
    tracking.setStatus("CANCELLED");
    tracking.setLastUpdate(LocalDateTime.now());
    
    return trackingRepository.save(tracking);
}
```

### 4.3 Add Endpoints to TrackingController

Add these methods to `TrackingController.java`:

```java
// Add these imports
import com.example.tracking.dto.EnrichedTrackingDTO;

// Add these methods

@GetMapping("/order/{orderId}/enriched")
public ResponseEntity<EnrichedTrackingDTO> getEnrichedTrackingByOrderId(
    @PathVariable Long orderId
) {
    EnrichedTrackingDTO dto = trackingService.getEnrichedTrackingByOrderId(orderId);
    return ResponseEntity.ok(dto);
}

@GetMapping("/product/{productId}/enriched")
public ResponseEntity<List<EnrichedTrackingDTO>> getEnrichedTrackingsByProductId(
    @PathVariable Long productId
) {
    List<EnrichedTrackingDTO> dtos = trackingService.getEnrichedTrackingsByProductId(productId);
    return ResponseEntity.ok(dtos);
}

@PutMapping("/{id}/cancel")
public ResponseEntity<TrackingInfo> cancelOrder(@PathVariable Long id) {
    TrackingInfo tracking = trackingService.cancelOrder(id);
    return ResponseEntity.ok(tracking);
}
```

**Test:**
```bash
# After completing a checkout, get enriched tracking
curl "http://localhost:8084/api/tracking/order/1/enriched"

# Cancel an order
curl -X PUT "http://localhost:8084/api/tracking/1/cancel"
```

---

## Complete E2E Test Workflow

Run these commands in sequence to test the entire flow:

```bash
# 1. Check initial products and stock
curl http://localhost:8080/api/products

# 2. Add product to cart
curl -X POST "http://localhost:8080/cart/add?productId=1&quantity=2"

# 3. Add another product
curl -X POST "http://localhost:8080/cart/add?productId=2&quantity=1"

# 4. View cart
curl http://localhost:8080/cart

# 5. Checkout
curl -X POST http://localhost:8080/cart/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "cardType": "VISA",
    "cardNumber": "4111111111111111",
    "shippingAddress": "123 Main St, Paris"
  }'

# Note the orderId and trackingId from the response

# 6. Check stock was deducted
curl http://localhost:8080/api/products/1

# 7. View order details (replace {orderId} with actual ID)
curl http://localhost:8080/cart/orders/1

# 8. Track order with enriched data (replace {orderId})
curl http://localhost:8080/api/tracking/order/1/enriched

# 9. Update tracking status (replace {trackingId})
curl -X PUT "http://localhost:8080/api/tracking/1?status=SHIPPED&location=In Transit"

# 10. Cancel order (replace {trackingId})
curl -X PUT "http://localhost:8080/api/tracking/1/cancel"

# 11. Verify stock was restored
curl http://localhost:8080/api/products/1
```

---

## Troubleshooting

### Issue: Services not communicating

**Check:**
1. All services running?
   ```bash
   curl http://localhost:8081/api/products
   curl http://localhost:8082/cart
   curl http://localhost:8083/api/payments
   curl http://localhost:8084/api/tracking
   ```

2. Gateway routing correctly?
   - Check [gateway-service/target/classes/application.yml](gateway-service/target/classes/application.yml)

### Issue: RestTemplate errors

**Solution:** Ensure RestTemplate bean exists in Application class:

```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

### Issue: H2 Database errors

**Solution:** Check table creation in H2 console:
- Catalogue: http://localhost:8081/h2-console
- Panier: http://localhost:8082/h2-console
- Tracking: http://localhost:8084/h2-console

---

## Summary of Changes

| Service | Files Created | Files Modified | LOC Added |
|---------|---------------|----------------|-----------|
| Catalogue | 1 | 2 | ~100 |
| Panier | 7 | 2 | ~400 |
| Tracking | 1 | 2 | ~150 |
| **Total** | **9** | **6** | **~650** |

---

## Next Steps

1. ✅ Implement all code changes
2. Test each service individually
3. Test E2E workflow
4. Add error handling (GlobalExceptionHandler)
5. Add logging
6. Document API with Swagger
7. Write unit tests
8. Deploy to Docker

---

**Last Updated:** January 19, 2026
