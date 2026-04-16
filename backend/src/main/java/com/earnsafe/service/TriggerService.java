package com.earnsafe.service;

import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.entity.ClaimTrigger;
import com.earnsafe.entity.DeliveryActivity;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;
import com.earnsafe.entity.WeatherEvent;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.ClaimTriggerRepository;
import com.earnsafe.repository.DeliveryActivityRepository;
import com.earnsafe.repository.PolicyRepository;
import com.earnsafe.repository.WeatherEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerService {

    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final ClaimTriggerRepository claimTriggerRepository;
    private final DeliveryActivityRepository deliveryActivityRepository;
    private final WeatherEventRepository weatherEventRepository;
    private final WeatherService weatherService;
    private final ClaimService claimService;

    public List<WeatherEvent> getLiveEvents() {
        return weatherEventRepository.findTop10ByOrderByEventTimestampDesc();
    }

    public List<ClaimResponse> scanAndEvaluateCity(String city) {
        WeatherEvent event = weatherService.fetchAndStoreCurrentWeather(city, city);
        return evaluatePoliciesForEvent(event);
    }

    public List<ClaimResponse> scanAndEvaluateAllCities() {
        List<String> activeCities = policyRepository.findByStatus(Policy.PolicyStatus.ACTIVE)
                .stream()
                .map(p -> p.getUser() != null ? p.getUser().getCity() : null)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .collect(Collectors.toList());

        List<ClaimResponse> allClaims = new ArrayList<>();
        for (String city : activeCities) {
            try {
                allClaims.addAll(scanAndEvaluateCity(city));
            } catch (Exception ex) {
                log.warn("Failed weather scan for city {}: {}", city, ex.getMessage());
            }
        }
        return allClaims;
    }

    public List<ClaimResponse> evaluatePoliciesForEvent(WeatherEvent event) {
        List<ClaimResponse> allClaims = new ArrayList<>();
        List<Policy> activePolicies = policyRepository.findByStatus(Policy.PolicyStatus.ACTIVE);

        LocalDate eventDate = event.getEventTimestamp() != null
                ? event.getEventTimestamp().toLocalDate()
                : LocalDate.now();

        boolean severeWeather = isSevereWeather(event);

        for (Policy policy : activePolicies) {
            User user = policy.getUser();
            if (user == null || user.getCity() == null || event.getCity() == null) continue;
            if (!user.getCity().equalsIgnoreCase(event.getCity())) continue;

            boolean workerInactive = isWorkerInactive(user);
            String reason = "weatherSevere=" + severeWeather + ", workerInactive=" + workerInactive;

            boolean duplicate = claimRepository.existsByUserAndDisruptionDateAndTriggerType(user, eventDate, event.getEventType());
            if (duplicate || !severeWeather || !workerInactive) {
                claimTriggerRepository.save(ClaimTrigger.builder()
                        .policy(policy)
                        .user(user)
                        .city(event.getCity())
                        .zone(event.getZone())
                        .eventType(event.getEventType())
                        .weatherSevere(severeWeather)
                        .workerInactive(workerInactive)
                        .reason(duplicate ? reason + ", duplicate=true" : reason)
                        .claimCreated(false)
                        .build());
                continue;
            }

            try {
                ClaimResponse claim = claimService.triggerClaim(user, event);
                allClaims.add(claim);
                claimTriggerRepository.save(ClaimTrigger.builder()
                        .policy(policy)
                        .user(user)
                        .city(event.getCity())
                        .zone(event.getZone())
                        .eventType(event.getEventType())
                        .weatherSevere(true)
                        .workerInactive(true)
                        .reason(reason + ", auto-approved-and-paid=" + "PAID".equals(claim.getClaimStatus()))
                        .claimCreated(true)
                        .build());
            } catch (Exception ex) {
                log.warn("Claim trigger failed for user {}: {}", user.getEmail(), ex.getMessage());
            }
        }

        return allClaims;
    }

    private boolean isWorkerInactive(User user) {
        Optional<DeliveryActivity> latest = deliveryActivityRepository.findTopByUserOrderByDateDesc(user);
        if (latest.isEmpty()) return true;
        DeliveryActivity a = latest.get();
        int deliveries = a.getCompletedDeliveries() != null ? a.getCompletedDeliveries() : 0;
        double hours = a.getLoginHours() != null ? a.getLoginHours().doubleValue() : 0.0;
        return Boolean.FALSE.equals(a.getActiveStatus()) || deliveries == 0 || hours < 1.0;
    }

    private boolean isSevereWeather(WeatherEvent event) {
        return (event.getRainfallMm() != null && event.getRainfallMm().doubleValue() >= 30.0)
                || (event.getTemperature() != null && event.getTemperature().doubleValue() >= 42.0)
                || (event.getAqi() != null && event.getAqi() >= 300)
                || Boolean.TRUE.equals(event.getFloodAlert())
                || Boolean.TRUE.equals(event.getClosureAlert());
    }
}
