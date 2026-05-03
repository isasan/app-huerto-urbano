package com.huertoapp.service;

import com.huertoapp.dto.response.WeatherResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    @Value("${app.weather.base-url}")
    private String baseUrl;

    @Value("${app.weather.geocoding-url}")
    private String geocodingUrl;

    private final WebClient.Builder webClientBuilder;

    private final Map<String, CachedWeather> cache = new ConcurrentHashMap<>();

    public WeatherResponse getWeatherByCity(String city, String countryCode) {
        String cacheKey = city.toLowerCase().trim() + (countryCode != null ? "_" + countryCode.toLowerCase() : "");
        CachedWeather cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("Cache hit for: {}", cacheKey);
            return cached.data;
        }

        double[] coords = geocode(city, countryCode);
        WeatherResponse result = fetchForecast(coords[0], coords[1], city);
        cache.put(cacheKey, new CachedWeather(result));
        return result;
    }

    @SuppressWarnings("unchecked")
    private double[] geocode(String city, String countryCode) {
        String uri = geocodingUrl + "/search?name={city}&count=1&language=es&format=json";
        if (countryCode != null && !countryCode.isBlank()) {
            uri += "&countryCode={cc}";
        }

        Map<String, Object> response;
        if (countryCode != null && !countryCode.isBlank()) {
            response = webClientBuilder.build().get()
                    .uri(uri, city, countryCode)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } else {
            response = webClientBuilder.build().get()
                    .uri(uri, city)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        }

        if (response == null) {
            throw new ResourceNotFoundException("Ciudad no encontrada: " + city);
        }
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        if (results == null || results.isEmpty()) {
            throw new ResourceNotFoundException("Ciudad no encontrada: " + city);
        }

        Map<String, Object> location = results.get(0);
        double lat = ((Number) location.get("latitude")).doubleValue();
        double lon = ((Number) location.get("longitude")).doubleValue();
        return new double[]{lat, lon};
    }

    @SuppressWarnings("unchecked")
    private WeatherResponse fetchForecast(double lat, double lon, String cityName) {
        String uri = baseUrl + "/forecast"
                + "?latitude={lat}&longitude={lon}"
                + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,visibility"
                + "&timezone=auto";

        Map<String, Object> response = webClientBuilder.build().get()
                .uri(uri, lat, lon)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) throw new RuntimeException("Error al consultar el clima");

        Map<String, Object> current = (Map<String, Object>) response.get("current");
        Map<String, Object> currentUnits = (Map<String, Object>) response.get("current_units");

        int weatherCode = ((Number) current.get("weather_code")).intValue();
        String description = wmoDescription(weatherCode);
        String icon = wmoIcon(weatherCode);

        Object visObj = current.get("visibility");
        Integer visibility = visObj != null ? ((Number) visObj).intValue() : null;

        return WeatherResponse.builder()
                .city(cityName)
                .country(null)
                .temperature(((Number) current.get("temperature_2m")).doubleValue())
                .feelsLike(((Number) current.get("apparent_temperature")).doubleValue())
                .humidity(((Number) current.get("relative_humidity_2m")).doubleValue())
                .windSpeed(((Number) current.get("wind_speed_10m")).doubleValue())
                .visibility(visibility)
                .description(description)
                .icon(icon)
                .main(wmoMain(weatherCode))
                .build();
    }

    // WMO 4677 codes → descripción en español
    private String wmoDescription(int code) {
        return switch (code) {
            case 0 -> "Cielo despejado";
            case 1 -> "Mayormente despejado";
            case 2 -> "Parcialmente nublado";
            case 3 -> "Nublado";
            case 45 -> "Niebla";
            case 48 -> "Niebla con escarcha";
            case 51 -> "Llovizna ligera";
            case 53 -> "Llovizna moderada";
            case 55 -> "Llovizna intensa";
            case 61 -> "Lluvia ligera";
            case 63 -> "Lluvia moderada";
            case 65 -> "Lluvia intensa";
            case 71 -> "Nieve ligera";
            case 73 -> "Nieve moderada";
            case 75 -> "Nieve intensa";
            case 77 -> "Granizo de nieve";
            case 80 -> "Chubascos ligeros";
            case 81 -> "Chubascos moderados";
            case 82 -> "Chubascos fuertes";
            case 85 -> "Chubascos de nieve ligeros";
            case 86 -> "Chubascos de nieve fuertes";
            case 95 -> "Tormenta";
            case 96 -> "Tormenta con granizo";
            case 99 -> "Tormenta con granizo intenso";
            default -> "Desconocido";
        };
    }

    // WMO codes → Bootstrap icon class
    private String wmoIcon(int code) {
        if (code == 0) return "bi-sun";
        if (code == 1) return "bi-sun";
        if (code == 2) return "bi-cloud-sun";
        if (code == 3) return "bi-clouds";
        if (code == 45 || code == 48) return "bi-cloud-fog2";
        if (code >= 51 && code <= 55) return "bi-cloud-drizzle";
        if (code >= 61 && code <= 65) return "bi-cloud-rain";
        if (code >= 71 && code <= 77) return "bi-cloud-snow";
        if (code >= 80 && code <= 82) return "bi-cloud-rain-heavy";
        if (code == 85 || code == 86) return "bi-cloud-snow";
        if (code >= 95) return "bi-cloud-lightning-rain";
        return "bi-cloud";
    }

    // WMO codes → categoría principal
    private String wmoMain(int code) {
        if (code == 0 || code == 1) return "Clear";
        if (code == 2 || code == 3) return "Clouds";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 86) return "Rain";
        if (code >= 95) return "Thunderstorm";
        return "Unknown";
    }

    private static class CachedWeather {
        final WeatherResponse data;
        final LocalDateTime timestamp;

        CachedWeather(WeatherResponse data) {
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(timestamp.plusMinutes(30));
        }
    }
}
