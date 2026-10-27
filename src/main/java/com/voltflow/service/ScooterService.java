package com.voltflow.service;

import com.voltflow.entity.Location;
import com.voltflow.entity.MaintenanceLog;
import com.voltflow.entity.Scooter;
import com.voltflow.entity.ScooterStatus;
import com.voltflow.repository.MaintenanceLogRepository;
import com.voltflow.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScooterService {

    private final ScooterRepository scooterRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final NotificationService notificationService;

    public List<Scooter> getAllScooters() {
        return scooterRepository.findAll();
    }

    public Scooter getScooterById(Long id) {
        return scooterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scooter bulunamadı: " + id));
    }

    private void clearLocationIfUnavailable(Scooter scooter) {
        if (scooter.getStatus() == ScooterStatus.BAKIMDA || scooter.getStatus() == ScooterStatus.SARJDA) {
            scooter.setLocation(new Location(null, null));
        }
    }

    private Location generateRandomLocation() {
        double minLat = 40.900000;
        double maxLat = 41.100000;
        double minLng = 28.800000;
        double maxLng = 29.100000;

        double lat = Math.round((minLat + Math.random() * (maxLat - minLat)) * 1000000.0) / 1000000.0;
        double lng = Math.round((minLng + Math.random() * (maxLng - minLng)) * 1000000.0) / 1000000.0;

        return new Location(lat, lng);
    }

    @Transactional
    public Scooter saveScooter(Scooter scooter) {
        clearLocationIfUnavailable(scooter);
        Scooter saved = scooterRepository.save(scooter);
        checkBatteryAndCreateLog(saved);
        return saved;
    }

    @Transactional
    public Scooter updateScooter(Long id, Scooter updated) {
        Scooter existing = getScooterById(id);
        existing.setSerialNumber(updated.getSerialNumber());
        existing.setModel(updated.getModel());
        existing.setBatteryLevel(updated.getBatteryLevel());
        existing.setLocation(updated.getLocation());
        existing.setStatus(updated.getStatus());

        clearLocationIfUnavailable(existing);

        Scooter saved = scooterRepository.save(existing);
        checkBatteryAndCreateLog(saved);
        return saved;
    }

    @Transactional
    public void deleteScooter(Long id) {
        Scooter scooter = getScooterById(id);
        maintenanceLogRepository.deleteAll(maintenanceLogRepository.findByScooterId(id));
        scooterRepository.delete(scooter);
    }

    @Transactional
    public void checkBatteryAndCreateLog(Scooter scooter) {
        if (scooter.getBatteryLevel() <= 10.0 && scooter.getStatus() != ScooterStatus.SARJDA) {
            scooter.setStatus(ScooterStatus.SARJDA);
            scooter.setLocation(new Location(null, null));

            MaintenanceLog log = MaintenanceLog.builder()
                    .faultType("OTOMATİK ŞARJ İŞ EMRİ")
                    .technician("Sistem (Otomasyon)")
                    .completedAt(LocalDateTime.now())
                    .cost(75.0) // Sahadan toplama maliyeti
                    .scooter(scooter)
                    .build();
            maintenanceLogRepository.save(log);
            notificationService.createNotification("⚠️ " + scooter.getSerialNumber() + " şarjı kritik seviyeye düştüğü için otomatik şarja alındı.", "WARNING");
        }
    }

    @Transactional
    public void finishMaintenance(Long id) {
        Scooter scooter = getScooterById(id);
        if (scooter.getStatus() == ScooterStatus.BAKIMDA) {
            scooter.setStatus(ScooterStatus.YAYINDA);
            scooter.setLocation(generateRandomLocation());
            
            MaintenanceLog log = MaintenanceLog.builder()
                    .faultType("BAKIM TAMAMLANDI VE SAHAYA SÜRÜLDÜ")
                    .technician("Operasyon Ekibi")
                    .completedAt(LocalDateTime.now())
                    .cost(450.0)
                    .scooter(scooter)
                    .build();
            maintenanceLogRepository.save(log);
            scooterRepository.save(scooter);
            notificationService.createNotification("🔧 " + scooter.getSerialNumber() + " bakımı tamamlandı ve rastgele GPS atanarak sahaya sürüldü.", "SUCCESS");
        }
    }

    public List<Scooter> getCriticalScooters() {
        return scooterRepository.findByBatteryLevelLessThan(20.0);
    }

    public List<Scooter> getScootersByStatus(ScooterStatus status) {
        return scooterRepository.findByStatus(status);
    }

    public List<Scooter> getScootersByBatteryRange(Double min, Double max) {
        return scooterRepository.findAll().stream()
                .filter(s -> s.getBatteryLevel() >= min && s.getBatteryLevel() <= max)
                .collect(java.util.stream.Collectors.toList());
    }

    public long countByStatus(ScooterStatus status) {
        return scooterRepository.findByStatus(status).size();
    }

    public double getAverageBattery() {
        return scooterRepository.findAll().stream()
                .mapToDouble(Scooter::getBatteryLevel)
                .average()
                .orElse(0.0);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processChargingScooters() {
        List<Scooter> chargingScooters = scooterRepository.findByStatus(ScooterStatus.SARJDA);
        for (Scooter scooter : chargingScooters) {
            double newBattery = scooter.getBatteryLevel() + 3.33; 
            
            if (newBattery >= 100.0) {
                scooter.setBatteryLevel(100.0);
                scooter.setStatus(ScooterStatus.YAYINDA);
                scooter.setLocation(generateRandomLocation()); 

                MaintenanceLog log = MaintenanceLog.builder()
                        .faultType("ŞARJ TAMAMLANDI VE SAHAYA SÜRÜLDÜ")
                        .technician("Sistem (Otomasyon)")
                        .completedAt(LocalDateTime.now())
                        .cost(15.0)
                        .scooter(scooter)
                        .build();
                maintenanceLogRepository.save(log);
                notificationService.createNotification("⚡ " + scooter.getSerialNumber() + " şarjı tamamlandı ve sahaya dağıtıldı.", "INFO");
            } else {
                scooter.setBatteryLevel(Math.round(newBattery * 10.0) / 10.0);
            }
            scooterRepository.save(scooter);
        }
    }


    @Scheduled(fixedRate = 60000)
    @Transactional
    public void simulateBatteryDrain() {
        List<Scooter> activeScooters = scooterRepository.findByStatus(ScooterStatus.YAYINDA);
        for (Scooter scooter : activeScooters) {
            double newBattery = scooter.getBatteryLevel() - 0.1;

            if (newBattery <= 0) {
                newBattery = 0.0;
            }

            scooter.setBatteryLevel(Math.round(newBattery * 10.0) / 10.0);

            if (scooter.getBatteryLevel() <= 10.0 && scooter.getStatus() != ScooterStatus.SARJDA) {
                checkBatteryAndCreateLog(scooter);
            }

            scooterRepository.save(scooter);
        }
    }

    public Double getTotalCost() {
        return maintenanceLogRepository.getTotalMaintenanceCost();
    }

    public boolean isSerialNumberUnique(String serialNumber, Long currentId) {
        java.util.Optional<Scooter> existingScooter = scooterRepository.findBySerialNumber(serialNumber);
        
        if (existingScooter.isPresent()) {
            return existingScooter.get().getId().equals(currentId);
        }
        return true; 
    }

    @Transactional
    public void updateScooterLocation(Long id, Double latitude, Double longitude) {
        Scooter scooter = getScooterById(id);
        scooter.setLocation(new Location(latitude, longitude));
        scooterRepository.save(scooter);
    }
}