package com.earnsafe.service;

import com.earnsafe.entity.RiskZone;
import com.earnsafe.entity.User;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.RiskZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * AI-like risk scoring service.
 * Computes a numeric risk score in [0.0, 1.0] from three weighted signals:
 *   - weatherSeverity  (0–10)
 *   - claimHistory     (recent claim count → higher count = higher risk)
 *   - locationRisk     (0–10, from RiskZone data)
 */
@Service
@RequiredArgsConstructor
public class RiskService {

    private static final double WEATHER_WEIGHT  = 0.35;
    private static final double HISTORY_WEIGHT  = 0.30;
    private static final double LOCATION_WEIGHT = 0.35;

    private final RiskZoneRepository riskZoneRepository;
    private final ClaimRepository claimRepository;

    /**
     * Calculate a composite risk score for the given inputs.
     *
     * @param weatherSeverity 0–10 severity of recent weather in user's zone
     * @param claimHistory    number of claims filed in the last 90 days
     * @param locationRisk    0–10 overall risk score of the user's zone
     * @return risk score in [0.0, 1.0]
     */
    public double calculateRiskScore(double weatherSeverity, int claimHistory, double locationRisk) {
        double normWeather  = clamp(weatherSeverity / 10.0);
        double normHistory  = clamp(Math.log1p(claimHistory) / Math.log1p(10)); // log-scale; 10 claims → 1.0
        double normLocation = clamp(locationRisk / 10.0);

        double score = (normWeather  * WEATHER_WEIGHT)
                     + (normHistory  * HISTORY_WEIGHT)
                     + (normLocation * LOCATION_WEIGHT);
        return Math.round(clamp(score) * 100.0) / 100.0;
    }

    /**
     * Convenience method – derives all inputs from the User entity and RiskZone data.
     */
    public double calculateRiskScoreForUser(User user) {
        // Determine location risk from RiskZone registry
        double locationRisk = 5.0; // default medium
        Optional<RiskZone> rzOpt = riskZoneRepository.findByCityAndZone(user.getCity(), user.getZone());
        if (rzOpt.isPresent()) {
            RiskZone rz = rzOpt.get();
            locationRisk = (rz.getRainRiskScore()
                            + rz.getFloodRiskScore()
                            + rz.getHeatRiskScore()
                            + rz.getPollutionRiskScore()
                            + rz.getClosureRiskScore()) / 5.0;
        }

        // Derive weather severity from the location risk (used as a proxy when no live weather feed is configured)
        double weatherSeverity = locationRisk;

        // Count claims in the last 90 days
        int recentClaims = claimRepository.countByUserAndCreatedAfter(
                user, LocalDate.now().minusDays(90).atStartOfDay());

        return calculateRiskScore(weatherSeverity, recentClaims, locationRisk);
    }

    private double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
