package com.earnsafe.service;

import com.earnsafe.entity.DeliveryActivity;
import com.earnsafe.entity.RiskScore;
import com.earnsafe.entity.RiskZone;
import com.earnsafe.entity.User;
import com.earnsafe.entity.WeatherData;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.DeliveryActivityRepository;
import com.earnsafe.repository.RiskScoreRepository;
import com.earnsafe.repository.RiskZoneRepository;
import com.earnsafe.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskZoneRepository riskZoneRepository;
    private final ClaimRepository claimRepository;
    private final DeliveryActivityRepository deliveryActivityRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final AiInferenceService aiInferenceService;

    public AiInferenceService.RiskPrediction calculateRiskForUser(User user) {
        double locationRisk = resolveLocationRisk(user);
        int pastClaims = claimRepository.countByUserAndCreatedAfter(user, LocalDate.now().minusDays(90).atStartOfDay());
        double weatherSeverity = resolveWeatherSeverity(user, locationRisk);
        double workerActivity = resolveWorkerActivity(user);

        AiInferenceService.RiskPrediction prediction = aiInferenceService.predictRisk(
                user,
                weatherSeverity,
                pastClaims,
                locationRisk,
                workerActivity
        );

        riskScoreRepository.save(RiskScore.builder()
                .user(user)
                .city(user.getCity() == null ? "UNKNOWN" : user.getCity())
                .zone(user.getZone() == null ? "UNKNOWN" : user.getZone())
                .score(BigDecimal.valueOf(prediction.riskScore()))
                .suggestedPremium(prediction.premium())
                .riskLevel(prediction.riskLevel())
                .build());

        return prediction;
    }

    public double calculateRiskScoreForUser(User user) {
        return calculateRiskForUser(user).riskScore() / 100.0;
    }

    private double resolveLocationRisk(User user) {
        Optional<RiskZone> riskZone = riskZoneRepository.findByCityAndZone(user.getCity(), user.getZone());
        if (riskZone.isEmpty()) {
            return 5.0;
        }
        RiskZone rz = riskZone.get();
        return (rz.getRainRiskScore() + rz.getFloodRiskScore() + rz.getHeatRiskScore() + rz.getPollutionRiskScore() + rz.getClosureRiskScore()) / 5.0;
    }

    private double resolveWeatherSeverity(User user, double fallbackLocationRisk) {
        if (user.getCity() == null || user.getCity().isBlank()) {
            return fallbackLocationRisk;
        }
        return weatherDataRepository.findByCityOrderByObservedAtDesc(user.getCity())
                .stream()
                .findFirst()
                .map(WeatherData::getSeverityScore)
                .map(BigDecimal::doubleValue)
                .orElse(fallbackLocationRisk);
    }

    private double resolveWorkerActivity(User user) {
        Optional<DeliveryActivity> latest = deliveryActivityRepository.findTopByUserOrderByDateDesc(user);
        if (latest.isEmpty()) {
            return 0.5;
        }
        DeliveryActivity activity = latest.get();
        double hours = activity.getLoginHours() != null ? activity.getLoginHours().doubleValue() : 0;
        double deliveries = activity.getCompletedDeliveries() != null ? activity.getCompletedDeliveries() : 0;
        double activeBonus = Boolean.TRUE.equals(activity.getActiveStatus()) ? 0.2 : -0.2;
        double normalized = Math.min(1.0, (hours / 10.0) * 0.6 + (deliveries / 25.0) * 0.4 + activeBonus);
        return Math.max(0.0, normalized);
    }
}
