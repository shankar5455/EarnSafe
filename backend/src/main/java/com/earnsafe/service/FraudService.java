package com.earnsafe.service;

import com.earnsafe.entity.Claim;
import com.earnsafe.entity.DeliveryActivity;
import com.earnsafe.entity.FraudScore;
import com.earnsafe.entity.User;
import com.earnsafe.entity.WeatherEvent;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.DeliveryActivityRepository;
import com.earnsafe.repository.FraudScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudService {

    private static final double VELOCITY_MULTIPLIER_PER_CLAIM = 12.0;
    private static final double WEEKLY_VELOCITY_WEIGHT = 0.5;
    private static final double DEFAULT_SUSPICIOUS_VELOCITY = 2.0;
    private static final int HIGH_DELIVERY_THRESHOLD = 40;
    private static final double LOW_LOGIN_HOURS_THRESHOLD = 3.0;
    private static final double HIGH_INCOME_WITH_ZERO_DELIVERIES_THRESHOLD = 800.0;
    private static final double EXCESSIVE_LOGIN_HOURS_THRESHOLD = 12.0;
    private static final int VERY_LOW_DELIVERIES_THRESHOLD = 1;

    private static final Map<String, double[]> CITY_COORDINATES = Map.of(
            "mumbai", new double[]{19.0760, 72.8777},
            "delhi", new double[]{28.6139, 77.2090},
            "bangalore", new double[]{12.9716, 77.5946},
            "hyderabad", new double[]{17.3850, 78.4867},
            "chennai", new double[]{13.0827, 80.2707},
            "kolkata", new double[]{22.5726, 88.3639}
    );

    private final ClaimRepository claimRepository;
    private final DeliveryActivityRepository deliveryActivityRepository;
    private final FraudScoreRepository fraudScoreRepository;
    private final WeatherService weatherService;
    private final AiInferenceService aiInferenceService;

    @Value("${app.fraud.location-threshold-km:35.0}")
    private double locationMismatchThresholdKm;

    @Value("${app.fraud.max-claims-24h:2}")
    private int maxClaims24h;

    @Value("${app.fraud.max-claims-7d:4}")
    private int maxClaims7d;

    public FraudResult evaluate(User user, WeatherEvent event) {
        LocalDateTime since24h = LocalDateTime.now().minusHours(24);
        LocalDateTime since7d = LocalDateTime.now().minusDays(7);
        List<Claim> recentClaims = claimRepository.findByUserAndCreatedAtAfter(user, since24h);
        List<Claim> weeklyClaims = claimRepository.findByUserAndCreatedAtAfter(user, since7d);
        int pastClaims = (int) claimRepository.countByUser(user);

        Optional<DeliveryActivity> latestActivityOpt = deliveryActivityRepository.findTopByUserOrderByDateDesc(user);
        double workerActivity = latestActivityOpt.map(this::activityScore).orElse(0.4);
        boolean workerInactive = latestActivityOpt.map(this::isInactive).orElse(true);
        double unrealisticActivityPattern = latestActivityOpt.map(this::unrealisticActivityPattern).orElse(0.1);

        double weatherSeverity = weatherService.calculateSeverity(event).doubleValue();
        double gpsDistanceKm = inferGpsDistanceKm(user, event);
        boolean locationMismatch = gpsDistanceKm > locationMismatchThresholdKm;
        boolean inactivityMismatch = !workerInactive && weatherSeverity < 4.0;
        boolean tooManyRecentClaims = recentClaims.size() > maxClaims24h;
        boolean tooManyWeeklyClaims = weeklyClaims.size() > maxClaims7d;
        double suspiciousVelocity = calculateSuspiciousVelocity(recentClaims.size(), weeklyClaims.size());

        FraudResult aiResult = aiInferenceService.predictFraud(
                user,
                weatherSeverity,
                recentClaims.size(),
                pastClaims,
                workerActivity,
                gpsDistanceKm,
                inactivityMismatch,
                suspiciousVelocity
        );

        List<String> ruleReasons = new ArrayList<>();
        double scoreBoost = 0.0;
        if (locationMismatch) {
            ruleReasons.add("Location mismatch: worker-event distance " + Math.round(gpsDistanceKm) + "km exceeds " + Math.round(locationMismatchThresholdKm) + "km threshold");
            scoreBoost += 0.35;
        }
        if (tooManyRecentClaims) {
            ruleReasons.add("Too many claims in 24h: " + recentClaims.size());
            scoreBoost += 0.25;
        }
        if (tooManyWeeklyClaims) {
            ruleReasons.add("High 7-day claim velocity: " + weeklyClaims.size());
            scoreBoost += 0.20;
        }
        if (unrealisticActivityPattern >= 0.6) {
            ruleReasons.add("Unrealistic activity pattern detected");
            scoreBoost += 0.20;
        }
        if (inactivityMismatch) {
            ruleReasons.add("Inactivity mismatch with mild weather");
            scoreBoost += 0.10;
        }

        double finalScore = Math.max(0.0, Math.min(1.0, aiResult.fraudScore() + scoreBoost));
        boolean finalFlag = aiResult.fraudFlag() || locationMismatch || tooManyRecentClaims || finalScore >= 0.65;
        String finalReason = ruleReasons.isEmpty()
                ? aiResult.reason()
                : String.join(" | ", ruleReasons) + " | AI: " + aiResult.reason();
        FraudResult result = new FraudResult(finalScore, finalFlag, finalReason);

        fraudScoreRepository.save(FraudScore.builder()
                .user(user)
                .score(BigDecimal.valueOf(result.fraudScore()))
                .fraudFlag(result.fraudFlag())
                .reason(result.reason())
                .build());

        log.info("[FraudService] fraudScore={} fraudFlag={} user={}", result.fraudScore(), result.fraudFlag(), user.getEmail());
        return result;
    }

    private boolean isInactive(DeliveryActivity activity) {
        if (Boolean.FALSE.equals(activity.getActiveStatus())) return true;
        int deliveries = activity.getCompletedDeliveries() != null ? activity.getCompletedDeliveries() : 0;
        double loginHours = activity.getLoginHours() != null ? activity.getLoginHours().doubleValue() : 0.0;
        return deliveries == 0 || loginHours < 1.0;
    }

    private double activityScore(DeliveryActivity activity) {
        double deliveries = activity.getCompletedDeliveries() != null ? activity.getCompletedDeliveries() : 0;
        double loginHours = activity.getLoginHours() != null ? activity.getLoginHours().doubleValue() : 0.0;
        double normalized = (deliveries / 20.0) * 0.5 + (loginHours / 10.0) * 0.5;
        if (Boolean.FALSE.equals(activity.getActiveStatus())) normalized -= 0.2;
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    private double unrealisticActivityPattern(DeliveryActivity activity) {
        int deliveries = activity.getCompletedDeliveries() != null ? activity.getCompletedDeliveries() : 0;
        double loginHours = activity.getLoginHours() != null ? activity.getLoginHours().doubleValue() : 0.0;
        double estimatedIncome = activity.getEstimatedDailyIncome() != null ? activity.getEstimatedDailyIncome().doubleValue() : 0.0;

        if (deliveries >= HIGH_DELIVERY_THRESHOLD && loginHours <= LOW_LOGIN_HOURS_THRESHOLD) return 0.9;
        if (deliveries == 0 && estimatedIncome > HIGH_INCOME_WITH_ZERO_DELIVERIES_THRESHOLD) return 0.8;
        if (loginHours >= EXCESSIVE_LOGIN_HOURS_THRESHOLD && deliveries <= VERY_LOW_DELIVERIES_THRESHOLD) return 0.7;
        return 0.2;
    }

    private double calculateSuspiciousVelocity(int recentClaimCount, int weeklyClaimCount) {
        boolean elevatedShortTermVelocity = recentClaimCount > 1;
        boolean elevatedWeeklyVelocity = weeklyClaimCount > maxClaims7d;
        if (!elevatedShortTermVelocity && !elevatedWeeklyVelocity) {
            return DEFAULT_SUSPICIOUS_VELOCITY;
        }
        return (recentClaimCount + (weeklyClaimCount * WEEKLY_VELOCITY_WEIGHT)) * VELOCITY_MULTIPLIER_PER_CLAIM;
    }

    private double inferGpsDistanceKm(User user, WeatherEvent event) {
        double[] userCoord = CITY_COORDINATES.getOrDefault(
                user.getCity() != null ? user.getCity().toLowerCase() : "",
                new double[]{20.5937, 78.9629}
        );
        double[] eventCoord = CITY_COORDINATES.getOrDefault(
                event.getCity() != null ? event.getCity().toLowerCase() : "",
                new double[]{20.5937, 78.9629}
        );
        return haversine(userCoord[0], userCoord[1], eventCoord[0], eventCoord[1]);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public record FraudResult(double fraudScore, boolean fraudFlag, String reason) {}
}
