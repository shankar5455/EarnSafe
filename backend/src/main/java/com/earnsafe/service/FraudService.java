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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudService {

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

    public FraudResult evaluate(User user, WeatherEvent event) {
        LocalDateTime since24h = LocalDateTime.now().minusHours(24);
        List<Claim> recentClaims = claimRepository.findByUserAndCreatedAtAfter(user, since24h);
        int pastClaims = (int) claimRepository.countByUser(user);

        Optional<DeliveryActivity> latestActivityOpt = deliveryActivityRepository.findTopByUserOrderByDateDesc(user);
        double workerActivity = latestActivityOpt.map(this::activityScore).orElse(0.4);
        boolean workerInactive = latestActivityOpt.map(this::isInactive).orElse(true);

        double weatherSeverity = weatherService.calculateSeverity(event).doubleValue();
        double gpsDistanceKm = inferGpsDistanceKm(user, event);
        boolean inactivityMismatch = !workerInactive && weatherSeverity < 4.0;
        double suspiciousVelocity = recentClaims.size() > 1 ? recentClaims.size() * 12.0 : 2.0;

        FraudResult result = aiInferenceService.predictFraud(
                user,
                weatherSeverity,
                recentClaims.size(),
                pastClaims,
                workerActivity,
                gpsDistanceKm,
                inactivityMismatch,
                suspiciousVelocity
        );

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
