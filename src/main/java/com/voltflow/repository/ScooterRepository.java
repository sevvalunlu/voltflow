package com.voltflow.repository;

import com.voltflow.entity.Scooter;
import com.voltflow.entity.ScooterStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScooterRepository extends JpaRepository<Scooter, Long> {
    
    List<Scooter> findByStatus(ScooterStatus status);

    List<Scooter> findByBatteryLevelLessThan(Double batteryLevel); 

    java.util.Optional<Scooter> findBySerialNumber(String serialNumber);
}