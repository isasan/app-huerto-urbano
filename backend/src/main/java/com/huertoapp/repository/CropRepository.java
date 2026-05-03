package com.huertoapp.repository;

import com.huertoapp.model.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByPlotId(Long plotId);
    List<Crop> findByPlotGardenId(Long gardenId);
    List<Crop> findByPlotGardenUserId(Long userId);
    Optional<Crop> findByIdAndPlotId(Long id, Long plotId);
    List<Crop> findByPlotGardenUserIdAndStatus(Long userId, Crop.Status status);
    long countByPlotGardenUserIdAndStatusNot(Long userId, Crop.Status status);
}
