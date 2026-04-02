package com.gigshield.service;

import com.gigshield.dto.request.MockEventRequest;
import com.gigshield.dto.response.ClaimResponse;
import com.gigshield.entity.Policy;
import com.gigshield.entity.User;
import com.gigshield.entity.WeatherEvent;
import com.gigshield.repository.PolicyRepository;
import com.gigshield.repository.UserRepository;
import com.gigshield.repository.WeatherEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TriggerService {

    private final WeatherEventRepository weatherEventRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final ClaimService claimService;

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
                try {
                    ClaimResponse claim = claimService.triggerClaim(user, event);
                    claims.add(claim);
                } catch (Exception e) {
                    // Log but don't fail
                }
            }
        });

        return claims;
    }

    public List<ClaimResponse> evaluateAll(WeatherEvent event) {
        List<ClaimResponse> allClaims = new ArrayList<>();

        List<Policy> activePolicies = policyRepository.findByStatus(Policy.PolicyStatus.ACTIVE);

        for (Policy policy : activePolicies) {
            User user = policy.getUser();
            // Check zone match
            String userCity = user.getCity();
            String eventCity = event.getCity();
            if (userCity != null && userCity.equalsIgnoreCase(eventCity)) {
                if (isTriggerConditionMet(event)) {
                    try {
                        ClaimResponse claim = claimService.triggerClaim(user, event);
                        allClaims.add(claim);
                    } catch (Exception e) {
                        // Log but don't fail
                    }
                }
            }
        }

        return allClaims;
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
}
