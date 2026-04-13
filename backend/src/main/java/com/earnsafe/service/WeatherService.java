package com.earnsafe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weather data service.
 *
 * Provides a realistic simulation of weather conditions for Indian cities.
 * In a production deployment this would call the OpenWeatherMap API; here we use
 * deterministic baselines per city with a small random variance so every call
 * returns plausible, stable data that changes slightly on each invocation.
 *
 * Returns weatherSeverity in [0, 10].
 */
@Slf4j
@Service
public class WeatherService {

    private static final Map<String, Double> CITY_BASE_SEVERITY = Map.of(
            "mumbai",    8.0,
            "delhi",     7.0,
            "bangalore", 5.5,
            "kolkata",   7.5,
            "chennai",   6.5,
            "hyderabad", 5.0,
            "pune",      5.5,
            "ahmedabad", 6.0
    );

    /**
     * Returns a weather severity score (0–10) for the given city.
     *
     * @param city city name (case-insensitive)
     * @return severity in [0.0, 10.0]
     */
    public double getWeatherSeverity(String city) {
        if (city == null || city.isBlank()) return 5.0;
        double base = CITY_BASE_SEVERITY.getOrDefault(city.toLowerCase().trim(), 5.0);
        // Add ±1.5 random variance to simulate live data
        double variance = (ThreadLocalRandom.current().nextDouble() * 3.0) - 1.5;
        double severity = Math.max(0.0, Math.min(10.0, base + variance));
        double result = Math.round(severity * 10.0) / 10.0;
        log.debug("[WeatherService] city={} baseSeverity={} result={}", city, base, result);
        return result;
    }
}
