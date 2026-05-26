package com.huertoapp.service;

import com.huertoapp.dto.response.WeatherResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeatherServiceTest {

    private MockWebServer mockWebServer;
    private WeatherService weatherService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String serverUrl = "http://localhost:" + mockWebServer.getPort();

        weatherService = new WeatherService(WebClient.builder());
        ReflectionTestUtils.setField(weatherService, "baseUrl", serverUrl);
        ReflectionTestUtils.setField(weatherService, "geocodingUrl", serverUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Test
    void getWeatherByCity_success_returnsWeatherResponse() {
        String geocodingBody = """
                {"results": [{"latitude": 40.4165, "longitude": -3.7026, "name": "Madrid"}]}
                """;
        String forecastBody = """
                {"current": {"temperature_2m": 25.0, "apparent_temperature": 23.0,
                 "relative_humidity_2m": 60.0, "wind_speed_10m": 10.0,
                 "weather_code": 0, "visibility": 10000},
                 "current_units": {}}
                """;

        mockWebServer.enqueue(new MockResponse.Builder()
                .body(geocodingBody)
                .addHeader("Content-Type", "application/json")
                .build());
        mockWebServer.enqueue(new MockResponse.Builder()
                .body(forecastBody)
                .addHeader("Content-Type", "application/json")
                .build());

        WeatherResponse response = weatherService.getWeatherByCity("Madrid", "ES");

        assertThat(response.getCity()).isEqualTo("Madrid");
        assertThat(response.getTemperature()).isEqualTo(25.0);
        assertThat(response.getDescription()).isEqualTo("Cielo despejado");
        assertThat(response.getIcon()).isEqualTo("bi-sun");
    }

    @Test
    void getWeatherByCity_cityNotFound_throwsResourceNotFound() {
        String geocodingBody = """
                {"results": []}
                """;

        mockWebServer.enqueue(new MockResponse.Builder()
                .body(geocodingBody)
                .addHeader("Content-Type", "application/json")
                .build());

        assertThatThrownBy(() -> weatherService.getWeatherByCity("CiudadInexistente", null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("CiudadInexistente");
    }

    @Test
    void getWeatherByCity_cacheHit_doesNotCallApiSecondTime() throws IOException {
        String geocodingBody = """
                {"results": [{"latitude": 40.4165, "longitude": -3.7026, "name": "Madrid"}]}
                """;
        String forecastBody = """
                {"current": {"temperature_2m": 20.0, "apparent_temperature": 18.0,
                 "relative_humidity_2m": 50.0, "wind_speed_10m": 5.0,
                 "weather_code": 2, "visibility": 8000},
                 "current_units": {}}
                """;

        mockWebServer.enqueue(new MockResponse.Builder()
                .body(geocodingBody)
                .addHeader("Content-Type", "application/json")
                .build());
        mockWebServer.enqueue(new MockResponse.Builder()
                .body(forecastBody)
                .addHeader("Content-Type", "application/json")
                .build());

        WeatherResponse first = weatherService.getWeatherByCity("Madrid", "ES");
        WeatherResponse second = weatherService.getWeatherByCity("Madrid", "ES");

        assertThat(first.getTemperature()).isEqualTo(second.getTemperature());
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }
}
