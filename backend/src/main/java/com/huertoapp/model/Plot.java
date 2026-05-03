package com.huertoapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "plots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private Double sizeM2;

    @Column(length = 100)
    private String soilType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garden_id", nullable = false)
    private Garden garden;

    @OneToMany(mappedBy = "plot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Crop> crops;
}
