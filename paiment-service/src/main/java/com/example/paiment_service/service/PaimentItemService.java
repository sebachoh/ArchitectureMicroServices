package com.example.paiment_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.paiment_service.entity.PaimentItem;
import com.example.paiment_service.repository.PaimentItemRepository;

@Service
public class PaimentItemService {

    @Autowired
    private PaimentItemRepository paimentItemRepository;

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    private final String PANIER_URL = "http://localhost:8082/cart";
    private final String TRACKING_URL = "http://localhost:8084/api/tracking";
    private final String CATALOGUE_URL = "http://localhost:8081/api/products";

    @SuppressWarnings("unchecked")
    public PaimentItem createPayment(PaimentItem paiment) {
        // 1. Validate Cart (Simulated by checking if cart is not empty)
        // In a real scenario, we would pass paiment.getCartId() to validating endpoint
        java.util.List<java.util.Map<String, Object>> cartItems = restTemplate.getForObject(PANIER_URL, java.util.List.class);
        
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cannot process payment: Cart is empty");
        }

        // 2. Perform Payment (Simulated)
        paiment.setApproved(true);
        PaimentItem savedPayment = paimentItemRepository.save(paiment);

        // 3. Create Tracking
        try {
            java.util.Map<String, Object> trackingRequest = new java.util.HashMap<>();
            trackingRequest.put("orderId", savedPayment.getCartId()); // Linking Cart ID as Order ID
            trackingRequest.put("productId", 1L); // Placeholder as we treat cart as bulk, or logic needs refinement
            trackingRequest.put("status", "PAID");
            
            restTemplate.postForObject(TRACKING_URL, trackingRequest, Object.class);

            // 4. Reduce Stock in Catalogue
            for (java.util.Map<String, Object> item : cartItems) {
                Long productId = ((Number) item.get("productId")).longValue();
                Integer quantity = ((Number) item.get("quantity")).intValue();
                
                String reduceStockUrl = CATALOGUE_URL + "/" + productId + "/reduceStock?quantity=" + quantity;
                restTemplate.postForObject(reduceStockUrl, null, Void.class);
                System.out.println("Stock reduced successfully for Product ID: " + productId + ", Quantity: " + quantity);
            }

        } catch (Exception e) {
            System.err.println("Error in post-payment processing: " + e.getMessage());
            // Don't fail payment if calls fail, just log (in real system, use distributed transaction/saga)
        }

        return savedPayment;
    }

    @SuppressWarnings("null")
    public PaimentItem processPayment(Long id) {
        Optional<PaimentItem> paiment = paimentItemRepository.findById(id);
        if (paiment.isPresent()) {
            PaimentItem p = paiment.get();
            p.setApproved(true);
            return paimentItemRepository.save(p);
        }
        return null;
    }

    @SuppressWarnings("null")
    public Optional<PaimentItem> getPayment(Long id) {
        return paimentItemRepository.findById(id);
    }

    public Optional<PaimentItem> getPaymentByCardNumber(String cardNumber) {
        return paimentItemRepository.findByCardNumber(cardNumber);
    }

    public List<PaimentItem> getAllPayments() {
        return paimentItemRepository.findAll();
    }

    @SuppressWarnings("null")
    public void deletePayment(Long id) {
        paimentItemRepository.deleteById(id);
    }
}