package com.example.paiment_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.paiment_service.entity.PaimentItem;
import com.example.paiment_service.repository.PaimentItemRepository;

@Service
public class PaimentItemService {

    @Autowired
    private PaimentItemRepository paimentItemRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String PANIER_URL = "http://localhost:8082/cart";
    private final String TRACKING_URL = "http://localhost:8084/api/tracking";
    private final String CATALOGUE_URL = "http://localhost:8081/api/products";

    @SuppressWarnings("unchecked")
    public PaimentItem createPayment(PaimentItem paiment) {
        // Validate cart is not empty
        List<java.util.Map<String, Object>> cartItems = restTemplate.getForObject(PANIER_URL, List.class);
        
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cannot process payment: Cart is empty");
        }

        // Perform payment
        paiment.setApproved(true);
        PaimentItem savedPayment = paimentItemRepository.save(paiment);

        // Create tracking and reduce stock
        try {
            java.util.Map<String, Object> trackingRequest = new java.util.HashMap<>();
            trackingRequest.put("orderId", savedPayment.getCartId());
            trackingRequest.put("productId", 1L);
            trackingRequest.put("status", "PAID");
            
            restTemplate.postForObject(TRACKING_URL, trackingRequest, Object.class);

            // Reduce stock for each item
            for (java.util.Map<String, Object> item : cartItems) {
                Long productId = ((Number) item.get("productId")).longValue();
                Integer quantity = ((Number) item.get("quantity")).intValue();
                
                String reduceStockUrl = CATALOGUE_URL + "/" + productId + "/reduceStock?quantity=" + quantity;
                restTemplate.postForObject(reduceStockUrl, null, Void.class);
            }

        } catch (Exception e) {
            System.err.println("Error in post-payment processing: " + e.getMessage());
        }

        return savedPayment;
    }

    public PaimentItem processPayment(Long id) {
        Optional<PaimentItem> paiment = paimentItemRepository.findById(id);
        if (paiment.isPresent()) {
            PaimentItem p = paiment.get();
            p.setApproved(true);
            return paimentItemRepository.save(p);
        }
        return null;
    }

    public Optional<PaimentItem> getPayment(Long id) {
        return paimentItemRepository.findById(id);
    }

    public Optional<PaimentItem> getPaymentByCardNumber(String cardNumber) {
        return paimentItemRepository.findByCardNumber(cardNumber);
    }

    public List<PaimentItem> getAllPayments() {
        return paimentItemRepository.findAll();
    }

    public void deletePayment(Long id) {
        paimentItemRepository.deleteById(id);
    }
}