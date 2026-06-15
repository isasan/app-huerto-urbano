package com.huertoapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Provee el bean {@link WebClient.Builder} que {@code WeatherService} inyecta.
 * Desde Spring Boot 4 ya no se autoconfigura automáticamente en aplicaciones
 * servlet (starter-web), por lo que se declara aquí de forma explícita.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
