package com.huertoapp.config;

import com.huertoapp.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Value("${springdoc.swagger-ui.enabled:false}")
    private boolean swaggerEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> {
                // La consola H2 se sirve en un iframe; fuera de dev se deniega el framing
                if (h2ConsoleEnabled) {
                    headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
                } else {
                    headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);
                }
            })
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                // Herramientas de desarrollo: solo accesibles si están habilitadas (perfil dev)
                if (h2ConsoleEnabled) {
                    auth.requestMatchers("/h2-console/**").permitAll();
                }
                if (swaggerEnabled) {
                    auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll();
                }
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/calendar/**").permitAll()
                    .requestMatchers("/api/plant-details/**").permitAll()
                    .requestMatchers("/api/weather/**").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
