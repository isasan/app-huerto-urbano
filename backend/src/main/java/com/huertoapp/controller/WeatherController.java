package com.huertoapp.controller;

import com.huertoapp.dto.response.WeatherResponse;
import com.huertoapp.model.User;
import com.huertoapp.repository.UserRepository;
import com.huertoapp.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Tag(name = "Clima")
public class WeatherController {

    private final WeatherService weatherService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Obtener clima por ciudad", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String countryCode,
            @AuthenticationPrincipal UserDetails userDetails) {

        String targetCity = city;
        String targetCountry = countryCode;

        if (targetCity == null || targetCity.isBlank()) {
            if (userDetails == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Debes proporcionar una ciudad o iniciar sesión para usar tu ciudad configurada");
            }
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
            if (user.getCity() == null || user.getCity().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No tienes una ciudad configurada en tu perfil");
            }
            targetCity = user.getCity();
            targetCountry = user.getCountryCode();
        }

        return ResponseEntity.ok(weatherService.getWeatherByCity(targetCity, targetCountry));
    }
}
