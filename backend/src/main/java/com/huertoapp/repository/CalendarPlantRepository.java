package com.huertoapp.repository;

import com.huertoapp.model.CalendarPlant;
import com.huertoapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalendarPlantRepository extends JpaRepository<CalendarPlant, Long> {
    List<CalendarPlant> findByHemisphere(User.Hemisphere hemisphere);
    Optional<CalendarPlant> findByPlantName(String plantName);
    List<CalendarPlant> findAllByOrderByPlantNameAsc();
    boolean existsByPlantName(String plantName);
}
