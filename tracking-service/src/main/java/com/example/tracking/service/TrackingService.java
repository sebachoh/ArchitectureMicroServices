package com.example.tracking.service;

import com.example.tracking.entity.TrackingInfo;
import com.example.tracking.repository.TrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TrackingService {

    @Autowired
    private TrackingRepository trackingRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String CATALOGUE_URL = "http://localhost:8081/api/products/";

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

    // Get enriched tracking with product details
    public Map<String, Object> getEnrichedTracking(Long orderId) {
        TrackingInfo tracking = trackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Tracking not found for order: " + orderId));
        
        Map<String, Object> enrichedData = new HashMap<>();
        enrichedData.put("tracking", tracking);
        
        // Get product details from catalogue
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> product = restTemplate.getForObject(
                    CATALOGUE_URL + tracking.getProductId(), Map.class);
            enrichedData.put("productDetails", product);
        } catch (Exception e) {
            enrichedData.put("productDetails", null);
            enrichedData.put("error", "Could not fetch product details");
        }
        
        return enrichedData;
    }

    // Cancel order and restore stock
    public void cancelOrder(Long orderId) {
        TrackingInfo tracking = trackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Tracking not found for order: " + orderId));
        
        // Update tracking status
        tracking.setStatus("CANCELLED");
        tracking.setLastUpdate(LocalDateTime.now());
        trackingRepository.save(tracking);
        
        // Restore stock in catalogue (this would need order quantity info)
        // For now, we'll just mark it cancelled
        // In a real system, you'd get quantity from the order
    }
}
