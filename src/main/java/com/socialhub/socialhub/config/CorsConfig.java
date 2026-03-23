package com.socialhub.socialhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // React dev server
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));

        // Allow all common methods including OPTIONS (preflight)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow headers (Authorization needed for JWT)
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Expose Authorization header if you ever send it back
        config.setExposedHeaders(List.of("Authorization"));

        // If you're not using cookies, you can set this false.
        // But leaving true is okay for dev.
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}