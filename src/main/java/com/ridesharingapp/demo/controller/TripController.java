package com.ridesharingapp.demo.controller;

import com.ridesharingapp.demo.dto.Receipt;
import com.ridesharingapp.demo.model.Ride;
import com.ridesharingapp.demo.service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trips")
public class TripController {

    private final RideService rideService;

    public TripController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<Ride> endTrip(@PathVariable @NonNull String id) {
        Ride ride = rideService.endTrip(id);
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<Receipt> getReceipt(@PathVariable @NonNull String id) {
        Ride ride = rideService.getRide(id);
        if (ride != null && ride.getStatus() == Ride.RideStatus.COMPLETED) {
            Receipt receipt = new Receipt(
                    ride.getId(),
                    ride.getFare(),
                    ride.getCurrency(),
                    ride.getCreatedAt());
            return ResponseEntity.ok(receipt);
        }
        return ResponseEntity.notFound().build();
    }
}
