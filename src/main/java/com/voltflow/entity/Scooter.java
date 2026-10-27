package com.voltflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "scooters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scooter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Seri numarası boş bırakılamaz")
    @Column(unique = true, nullable = false)
    private String serialNumber;

    @NotBlank(message = "Model alanı boş bırakılamaz")
    @Column(nullable = false)
    private String model;

    @NotNull(message = "Batarya seviyesi zorunludur")
    @Min(value = 0, message = "Batarya 0'dan küçük olamaz")
    @Max(value = 100, message = "Batarya 100'den büyük olamaz")
    @Column(nullable = false)
    private Double batteryLevel;

    @Embedded
    private Location location;

    @NotNull(message = "Durum seçimi zorunludur")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScooterStatus status;

    @OneToMany(mappedBy = "scooter", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<MaintenanceLog> maintenanceLogs;
}