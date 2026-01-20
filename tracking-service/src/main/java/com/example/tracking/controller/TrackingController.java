package com.example.tracking.controller;

import com.example.tracking.entity.TrackingInfo;
import com.example.tracking.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/product/{productId}")
    public List<TrackingInfo> getTrackingByProductId(@PathVariable Long productId) {
        return trackingService.getTrackingByProductId(productId);
    }

    @PostMapping
    public TrackingInfo createTracking(@Valid @RequestBody TrackingInfo trackingInfo) {
        return trackingService.createTracking(trackingInfo);
    }

    @PutMapping("/{id}")
    public TrackingInfo updateStatus(@PathVariable Long id, 
                                    @RequestParam String status,
                                    @RequestParam(required = false) String location) {
        return trackingService.updateStatus(id, status, location);
    }

    @DeleteMapping("/{id}")
    public void deleteTracking(@PathVariable Long id) {
        trackingService.deleteTracking(id);
    }

    // Get enriched tracking with product details
    @GetMapping("/order/{orderId}/enriched")
    public Map<String, Object> getEnrichedTracking(@PathVariable Long orderId) {
        return trackingService.getEnrichedTracking(orderId);
    }

    // Cancel order
    @PutMapping("/order/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId) {
        trackingService.cancelOrder(orderId);
        return "Order cancelled successfully";
    }
}
