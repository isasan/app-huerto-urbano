package com.huertoapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "calendar_plants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarPlant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String plantName;

    @Column(nullable = false, length = 10)
    private String emoji;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private User.Hemisphere hemisphere;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(nullable = false)
    private int daysToHarvest;

    @Column(columnDefinition = "TEXT")
    private String sewingMonths;

    @Column(columnDefinition = "TEXT")
    private String transplantMonths;

    @Column(columnDefinition = "TEXT")
    private String harvestMonths;

    @Column(length = 200)
    private String latinName;

    @Column(length = 200)
    private String wateringFrequency;

    @Column(columnDefinition = "TEXT")
    private String pests;

    @Column(length = 500)
    private String wikipediaUrl;
}
