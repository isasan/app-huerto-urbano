package com.huertoapp.service;

import com.huertoapp.data.SeedingCalendarData;
import com.huertoapp.dto.response.CalendarPlantResponse;
import com.huertoapp.model.CalendarPlant;
import com.huertoapp.model.PlantDetail;
import com.huertoapp.model.User;
import com.huertoapp.repository.CalendarPlantRepository;
import com.huertoapp.repository.PlantDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarPlantRepository calendarPlantRepository;
    private final PlantDetailRepository plantDetailRepository;

    public List<CalendarPlantResponse> getCalendarByHemisphere(User.Hemisphere hemisphere) {
        Map<String, CalendarPlantResponse> merged = new LinkedHashMap<>();

        // 1. Static plants
        Map<String, PlantDetail> detailMap = buildDetailMap();
        for (SeedingCalendarData.PlantInfo p : SeedingCalendarData.getPlants(hemisphere)) {
            PlantDetail detail = detailMap.get(p.name());
            merged.put(p.name(), toResponseFromStatic(p, null, detail));
        }

        // 2. DB plants override / add (same hemisphere)
        for (CalendarPlant cp : calendarPlantRepository.findByHemisphere(hemisphere)) {
            merged.put(cp.getPlantName(), toResponseFromDb(cp, null));
        }

        return List.copyOf(merged.values());
    }

    public List<CalendarPlantResponse> getCalendarForMonth(User.Hemisphere hemisphere, int month) {
        Map<String, CalendarPlantResponse> merged = new LinkedHashMap<>();

        Map<String, PlantDetail> detailMap = buildDetailMap();
        for (SeedingCalendarData.PlantInfo p : SeedingCalendarData.getPlants(hemisphere)) {
            if (p.sewingMonths().contains(month) || p.transplantMonths().contains(month) || p.harvestMonths().contains(month)) {
                PlantDetail detail = detailMap.get(p.name());
                merged.put(p.name(), toResponseFromStatic(p, month, detail));
            }
        }

        for (CalendarPlant cp : calendarPlantRepository.findByHemisphere(hemisphere)) {
            List<Integer> sow = CalendarPlantService.parseMonths(cp.getSewingMonths());
            List<Integer> trans = CalendarPlantService.parseMonths(cp.getTransplantMonths());
            List<Integer> harv = CalendarPlantService.parseMonths(cp.getHarvestMonths());
            if (sow.contains(month) || trans.contains(month) || harv.contains(month)) {
                merged.put(cp.getPlantName(), toResponseFromDb(cp, month));
            }
        }

        return List.copyOf(merged.values());
    }

    private Map<String, PlantDetail> buildDetailMap() {
        Map<String, PlantDetail> map = new LinkedHashMap<>();
        for (PlantDetail d : plantDetailRepository.findAll()) {
            map.put(d.getPlantName(), d);
        }
        return map;
    }

    private CalendarPlantResponse toResponseFromStatic(SeedingCalendarData.PlantInfo p, Integer month, PlantDetail detail) {
        return new CalendarPlantResponse(
                p.name(),
                p.emoji(),
                p.difficulty(),
                p.daysToHarvest(),
                p.sewingMonths(),
                p.transplantMonths(),
                p.harvestMonths(),
                month != null && p.sewingMonths().contains(month),
                month != null && p.transplantMonths().contains(month),
                month != null && p.harvestMonths().contains(month),
                detail != null ? detail.getLatinName() : null,
                detail != null ? detail.getWateringFrequency() : null,
                detail != null ? detail.getPests() : null,
                detail != null ? detail.getWikipediaUrl() : null
        );
    }

    private CalendarPlantResponse toResponseFromDb(CalendarPlant cp, Integer month) {
        List<Integer> sow = CalendarPlantService.parseMonths(cp.getSewingMonths());
        List<Integer> trans = CalendarPlantService.parseMonths(cp.getTransplantMonths());
        List<Integer> harv = CalendarPlantService.parseMonths(cp.getHarvestMonths());
        return new CalendarPlantResponse(
                cp.getPlantName(),
                cp.getEmoji(),
                cp.getDifficulty(),
                cp.getDaysToHarvest(),
                sow,
                trans,
                harv,
                month != null && sow.contains(month),
                month != null && trans.contains(month),
                month != null && harv.contains(month),
                cp.getLatinName(),
                cp.getWateringFrequency(),
                cp.getPests(),
                cp.getWikipediaUrl()
        );
    }
}
