package com.example.panier.service;

import com.example.panier.entity.CartItem;
import com.example.panier.entity.Order;
import com.example.panier.entity.OrderItem;
import com.example.panier.repository.CartItemRepository;
import com.example.panier.repository.OrderRepository;
import com.example.panier.dto.CheckoutRequest;
import com.example.panier.dto.CheckoutResponse;
import com.example.panier.dto.TrackingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String CATALOGUE_URL = "http://localhost:8081/api/products/";
    private final String TRACKING_URL = "http://localhost:8084/api/tracking";

    public CartItem addToCart(Long productId, Integer quantity) {
        // 1. Get product from catalogue service
        @SuppressWarnings("unchecked")
        Map<String, Object> product = restTemplate.getForObject(CATALOGUE_URL + productId, Map.class);

        if (product == null) {
            throw new RuntimeException("Product not found in catalogue");
        }

        // 2. Check stock availability
        String checkStockUrl = CATALOGUE_URL + productId + "/check-stock?quantity=" + quantity;
        @SuppressWarnings("unchecked")
        Map<String, Object> stockResponse = restTemplate.getForObject(checkStockUrl, Map.class);
        
        if (stockResponse != null && !(Boolean) stockResponse.get("available")) {
            throw new RuntimeException("Insufficient stock. Available: " + stockResponse.get("currentStock"));
        }

        // 3. Extract price
        double price = Double.parseDouble(product.get("price").toString());

        // 4. Check if product already in cart
        CartItem item = cartItemRepository.findByProductId(productId)
                .orElse(new CartItem(null, productId, 0, price));

        item.setQuantity(item.getQuantity() + quantity);
        item.setUnitPrice(price);

        return cartItemRepository.save(item);
    }


    // Complete checkout process with order creation and tracking
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        // 1. Get cart items
        List<CartItem> cartItems = cartItemRepository.findAll();
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 2. Validate stock for all items
        for (CartItem cartItem : cartItems) {
            String checkStockUrl = CATALOGUE_URL + cartItem.getProductId() + 
                    "/check-stock?quantity=" + cartItem.getQuantity();
            @SuppressWarnings("unchecked")
            Map<String, Object> stockResponse = restTemplate.getForObject(checkStockUrl, Map.class);
            
            if (stockResponse != null && !(Boolean) stockResponse.get("available")) {
                throw new RuntimeException("Insufficient stock for product ID: " + cartItem.getProductId() + 
                        ". Available: " + stockResponse.get("currentStock"));
            }
        }

        // 3. Create order
        Order order = new Order(request.getCustomerName(), request.getCustomerEmail(), request.getShippingAddress());
        double totalAmount = 0;

        // 4. Convert cart items to order items
        for (CartItem cartItem : cartItems) {
            // Get product details
            @SuppressWarnings("unchecked")
            Map<String, Object> product = restTemplate.getForObject(
                    CATALOGUE_URL + cartItem.getProductId(), Map.class);
            
            String productName = product != null ? product.get("name").toString() : "Unknown";
            
            OrderItem orderItem = new OrderItem(
                    cartItem.getProductId(),
                    productName,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice()
            );
            order.addItem(orderItem);
            totalAmount += orderItem.getSubtotal();
        }

        order.setTotalAmount(totalAmount);
        order.setStatus("CONFIRMED");
        Order savedOrder = orderRepository.save(order);

        // 5. Reduce stock in catalogue service
        for (CartItem cartItem : cartItems) {
            String reduceStockUrl = CATALOGUE_URL + cartItem.getProductId() + 
                    "/reduce-stock?quantity=" + cartItem.getQuantity();
            restTemplate.put(reduceStockUrl, null);
        }

        // 6. Create tracking in tracking service
        Long trackingId = null;
        try {
            for (OrderItem orderItem : savedOrder.getItems()) {
                TrackingRequest trackingRequest = new TrackingRequest(
                        savedOrder.getId(),
                        orderItem.getProductId(),
                        "PREPARING",
                        "Warehouse"
                );
                
                @SuppressWarnings("unchecked")
                Map<String, Object> trackingResponse = restTemplate.postForObject(
                        TRACKING_URL, trackingRequest, Map.class);
                
                if (trackingResponse != null && trackingId == null) {
                    Object idObj = trackingResponse.get("id");
                    if (idObj != null) {
                        trackingId = Long.parseLong(idObj.toString());
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the order
            System.err.println("Failed to create tracking: " + e.getMessage());
        }

        // 7. Clear cart
        cartItemRepository.deleteAll();

        // 8. Return response
        return new CheckoutResponse(
                savedOrder.getId(),
                trackingId,
                totalAmount,
                "CONFIRMED",
                "Order placed successfully!"
        );
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get order by ID
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }
    
    public List<CartItem> getCart() {
        return cartItemRepository.findAll();
    }

    public void clearCart() {
        cartItemRepository.deleteAll();
    }
}