package com.ridesharingapp.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class PaymentRequest {
    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @Min(value = 0, message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Payment method is required")
    private String method;

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
