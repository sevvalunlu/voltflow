package com.voltflow.repository;

import com.voltflow.entity.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {
    List<MaintenanceLog> findByScooterId(Long scooterId);

    @Query("SELECT COALESCE(SUM(m.cost), 0.0) FROM MaintenanceLog m")
    Double getTotalMaintenanceCost();
}