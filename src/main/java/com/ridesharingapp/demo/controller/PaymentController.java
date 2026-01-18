package com.ridesharingapp.demo.controller;

import com.ridesharingapp.demo.dto.PaymentRequest;
import com.ridesharingapp.demo.service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final RideService rideService;

    public PaymentController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<Void> processPayment(@RequestBody @Valid PaymentRequest request) {
        boolean success = rideService.processPayment(request);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
