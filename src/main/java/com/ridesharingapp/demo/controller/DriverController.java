package com.ridesharingapp.demo.controller;

import com.ridesharingapp.demo.dto.LocationUpdate;
import com.ridesharingapp.demo.model.Ride;
import com.ridesharingapp.demo.service.RideService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/v1/drivers")
public class DriverController {

    private final RideService rideService;

    public DriverController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/{id}/location")
    public ResponseEntity<Void> updateLocation(@PathVariable @NonNull String id,
            @RequestBody @Valid @NonNull LocationUpdate location) {
        rideService.updateDriverLocation(id, location);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Ride> acceptRide(@PathVariable @NonNull String id, @RequestParam @NonNull String rideId) {
        Ride ride = rideService.acceptRide(rideId, id);
        return ResponseEntity.ok(ride);
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<Ride> endTrip(@PathVariable @NonNull String id, @RequestParam @NonNull String rideId) {
        Ride ride = rideService.endTrip(rideId);
        return ResponseEntity.ok(ride);
    }
}
