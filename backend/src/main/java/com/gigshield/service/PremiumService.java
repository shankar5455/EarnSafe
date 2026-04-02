package com.gigshield.service;

import com.gigshield.dto.response.PremiumCalculationResponse;
import com.gigshield.entity.RiskZone;
import com.gigshield.entity.User;
import com.gigshield.repository.RiskZoneRepository;
import com.gigshield.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PremiumService {

    private static final BigDecimal BASE_PREMIUM = new BigDecimal("39");

    private final RiskZoneRepository riskZoneRepository;
    private final UserRepository userRepository;

    public PremiumCalculationResponse calculate(User user) {
        List<PremiumCalculationResponse.BreakdownItem> breakdown = new ArrayList<>();
        StringBuilder explanation = new StringBuilder();
        BigDecimal total = BASE_PREMIUM;

        breakdown.add(new PremiumCalculationResponse.BreakdownItem("Base Premium", BASE_PREMIUM));

        // Get zone risk
        Optional<RiskZone> riskZoneOpt = riskZoneRepository.findByCityAndZone(user.getCity(), user.getZone());
        RiskZone riskZone = riskZoneOpt.orElse(null);

        int rainRisk = riskZone != null ? riskZone.getRainRiskScore() : 5;
        int floodRisk = riskZone != null ? riskZone.getFloodRiskScore() : 5;
        int heatRisk = riskZone != null ? riskZone.getHeatRiskScore() : 5;
        int pollutionRisk = riskZone != null ? riskZone.getPollutionRiskScore() : 5;
        int closureRisk = riskZone != null ? riskZone.getClosureRiskScore() : 5;
        int overallRisk = (rainRisk + floodRisk + heatRisk + pollutionRisk + closureRisk) / 5;

        // Rain risk adjustment
        if (rainRisk >= 8) {
            BigDecimal adj = new BigDecimal("8");
            total = total.add(adj);
            breakdown.add(new PremiumCalculationResponse.BreakdownItem("High Rain Risk", adj));
            explanation.append("Your zone has high rain risk. ");
        } else if (rainRisk >= 5) {
            BigDecimal adj = new BigDecimal("4");
            total = total.add(adj);
            breakdown.add(new PremiumCalculationResponse.BreakdownItem("Moderate Rain Risk", adj));
        }

        // Flood risk adjustment
        if (floodRisk >= 8) {
            BigDecimal adj = new BigDecimal("12");
            total = total.add(adj);
            breakdown.add(new PremiumCalculationResponse.BreakdownItem("High Flood Risk", adj));
            explanation.append("Your zone is flood-prone. ");
        } else if (floodRisk >= 5) {
            BigDecimal adj = new BigDecimal("6");
            total = total.add(adj);
            breakdown.add(new PremiumCalculationResponse.BreakdownItem("Moderate Flood Risk", adj));
        }

        // Heat risk
        if (heatRisk >= 8) {
            BigDecimal adj = new BigDecimal("5");
            total = total.add(adj);
            breakdown.add(new PremiumCalculationResponse.BreakdownItem("High Heat Risk", adj));
        }

        // Pollution risk
        if (pollutionRisk >= 8) {
            BigDecimal adj = new BigDecimal("4");
            total = total.add(adj);
            breakdown.add(new PremiumCalculationResponse.BreakdownItem("High Pollution Risk", adj));
        }

        // Shift adjustment
        if ("Night".equalsIgnoreCase(user.getPreferredShift())) {
            BigDecimal adj = new BigDecimal("5");
            total = total.add(adj);
            breakdown.add(new PremiumCalculationResponse.BreakdownItem("Night Shift", adj));
            explanation.append("You work in the high-disruption night shift. ");
        }

        // Earnings adjustment
        if (user.getAverageDailyEarnings() != null && user.getAverageDailyEarnings().compareTo(new BigDecimal("800")) > 0) {
            BigDecimal adj = new BigDecimal("6");
            total = total.add(adj);
            breakdown.add(new PremiumCalculationResponse.BreakdownItem("High Earner Premium", adj));
        }

        // Safe zone discount
        if (overallRisk < 4) {
            BigDecimal disc = new BigDecimal("-5");
            total = total.add(disc);
            breakdown.add(new PremiumCalculationResponse.BreakdownItem("Safe Zone Discount", disc));
            explanation.append("You benefit from a safe zone discount. ");
        }

        // Category adjustment
        if ("Grocery".equalsIgnoreCase(user.getDeliveryCategory())) {
            BigDecimal adj = new BigDecimal("3");
            total = total.add(adj);
            breakdown.add(new PremiumCalculationResponse.BreakdownItem("Grocery Delivery Risk", adj));
        }

        // Determine risk score
        String riskScore;
        if (overallRisk >= 7) {
            riskScore = "HIGH";
        } else if (overallRisk >= 4) {
            riskScore = "MEDIUM";
        } else {
            riskScore = "LOW";
        }

        if (explanation.length() == 0) {
            explanation.append("Your weekly premium reflects your zone risk and work profile.");
        }

        return PremiumCalculationResponse.builder()
                .finalWeeklyPremium(total.setScale(2, RoundingMode.HALF_UP))
                .riskScore(riskScore)
                .breakdown(breakdown)
                .explanation(explanation.toString().trim())
                .build();
    }

    public PremiumCalculationResponse calculateForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return calculate(user);
    }
}
