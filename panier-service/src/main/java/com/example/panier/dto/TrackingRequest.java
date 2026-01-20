package com.example.panier.dto;

public class TrackingRequest {
    
    private Long orderId;
    private Long productId;
    private String status;
    private String currentLocation;

    public TrackingRequest() {
    }

    public TrackingRequest(Long orderId, Long productId, String status, String currentLocation) {
        this.orderId = orderId;
        this.productId = productId;
        this.status = status;
        this.currentLocation = currentLocation;
    }

    // Getters and Setters
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
}
