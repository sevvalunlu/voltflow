package com.voltflow.repository;

import com.voltflow.entity.Scooter;
import com.voltflow.entity.ScooterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScooterRepository extends JpaRepository<Scooter, Long> {
    
    List<Scooter> findByStatus(ScooterStatus status);

    List<Scooter> findByBatteryLevelLessThan(Double batteryLevel); 

    java.util.Optional<Scooter> findBySerialNumber(String serialNumber);
}