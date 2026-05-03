package com.huertoapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plant_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String plantName;

    @Column(length = 200)
    private String latinName;

    @Column(length = 200)
    private String wateringFrequency;

    @Column(columnDefinition = "TEXT")
    private String pests;

    @Column(length = 500)
    private String wikipediaUrl;
}
