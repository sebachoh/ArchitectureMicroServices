package com.example.tracking.service;

import com.example.tracking.entity.TrackingInfo;
import com.example.tracking.repository.TrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TrackingService {

    @Autowired
    private TrackingRepository trackingRepository;

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
}
