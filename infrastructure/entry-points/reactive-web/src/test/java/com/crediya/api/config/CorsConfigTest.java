package com.crediya.api.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

    private final CorsConfig corsConfig = new CorsConfig();

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
        "http://localhost:3000",
        "http://localhost:3000,https://app.crediya.com,https://admin.crediya.com",
        "",
        "*",
        "http://localhost:3000,http://127.0.0.1:3000,http://0.0.0.0:3000",
        "https://crediya.com,https://www.crediya.com,https://api.crediya.com",
        "http://localhost:3000, https://app.crediya.com, https://admin.crediya.com",
        "http://localhost:3000,http://localhost:8080,http://localhost:4200",
        "http://localhost:3000,https://secure.crediya.com"
    })
    void shouldCreateCorsWebFilterWithVariousOrigins(String origins) {
        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        assertNotNull(corsWebFilter);
    }


}