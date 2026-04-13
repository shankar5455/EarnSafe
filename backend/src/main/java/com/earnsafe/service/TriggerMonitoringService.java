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

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerMonitoringService {

    private final PolicyRepository policyRepository;
    private final WeatherEventRepository weatherEventRepository;
    private final TriggerService triggerService;

    /**
     * Scheduled trigger scan – runs every app.trigger.interval ms (default 5 min).
     */
    @Transactional
    @Scheduled(fixedDelayString = "${app.trigger.interval:300000}")
    public void runTriggerScan() {
        log.info("=== [AutoTrigger] Starting scheduled trigger scan at {} ===", LocalDateTime.now());

        // fetch active policies WITH user eagerly loaded
        List<Policy> activePolicies = policyRepository.findActivePoliciesWithUser();

        if (activePolicies.isEmpty()) {
            log.info("[AutoTrigger] No active policies found. Skipping scan.");
            return;
        }

        // Group policies by zone
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
     * Resolve zone from policy → user.zone → user.city
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
     * Get city from policies
     */
    private String resolveCity(List<Policy> policies) {
        return policies.stream()
                .map(p -> p.getUser() != null ? p.getUser().getCity() : null)
                .filter(c -> c != null && !c.isBlank())
                .findFirst()
                .orElse(null);
    }

    /**
     * Evaluate weather conditions and trigger claims
     */
    private List<ClaimResponse> evaluateZoneConditions(String zone, String city) {
        log.info("[AutoTrigger] Checking triggers for zone: {} (city: {})", zone, city);

        // Replace later with real weather API
        WeatherEvent event = buildSimulatedEvent(zone, city);

        weatherEventRepository.save(event);

        List<ClaimResponse> claims = triggerService.evaluatePoliciesForEvent(event);

        if (claims.isEmpty()) {
            log.info("[AutoTrigger] No disruption triggered for zone: {}", zone);
        } else {
            log.info("[AutoTrigger] {} claim(s) auto-created for zone: {}", claims.size(), zone);
        }

        return claims;
    }

    /**
     * Simulate weather events (rotating conditions)
     */
    private WeatherEvent buildSimulatedEvent(String zone, String city) {
        int minute = LocalDateTime.now().getMinute();

        String eventType;
        BigDecimal rainfallMm = null;
        BigDecimal temperature = null;
        Integer aqi = null;
        boolean floodAlert = false;
        boolean closureAlert = false;

        int bucket = (minute / 5) % 5;

        switch (bucket) {
            case 0 -> {
                eventType = "HEAVY_RAIN";
                rainfallMm = BigDecimal.valueOf(35.0);
            }
            case 1 -> {
                eventType = "HEATWAVE";
                temperature = BigDecimal.valueOf(44.0);
            }
            case 2 -> {
                eventType = "POLLUTION_SPIKE";
                aqi = 320;
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
                .city(city != null ? city : zone)
                .zone(zone)
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