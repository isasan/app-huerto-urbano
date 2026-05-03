package com.huertoapp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherResponse {
    private String city;
    private String country;
    private Double temperature;
    private Double feelsLike;
    private Double humidity;
    private Double windSpeed;
    private Integer visibility;
    private String description;
    private String icon;
    private String main;
}
