package com.example.tracking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class TrackingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Status cannot be null")
    private String status;

    private String currentLocation;

    private LocalDateTime lastUpdate;

    // Default constructor required by JPA
    public TrackingInfo() {
    }

    public TrackingInfo(Long id, Long orderId, Long productId, String status, 
    String currentLocation, LocalDateTime lastUpdate) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.status = status;
        this.currentLocation = currentLocation;
        this.lastUpdate = lastUpdate;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
