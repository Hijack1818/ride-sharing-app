package com.ridesharingapp.demo.repository;

import com.ridesharingapp.demo.model.Ride;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideRepository extends JpaRepository<Ride, String> {
    List<Ride> findAllByStatusAndCreatedAtBefore(Ride.RideStatus status, LocalDateTime createdAt);

    // Check if driver is already on an active ride
    long countByDriverIdAndStatusIn(String driverId, List<Ride.RideStatus> statuses);

    List<Ride> findByStatus(Ride.RideStatus status);
}
