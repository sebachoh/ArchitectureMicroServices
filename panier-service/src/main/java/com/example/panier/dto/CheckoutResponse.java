package com.example.panier.dto;

public class CheckoutResponse {
    
    private Long orderId;
    private Long trackingId;
    private double totalAmount;
    private String status;
    private String message;

    public CheckoutResponse() {
    }

    public CheckoutResponse(Long orderId, Long trackingId, double totalAmount, String status, String message) {
        this.orderId = orderId;
        this.trackingId = trackingId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(Long trackingId) {
        this.trackingId = trackingId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
