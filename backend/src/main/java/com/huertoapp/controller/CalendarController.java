package com.huertoapp.controller;

import com.huertoapp.dto.response.CalendarPlantResponse;
import com.huertoapp.model.User;
import com.huertoapp.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendario de Siembra")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping
    @Operation(summary = "Obtener calendario de siembra. Con month filtra y añade flags de acción.")
    public ResponseEntity<List<CalendarPlantResponse>> getCalendar(
            @RequestParam User.Hemisphere hemisphere,
            @RequestParam(required = false) Integer month) {
        if (month != null) {
            return ResponseEntity.ok(calendarService.getCalendarForMonth(hemisphere, month));
        }
        return ResponseEntity.ok(calendarService.getCalendarByHemisphere(hemisphere));
    }
}
