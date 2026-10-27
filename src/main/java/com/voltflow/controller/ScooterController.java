package com.voltflow.controller;

import com.voltflow.entity.Scooter;
import com.voltflow.entity.ScooterStatus;
import com.voltflow.entity.Location;
import com.voltflow.service.ScooterService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/scooters")
@RequiredArgsConstructor
public class ScooterController {

    private final ScooterService scooterService;

    @GetMapping
    public String listScooters(Model model,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String battery) {
        List<Scooter> scooters;

        if (status != null && !status.isEmpty()) {
            scooters = scooterService.getScootersByStatus(ScooterStatus.valueOf(status));
        } else if ("critical".equals(battery)) {
            scooters = scooterService.getCriticalScooters();
        } else {
            scooters = scooterService.getAllScooters();
        }

        model.addAttribute("scooters", scooters);
        model.addAttribute("criticalCount", scooterService.getCriticalScooters().size());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedBattery", battery);
        return "scooters/list";
    }

    @GetMapping("/new")
    public String newScooterForm(Model model) {
        model.addAttribute("scooter", new Scooter());
        model.addAttribute("statuses", ScooterStatus.values());
        return "scooters/form";
    }

    @PostMapping
    public String saveScooter(@ModelAttribute Scooter scooter, 
                              org.springframework.validation.BindingResult bindingResult,
                              @RequestParam(required = false) Double latitude,
                              @RequestParam(required = false) Double longitude,
                              Model model) {

        if (!scooterService.isSerialNumberUnique(scooter.getSerialNumber(), scooter.getId())) {
            bindingResult.rejectValue("serialNumber", "error.scooter", "Bu donanım seri numarası sistemde zaten kayıtlı!");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", ScooterStatus.values());
            return "scooters/form";
        }

        scooter.setLocation(new Location(latitude, longitude));
        scooterService.saveScooter(scooter);
        return "redirect:/scooters";
    }

    @GetMapping("/edit/{id}")
    public String editScooterForm(@PathVariable Long id, Model model) {
        model.addAttribute("scooter", scooterService.getScooterById(id));
        model.addAttribute("statuses", ScooterStatus.values());
        return "scooters/form";
    }

    @PostMapping("/update/{id}")
    public String updateScooter(@PathVariable Long id,
                                @ModelAttribute Scooter scooter,
                                org.springframework.validation.BindingResult bindingResult,
                                @RequestParam(required = false) Double latitude,
                                @RequestParam(required = false) Double longitude,
                                Model model) {

        if (!scooterService.isSerialNumberUnique(scooter.getSerialNumber(), id)) {
            bindingResult.rejectValue("serialNumber", "error.scooter", "Bu seri numarası başka bir araca tahsis edilmiş!");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", ScooterStatus.values());
            return "scooters/form";
        }

        scooter.setLocation(new Location(latitude, longitude));
        scooterService.updateScooter(id, scooter);
        return "redirect:/scooters";
    }

    @GetMapping("/delete/{id}")
    public String deleteScooter(@PathVariable Long id) {
        scooterService.deleteScooter(id);
        return "redirect:/scooters";
    }

    @GetMapping("/{id}")
    public String scooterDetail(@PathVariable Long id, Model model) {
        model.addAttribute("scooter", scooterService.getScooterById(id));
        return "scooters/detail";
    }

    @GetMapping("/finish-maintenance/{id}")
    public String finishMaintenance(@PathVariable Long id) {
        scooterService.finishMaintenance(id);
        return "redirect:/dashboard";
    }

   
}
