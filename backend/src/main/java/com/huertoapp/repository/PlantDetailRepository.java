package com.huertoapp.repository;

import com.huertoapp.model.PlantDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlantDetailRepository extends JpaRepository<PlantDetail, Long> {
    Optional<PlantDetail> findByPlantName(String plantName);
    List<PlantDetail> findAllByOrderByPlantNameAsc();
}
