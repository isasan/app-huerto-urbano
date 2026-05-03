package com.huertoapp.repository;

import com.huertoapp.model.HarvestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HarvestRepository extends JpaRepository<HarvestLog, Long> {
    List<HarvestLog> findByCropPlotGardenUserId(Long userId);
    List<HarvestLog> findByCropIdAndCropPlotGardenUserId(Long cropId, Long userId);
    List<HarvestLog> findTop5ByCropPlotGardenUserIdOrderByHarvestDateDesc(Long userId);
}
