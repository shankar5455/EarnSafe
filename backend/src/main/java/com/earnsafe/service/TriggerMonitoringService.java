package com.earnsafe.service;

import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.WeatherEvent;
import com.earnsafe.repository.PolicyRepository;
import com.earnsafe.repository.WeatherEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Automatic parametric trigger monitoring service.
 * Runs on a fixed schedule, simulates weather condition checks per active-policy city,
 * and auto-creates claims when disruption thresholds are breached.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerMonitoringService {

    private final PolicyRepository policyRepository;
    private final WeatherEventRepository weatherEventRepository;
    private final TriggerService triggerService;

    /**
     * Scheduled trigger scan – runs every app.trigger.interval ms (default 5 min).
     *
     * FIXED:
     * - @Transactional keeps Hibernate session open
     * - findActivePoliciesWithUser() avoids LazyInitializationException
     */
    @Transactional
    @Scheduled(fixedDelayString = "${app.trigger.interval:300000}")
    public void runTriggerScan() {
        log.info("=== [AutoTrigger] Starting scheduled trigger scan at {} ===", LocalDateTime.now());

        // FIX: fetch active policies WITH user eagerly loaded
        List<Policy> activePolicies = policyRepository.findActivePoliciesWithUser();

        if (activePolicies.isEmpty()) {
            log.info("[AutoTrigger] No active policies found. Skipping scan.");
            return;
        }

        // Group policies by city to avoid redundant event creation per city
        Map<String, List<Policy>> byCity = activePolicies.stream()
                .filter(p -> p.getUser() != null && p.getUser().getCity() != null)
                .collect(Collectors.groupingBy(p -> p.getUser().getCity()));

        log.info("[AutoTrigger] Scanning {} unique city/zone(s) for disruptions: {}",
                byCity.size(), byCity.keySet());

        int totalClaims = 0;
        for (String city : byCity.keySet()) {
            List<ClaimResponse> claims = evaluateCityConditions(city);
            totalClaims += claims.size();
        }

        log.info("=== [AutoTrigger] Scan complete. Auto-created {} claim(s). ===", totalClaims);
    }

    /**
     * Simulates weather condition checks for a given city and triggers evaluation.
     * Cycles through parametric event types so each scheduler run tests a different condition.
     */
    private List<ClaimResponse> evaluateCityConditions(String city) {
        log.info("[AutoTrigger] Checking triggers for zone/city: {}", city);

        // Simulate realistic disruption conditions that breach parametric thresholds.
        // Later you can replace this with real OpenWeather API integration.
        WeatherEvent event = buildSimulatedEvent(city);
        weatherEventRepository.save(event);

        List<ClaimResponse> claims = triggerService.evaluatePoliciesForEvent(event);

        if (claims.isEmpty()) {
            log.info("[AutoTrigger] No disruption triggered for city: {}", city);
        } else {
            log.info("[AutoTrigger] {} claim(s) auto-created for city: {}", claims.size(), city);
        }

        return claims;
    }

    /**
     * Builds a simulated weather event that meets parametric trigger thresholds.
     * Rotates through HEAVY_RAIN → HEATWAVE → POLLUTION_SPIKE → FLOOD_ALERT → ZONE_CLOSURE
     * every scheduler cycle based on the current minute.
     */
    private WeatherEvent buildSimulatedEvent(String city) {
        int minute = LocalDateTime.now().getMinute();

        String eventType;
        BigDecimal rainfallMm = null;
        BigDecimal temperature = null;
        Integer aqi = null;
        boolean floodAlert = false;
        boolean closureAlert = false;

        // Rotate event type per 5-minute bucket (0–4)
        int bucket = (minute / 5) % 5;

        switch (bucket) {
            case 0 -> {
                eventType = "HEAVY_RAIN";
                rainfallMm = BigDecimal.valueOf(35.0); // threshold > 30
            }
            case 1 -> {
                eventType = "HEATWAVE";
                temperature = BigDecimal.valueOf(44.0); // threshold > 42
            }
            case 2 -> {
                eventType = "POLLUTION_SPIKE";
                aqi = 320; // threshold > 300
            }
            case 3 -> {
                eventType = "FLOOD_ALERT";
                floodAlert = true;
                rainfallMm = BigDecimal.valueOf(80.0);
            }
            default -> {
                eventType = "ZONE_CLOSURE";
                closureAlert = true;
            }
        }

        return WeatherEvent.builder()
                .city(city)
                .zone(city) // for now same as city; later you can replace with actual zone
                .eventType(eventType)
                .rainfallMm(rainfallMm)
                .temperature(temperature)
                .aqi(aqi)
                .floodAlert(floodAlert)
                .closureAlert(closureAlert)
                .eventTimestamp(LocalDateTime.now())
                .sourceType("AUTO")
                .build();
    }
}