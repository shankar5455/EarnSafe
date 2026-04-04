package com.earnsafe.service;

import com.earnsafe.dto.request.MockEventRequest;
import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;
import com.earnsafe.entity.WeatherEvent;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.PolicyRepository;
import com.earnsafe.repository.UserRepository;
import com.earnsafe.repository.WeatherEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerService {

    // Parametric threshold constants used in the realtime feed simulation
    private static final java.math.BigDecimal SIM_RAINFALL_MM  = new java.math.BigDecimal("40");
    private static final java.math.BigDecimal SIM_TEMPERATURE  = new java.math.BigDecimal("44");
    private static final int                  SIM_AQI          = 320;

    private final WeatherEventRepository weatherEventRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final ClaimService claimService;
    private final ClaimRepository claimRepository;

    public List<WeatherEvent> getLiveEvents() {
        return weatherEventRepository.findTop10ByOrderByEventTimestampDesc();
    }

    public WeatherEvent createMockEvent(MockEventRequest request) {
        WeatherEvent event = WeatherEvent.builder()
                .city(request.getCity())
                .zone(request.getZone())
                .eventType(request.getEventType())
                .temperature(request.getTemperature())
                .rainfallMm(request.getRainfallMm())
                .aqi(request.getAqi())
                .floodAlert(request.getFloodAlert() != null ? request.getFloodAlert() : false)
                .closureAlert(request.getClosureAlert() != null ? request.getClosureAlert() : false)
                .eventTimestamp(LocalDateTime.now())
                .sourceType("MOCK")
                .build();

        return weatherEventRepository.save(event);
    }

    public List<ClaimResponse> evaluateForUser(Long userId, WeatherEvent event) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ClaimResponse> claims = new ArrayList<>();

        // Check if user has active policy
        policyRepository.findActiveByUser(user).ifPresent(policy -> {
            if (isTriggerConditionMet(event)) {
                LocalDate eventDate = event.getEventTimestamp() != null
                        ? event.getEventTimestamp().toLocalDate()
                        : LocalDate.now();

                boolean alreadyExists = claimRepository.existsByUserAndDisruptionDateAndTriggerType(
                        user, eventDate, event.getEventType());
                if (alreadyExists) {
                    log.info("Duplicate claim already exists for user {} (date: {}, type: {}). Not creating a new claim.",
                            user.getEmail(), eventDate, event.getEventType());
                    return;
                }

                try {
                    ClaimResponse claim = claimService.triggerClaim(user, event);
                    claims.add(claim);
                } catch (Exception e) {
                    log.warn("Failed to create claim for user {}: {}", user.getEmail(), e.getMessage());
                }
            }
        });

        return claims;
    }

    /**
     * Core reusable method: evaluates all active policies against a weather event.
     * Called by both the REST controller (/triggers/evaluate-all) and the scheduler.
     */
    public List<ClaimResponse> evaluatePoliciesForEvent(WeatherEvent event) {
        List<ClaimResponse> allClaims = new ArrayList<>();
        List<Policy> activePolicies = policyRepository.findByStatus(Policy.PolicyStatus.ACTIVE);
        LocalDate eventDate = event.getEventTimestamp() != null
                ? event.getEventTimestamp().toLocalDate()
                : LocalDate.now();

        log.info("Checking triggers for zone/city: {} | eventType: {} | active policies: {}",
                event.getCity(), event.getEventType(), activePolicies.size());

        for (Policy policy : activePolicies) {
            User user = policy.getUser();
            String userCity = user.getCity();
            String eventCity = event.getCity();

            if (userCity == null || !userCity.equalsIgnoreCase(eventCity)) {
                continue;
            }

            if (!isTriggerConditionMet(event)) {
                continue;
            }

            // Duplicate prevention: skip if a claim already exists for this user/date/type
            boolean alreadyExists = claimRepository.existsByUserAndDisruptionDateAndTriggerType(
                    user, eventDate, event.getEventType());
            if (alreadyExists) {
                log.info("Skipping duplicate claim for policy ID {} (user: {}, date: {}, type: {})",
                        policy.getId(), user.getEmail(), eventDate, event.getEventType());
                continue;
            }

            try {
                log.info("Disruption detected for policy ID {} (user: {}, city: {}, eventType: {})",
                        policy.getId(), user.getEmail(), eventCity, event.getEventType());
                ClaimResponse claim = claimService.triggerClaim(user, event);
                log.info("Claim auto-created: {} for policy ID {}", claim.getClaimNumber(), policy.getId());
                allClaims.add(claim);
            } catch (Exception e) {
                log.warn("Failed to create claim for policy ID {}: {}", policy.getId(), e.getMessage());
            }
        }

        return allClaims;
    }

    public List<ClaimResponse> evaluateAll(WeatherEvent event) {
        return evaluatePoliciesForEvent(event);
    }

    private boolean isTriggerConditionMet(WeatherEvent event) {
        if (event.getEventType() == null) return false;
        return switch (event.getEventType()) {
            case "HEAVY_RAIN" -> event.getRainfallMm() != null && event.getRainfallMm().doubleValue() > 30;
            case "FLOOD_ALERT" -> Boolean.TRUE.equals(event.getFloodAlert());
            case "HEATWAVE" -> event.getTemperature() != null && event.getTemperature().doubleValue() > 42;
            case "POLLUTION_SPIKE" -> event.getAqi() != null && event.getAqi() > 300;
            case "ZONE_CLOSURE" -> Boolean.TRUE.equals(event.getClosureAlert());
            default -> false;
        };
    }

    public List<ClaimResponse> processMockEventAndEvaluateAll(MockEventRequest request) {
        WeatherEvent event = createMockEvent(request);
        return evaluateAll(event);
    }

    /**
     * Simulates a real-time trigger feed for all cities that have active policies.
     * Cycles through all 5 parametric event types (HEAVY_RAIN, FLOOD_ALERT, HEATWAVE,
     * POLLUTION_SPIKE, ZONE_CLOSURE) so each city experiences a different condition
     * on each call. Claims are auto-created whenever thresholds are met.
     */
    public List<ClaimResponse> simulateRealtimeTriggerFeed() {
        List<String> activeCities = policyRepository.findByStatus(Policy.PolicyStatus.ACTIVE)
                .stream()
                .map(p -> p.getUser() != null ? p.getUser().getCity() : null)
                .filter(city -> city != null && !city.isBlank())
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        if (activeCities.isEmpty()) {
            log.info("[SimFeed] No active policies found – nothing to simulate.");
            return java.util.Collections.emptyList();
        }

        String[] eventTypes  = {"HEAVY_RAIN", "FLOOD_ALERT", "HEATWAVE", "POLLUTION_SPIKE", "ZONE_CLOSURE"};
        List<ClaimResponse> allClaims = new ArrayList<>();
        int idx = java.time.LocalDateTime.now().getMinute() % eventTypes.length;

        for (String city : activeCities) {
            String eventType = eventTypes[idx % eventTypes.length];
            idx++;

            MockEventRequest req = new MockEventRequest();
            req.setCity(city);
            req.setZone(city);
            req.setEventType(eventType);

            switch (eventType) {
                case "HEAVY_RAIN"      -> req.setRainfallMm(SIM_RAINFALL_MM);
                case "FLOOD_ALERT"     -> req.setFloodAlert(true);
                case "HEATWAVE"        -> req.setTemperature(SIM_TEMPERATURE);
                case "POLLUTION_SPIKE" -> req.setAqi(SIM_AQI);
                case "ZONE_CLOSURE"    -> req.setClosureAlert(true);
            }

            log.info("[SimFeed] Simulating {} in {}", eventType, city);
            List<ClaimResponse> claims = processMockEventAndEvaluateAll(req);
            allClaims.addAll(claims);
        }

        log.info("[SimFeed] Simulation complete – {} claim(s) auto-created.", allClaims.size());
        return allClaims;
    }
}
