package com.ridesharingapp.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RideRequest {
    @NotBlank(message = "Passenger ID is required")
    private String passengerId;
    @NotBlank(message = "Pickup location is required")
    private String pickupLocation;
    @NotBlank(message = "Dropoff location is required")
    private String dropoffLocation;

    @NotNull(message = "Pickup latitude is required")
    private Double pickupLat;
    @NotNull(message = "Pickup longitude is required")
    private Double pickupLng;
    @NotNull(message = "Dropoff latitude is required")
    private Double dropoffLat;
    @NotNull(message = "Dropoff longitude is required")
    private Double dropoffLng;

    @NotBlank(message = "Tier is required")
    private String tier; // e.g., "STANDARD", "PREMIUM"

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // e.g., "CASH", "CARD"

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public Double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(Double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public Double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(Double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public Double getDropoffLat() {
        return dropoffLat;
    }

    public void setDropoffLat(Double dropoffLat) {
        this.dropoffLat = dropoffLat;
    }

    public Double getDropoffLng() {
        return dropoffLng;
    }

    public void setDropoffLng(Double dropoffLng) {
        this.dropoffLng = dropoffLng;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
