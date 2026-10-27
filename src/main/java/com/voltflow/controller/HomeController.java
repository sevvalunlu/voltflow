package com.voltflow.controller;

import com.voltflow.entity.ScooterStatus;
import com.voltflow.service.ScooterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ScooterService scooterService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalCount", scooterService.getAllScooters().size());
        model.addAttribute("criticalCount", scooterService.getCriticalScooters().size());
        model.addAttribute("activeCount", scooterService.countByStatus(ScooterStatus.YAYINDA));
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalCount", scooterService.getAllScooters().size());
        model.addAttribute("activeCount", scooterService.countByStatus(ScooterStatus.YAYINDA));
        model.addAttribute("maintenanceCount", scooterService.countByStatus(ScooterStatus.BAKIMDA));
        model.addAttribute("chargingCount", scooterService.countByStatus(ScooterStatus.SARJDA));
        model.addAttribute("criticalCount", scooterService.getCriticalScooters().size());
        model.addAttribute("avgBattery", Math.round(scooterService.getAverageBattery() * 10.0) / 10.0);
        model.addAttribute("criticalScooters", scooterService.getCriticalScooters());
        model.addAttribute("maintenanceScooters", scooterService.getScootersByStatus(ScooterStatus.BAKIMDA));
        model.addAttribute("totalCost", scooterService.getTotalCost());
        return "dashboard";
    }

    @GetMapping("/map")
    public String showLiveMap(Model model) {

        model.addAttribute("scooters", scooterService.getAllScooters());
        return "scooters/map";
    }

    @PostMapping("/update-location")
    @ResponseBody 
    public org.springframework.http.ResponseEntity<?> updateLocationAjax(
            @RequestParam Long id, 
            @RequestParam Double latitude, 
            @RequestParam Double longitude) {
        
        try {
            scooterService.updateScooterLocation(id, latitude, longitude);
            return org.springframework.http.ResponseEntity.ok().body("Konum başarıyla güncellendi.");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Konum güncellenirken bir hata oluştu.");
        }
    }
}