package com.ridesharingapp.demo.repository;

import com.ridesharingapp.demo.model.DriverLocation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverLocationRepository extends CrudRepository<DriverLocation, String> {
}
