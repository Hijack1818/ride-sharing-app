package com.ridesharingapp.demo.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "rides", indexes = {
        @Index(name = "idx_ride_status_created_at", columnList = "status, createdAt"),
        @Index(name = "idx_ride_passenger_id", columnList = "passengerId"),
        @Index(name = "idx_ride_driver_id", columnList = "driverId")
})
public class Ride implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String passengerId;
    private String driverId;
    private String pickupLocation;
    private String dropoffLocation;
    @Enumerated(EnumType.STRING)
    private RideStatus status;
    private java.time.LocalDateTime createdAt;

    private Double pickupLat;
    private Double pickupLng;
    private Double dropoffLat;
    private Double dropoffLng;
    private Double fare;
    private String currency;
    @NotBlank
    private String tier;

    @NotBlank
    private String paymentMethod;

    @Version
    private Long version;

    public enum RideStatus {
        REQUESTED, MATCHING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public Ride() {
    }

    public Ride(String id, String passengerId, Double pickupLat, Double pickupLng, Double dropoffLat,
            Double dropoffLng) {
        this.id = id;
        this.passengerId = passengerId;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.dropoffLat = dropoffLat;
        this.dropoffLng = dropoffLng;
        this.status = RideStatus.REQUESTED;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
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

    public Double getFare() {
        return fare;
    }

    public void setFare(Double fare) {
        this.fare = fare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
