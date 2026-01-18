package com.ridesharingapp.demo.dto;

import java.time.LocalDateTime;

public class Receipt {
    private String rideId;
    private Double amount;
    private String currency;
    private LocalDateTime date;

    public Receipt(String rideId, Double amount, String currency, LocalDateTime date) {
        this.rideId = rideId;
        this.amount = amount;
        this.currency = currency;
        this.date = date;
    }

    // Getters
    public String getRideId() {
        return rideId;
    }

    public Double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
