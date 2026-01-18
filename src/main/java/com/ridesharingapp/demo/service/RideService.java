package com.ridesharingapp.demo.service;

import com.ridesharingapp.demo.dto.LocationUpdate;
import com.ridesharingapp.demo.dto.PaymentRequest;
import com.ridesharingapp.demo.dto.RideRequest;
import com.ridesharingapp.demo.model.Ride;
import com.ridesharingapp.demo.repository.RideRepository;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableScheduling
public class RideService {

    private final RideRepository rideRepository;
    private final StringRedisTemplate redisTemplate;
    private static final String DRIVER_GEO_KEY = "ACTIVE_DRIVERS";

    public RideService(RideRepository rideRepository, StringRedisTemplate redisTemplate) {
        this.rideRepository = rideRepository;
        this.redisTemplate = redisTemplate;
    }

    // Run every 10 seconds to check for expired rides
    @Scheduled(fixedRate = 10000)
    public void checkRideTimeouts() {
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusMinutes(5);
        List<Ride> expiredRides = rideRepository.findAllByStatusAndCreatedAtBefore(Ride.RideStatus.REQUESTED, cutoff);
        for (Ride ride : expiredRides) {
            ride.setStatus(Ride.RideStatus.CANCELLED);
            rideRepository.save(ride);
            System.out.println("Ride " + ride.getId() + " cancelled due to timeout.");
        }
    }

    public Ride createRide(RideRequest request) {
        return createRide(request, null);
    }

    @Transactional
    @CachePut(value = "rides", key = "#result.id")
    public Ride createRide(RideRequest request, String idempotencyKey) {
        if (idempotencyKey != null) {
            String cachedRideId = redisTemplate.opsForValue().get("idempotency:" + idempotencyKey);
            if (cachedRideId != null) {
                return getRide(cachedRideId);
            }
        }

        String id = UUID.randomUUID().toString();
        Ride ride = new Ride(id, request.getPassengerId(), request.getPickupLat(), request.getPickupLng(),
                request.getDropoffLat(), request.getDropoffLng());

        // Populate text locations and calculate mock fare
        ride.setPickupLocation(request.getPickupLocation());
        ride.setDropoffLocation(request.getDropoffLocation());
        ride.setCurrency("USD");
        ride.setTier(request.getTier());
        ride.setPaymentMethod(request.getPaymentMethod());
        ride.setFare(calculateMockFare(request));

        ride = rideRepository.save(ride);

        if (idempotencyKey != null) {
            redisTemplate.opsForValue().setIfAbsent("idempotency:" + idempotencyKey, ride.getId(),
                    Duration.ofHours(24));
        }

        // Broadcast to all nearby drivers
        broadcastKeyRide(ride);

        return ride;
    }

    private Double calculateMockFare(RideRequest request) {
        double distance = 0.0;
        if (request.getPickupLat() != null && request.getDropoffLat() != null) {
            distance = Math.sqrt(Math.pow(request.getPickupLat() - request.getDropoffLat(), 2)
                    + Math.pow(request.getPickupLng() - request.getDropoffLng(), 2)) * 111; // Rough km conversion
        }
        return 5.0 + (distance * 1.5); // Base $5 + $1.5/km
    }

    // Keeping existing broadcastKeyRide method...
    private void broadcastKeyRide(Ride ride) {
        if (ride.getPickupLat() == null || ride.getPickupLng() == null)
            return;

        Point pickupPoint = new Point(ride.getPickupLng(), ride.getPickupLat());
        Distance radius = new Distance(5, Metrics.KILOMETERS);
        Circle circle = new Circle(pickupPoint, radius);
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance().sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(DRIVER_GEO_KEY,
                circle, args);

        if (results != null) {
            System.out.println("Broadcasting Ride " + ride.getId() + " (Fare: $" + String.format("%.2f", ride.getFare())
                    + ") to " + results.getContent().size() + " drivers:");
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                String driverId = result.getContent().getName();
                System.out.println(" -> Notifying Driver " + driverId + ": New Ride from " + ride.getPickupLocation()
                        + " to " + ride.getDropoffLocation());
                // In real app, Push notification here
            }
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "rides", key = "#id")
    public Ride getRide(@NonNull String id) {
        return rideRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Ride> getAvailableRides(@NonNull Double lat, @NonNull Double lng, double radiusKm) {
        List<Ride> allRequested = rideRepository.findByStatus(Ride.RideStatus.REQUESTED);
        return allRequested.stream()
                .filter(ride -> {
                    if (ride.getPickupLat() == null || ride.getPickupLng() == null)
                        return false;
                    double distance = calculateDistance(lat, lng, ride.getPickupLat(), ride.getPickupLng());
                    return distance <= radiusKm;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // convert to kilometers
    }

    @Async
    public void updateDriverLocation(@NonNull String driverId, @NonNull LocationUpdate location) {
        redisTemplate.opsForGeo().add(DRIVER_GEO_KEY, new Point(location.getLongitude(), location.getLatitude()),
                driverId);
        System.out.println(
                "Driver " + driverId + " location updated in Redis: " + location.getLatitude() + ", "
                        + location.getLongitude());
    }

    @Transactional
    @CachePut(value = "rides", key = "#result.id")
    public Ride acceptRide(@NonNull String rideId, @NonNull String driverId) {
        // Enforce Driver Consistency: Check if driver is already on an active trip
        long activeRides = rideRepository.countByDriverIdAndStatusIn(driverId,
                List.of(Ride.RideStatus.ACCEPTED, Ride.RideStatus.IN_PROGRESS));

        if (activeRides > 0) {
            throw new IllegalStateException("Driver is already on an active trip");
        }

        Ride ride = getRide(rideId);
        if (ride != null && ride.getStatus() == Ride.RideStatus.REQUESTED) {
            ride.setDriverId(driverId);
            ride.setStatus(Ride.RideStatus.ACCEPTED);
            return rideRepository.save(ride);
        }
        return ride;
    }

    @Transactional
    @CachePut(value = "rides", key = "#result.id")
    public Ride endTrip(@NonNull String rideId) {
        Ride ride = getRide(rideId);
        if (ride != null) {
            ride.setStatus(Ride.RideStatus.COMPLETED);

            long durationMinutes = Duration.between(ride.getCreatedAt(), LocalDateTime.now())
                    .toMinutes();
            if (durationMinutes < 5)
                durationMinutes = 5;

            double convenienceFee = "PREMIUM".equalsIgnoreCase(ride.getTier()) ? 10.0 : 0.0;
            double finalFare = ride.getFare() + (durationMinutes * 0.5) + convenienceFee;
            ride.setFare(finalFare);

            return rideRepository.save(ride);
        }
        return ride;
    }

    public boolean processPayment(PaymentRequest payment) {
        System.out.println("Processing payment of " + payment.getAmount() + " for ride " + payment.getRideId() + " via "
                + payment.getMethod());
        return true;
    }
}
