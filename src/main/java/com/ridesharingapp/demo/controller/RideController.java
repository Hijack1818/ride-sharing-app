package com.ridesharingapp.demo.controller;

import com.ridesharingapp.demo.dto.RideRequest;
import com.ridesharingapp.demo.model.Ride;
import com.ridesharingapp.demo.service.RideService;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/v1/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<Ride> createRide(@RequestBody @Valid RideRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Ride ride = rideService.createRide(request, idempotencyKey);
        return ResponseEntity.status(201).body(ride);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ride> getRide(@PathVariable @NonNull String id) {
        Ride ride = rideService.getRide(id);
        if (ride == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/available")
    public ResponseEntity<java.util.List<Ride>> getAvailableRides(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "5.0") Double radius) {
        if (lat == null || lng == null) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(rideService.getAvailableRides(lat, lng, radius));
    }
}
