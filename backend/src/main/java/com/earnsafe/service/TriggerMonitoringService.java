package com.earnsafe.service;

import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;
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

<<<<<<< HEAD
        // FIX: fetch active policies WITH user eagerly loaded
        List<Policy> activePolicies = policyRepository.findActivePoliciesWithUser();

=======
        List<Policy> activePolicies = policyRepository.findActivePoliciesWithUser();
>>>>>>> 84d46b715f50900d5fcf9292b90b3f41007422be
        if (activePolicies.isEmpty()) {
            log.info("[AutoTrigger] No active policies found. Skipping scan.");
            return;
        }

        // Group policies by zone (preferred: zoneCovered → user.zone → user.city)
        Map<String, List<Policy>> byZone = activePolicies.stream()
                .filter(p -> resolveZone(p) != null)
                .collect(Collectors.groupingBy(this::resolveZone));

        log.info("[AutoTrigger] Scanning {} unique zone(s) for disruptions: {}",
                byZone.size(), byZone.keySet());

        int totalClaims = 0;
        for (Map.Entry<String, List<Policy>> entry : byZone.entrySet()) {
            String zone = entry.getKey();
            String city = resolveCity(entry.getValue());
            List<ClaimResponse> claims = evaluateZoneConditions(zone, city);
            totalClaims += claims.size();
        }

        log.info("=== [AutoTrigger] Scan complete. Auto-created {} claim(s). ===", totalClaims);
    }

    /**
     * Resolves the zone key for a policy using the preferred fallback chain:
     * policy.zoneCovered → user.zone → user.city.
     */
    private String resolveZone(Policy p) {
        if (p.getZoneCovered() != null && !p.getZoneCovered().isBlank()) {
            return p.getZoneCovered();
        }
        User user = p.getUser();
        if (user != null) {
            if (user.getZone() != null && !user.getZone().isBlank()) {
                return user.getZone();
            }
            if (user.getCity() != null && !user.getCity().isBlank()) {
                return user.getCity();
            }
        }
        return null;
    }

    /**
     * Returns the actual city for a group of policies in the same zone.
     * Uses the first policy whose user has a non-blank city.
     */
    private String resolveCity(List<Policy> policies) {
        return policies.stream()
                .map(p -> p.getUser() != null ? p.getUser().getCity() : null)
                .filter(c -> c != null && !c.isBlank())
                .findFirst()
                .orElse(null);
    }

    /**
     * Simulates weather condition checks for a given zone and triggers evaluation.
     * Cycles through parametric event types so each scheduler run tests a different condition.
     */
    private List<ClaimResponse> evaluateZoneConditions(String zone, String city) {
        log.info("[AutoTrigger] Checking triggers for zone: {} (city: {})", zone, city);

        // Simulate realistic disruption conditions that breach parametric thresholds.
<<<<<<< HEAD
        // Later you can replace this with real OpenWeather API integration.
        WeatherEvent event = buildSimulatedEvent(city);
=======
        // In a production setup this data would come from a live weather/AQI API.
        WeatherEvent event = buildSimulatedEvent(zone, city);
>>>>>>> 84d46b715f50900d5fcf9292b90b3f41007422be
        weatherEventRepository.save(event);

        List<ClaimResponse> claims = triggerService.evaluatePoliciesForEvent(event);

        if (claims.isEmpty()) {
<<<<<<< HEAD
            log.info("[AutoTrigger] No disruption triggered for city: {}", city);
        } else {
            log.info("[AutoTrigger] {} claim(s) auto-created for city: {}", claims.size(), city);
=======
            log.info("[AutoTrigger] No disruption triggered for zone: {}", zone);
>>>>>>> 84d46b715f50900d5fcf9292b90b3f41007422be
        }

        return claims;
    }

    /**
     * Builds a simulated weather event that meets parametric trigger thresholds.
<<<<<<< HEAD
     * Rotates through HEAVY_RAIN → HEATWAVE → POLLUTION_SPIKE → FLOOD_ALERT → ZONE_CLOSURE
     * every scheduler cycle based on the current minute.
=======
     * Rotates through HEAVY_RAIN → HEATWAVE → POLLUTION_SPIKE every scheduler cycle
     * based on the current minute, so different conditions are exercised over time.
     * city is set to the actual user/policy city; zone is the coverage zone used for grouping.
>>>>>>> 84d46b715f50900d5fcf9292b90b3f41007422be
     */
    private WeatherEvent buildSimulatedEvent(String zone, String city) {
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
<<<<<<< HEAD
                .city(city)
                .zone(city) // for now same as city; later you can replace with actual zone
=======
                .city(city != null ? city : zone)
                .zone(zone)
>>>>>>> 84d46b715f50900d5fcf9292b90b3f41007422be
                .eventType(eventType)
                .rainfallMm(rainfallMm)
                .temperature(temperature)
                .aqi(aqi)
                .floodAlert(floodAlert)
                .closureAlert(closureAlert)
                .eventTimestamp(LocalDateTime.now())
                .sourceType("SCHEDULER_SIMULATED")
                .build();
    }
}