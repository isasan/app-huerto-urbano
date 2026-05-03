package com.huertoapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "harvest_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HarvestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate harvestDate;

    private Double quantity;

    @Column(length = 50)
    private String unit;

    @Enumerated(EnumType.STRING)
    private Quality quality;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    public enum Quality { MALA, NORMAL, BUENA, EXCELENTE }
}
