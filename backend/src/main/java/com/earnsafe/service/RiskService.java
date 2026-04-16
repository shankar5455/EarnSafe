package com.earnsafe.service;

import com.earnsafe.entity.RiskZone;
import com.earnsafe.entity.User;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.RiskZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskZoneRepository riskZoneRepository;
    private final ClaimRepository claimRepository;
    private final AiInferenceService aiInferenceService;

    public double calculateRiskScore(double weatherSeverity, int claimHistory, double locationRisk) {
        User syntheticUser = User.builder().build();
        return aiInferenceService.predictRiskScore(syntheticUser, weatherSeverity, claimHistory, locationRisk);
    }

    public double calculateRiskScoreForUser(User user) {
        double locationRisk = 5.0;
        Optional<RiskZone> rzOpt = riskZoneRepository.findByCityAndZone(user.getCity(), user.getZone());
        if (rzOpt.isPresent()) {
            RiskZone rz = rzOpt.get();
            locationRisk = (rz.getRainRiskScore()
                            + rz.getFloodRiskScore()
                            + rz.getHeatRiskScore()
                            + rz.getPollutionRiskScore()
                            + rz.getClosureRiskScore()) / 5.0;
        }

        double weatherSeverity = locationRisk;

        int recentClaims = claimRepository.countByUserAndCreatedAfter(
                user, LocalDate.now().minusDays(90).atStartOfDay());

        return aiInferenceService.predictRiskScore(user, weatherSeverity, recentClaims, locationRisk);
    }
}
