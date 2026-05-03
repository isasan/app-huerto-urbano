package com.huertoapp.repository;

import com.huertoapp.model.Plot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlotRepository extends JpaRepository<Plot, Long> {
    List<Plot> findByGardenId(Long gardenId);
    Optional<Plot> findByIdAndGardenId(Long id, Long gardenId);
    Optional<Plot> findByIdAndGardenUserId(Long id, Long userId);
}
