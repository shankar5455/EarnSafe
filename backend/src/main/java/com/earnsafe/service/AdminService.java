package com.earnsafe.service;

import com.earnsafe.dto.response.AdminDashboardResponse;
import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.dto.response.PolicyResponse;
import com.earnsafe.dto.response.UserResponse;
import com.earnsafe.entity.Claim;
import com.earnsafe.entity.FraudScore;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.RiskScore;
import com.earnsafe.entity.RiskZone;
import com.earnsafe.entity.User;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.FraudScoreRepository;
import com.earnsafe.repository.PolicyRepository;
import com.earnsafe.repository.RiskScoreRepository;
import com.earnsafe.repository.RiskZoneRepository;
import com.earnsafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final RiskZoneRepository riskZoneRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final FraudScoreRepository fraudScoreRepository;
    private final PolicyService policyService;
    private final ClaimService claimService;

    public AdminDashboardResponse getDashboard() {
        long totalWorkers = userRepository.countByRole(User.Role.WORKER);
        long activePolicies = policyRepository.countByStatus(Policy.PolicyStatus.ACTIVE);
        long totalClaims = claimRepository.count();
        long approvedClaims = claimRepository.countByClaimStatus(Claim.ClaimStatus.APPROVED);
        long rejectedClaims = claimRepository.countByClaimStatus(Claim.ClaimStatus.REJECTED);
        long paidClaims = claimRepository.countByClaimStatus(Claim.ClaimStatus.PAID);
        long pendingClaims = claimRepository.countByClaimStatus(Claim.ClaimStatus.TRIGGERED)
                + claimRepository.countByClaimStatus(Claim.ClaimStatus.UNDER_REVIEW)
                + claimRepository.countByClaimStatus(Claim.ClaimStatus.UNDER_VALIDATION);
        long fraudDetectedCount = claimRepository.countByFraudFlagTrue();
        BigDecimal totalPayouts = Optional.ofNullable(
                claimRepository.sumPayoutAmountForPaidClaims(Claim.ClaimStatus.PAID)
        ).orElse(BigDecimal.ZERO);

        LinkedHashMap<String, Long> triggerCounts = new LinkedHashMap<>();
        claimRepository.countByTriggerType().forEach(row -> triggerCounts.put((String) row[0], (Long) row[1]));

        LinkedHashMap<String, Long> claimsByStatus = new LinkedHashMap<>();
        claimRepository.countByStatus().forEach(row -> claimsByStatus.put(row[0].toString(), (Long) row[1]));

        List<AdminDashboardResponse.RiskZoneInfo> topZones = riskZoneRepository.findByOverallRiskLevel("HIGH")
                .stream()
                .limit(5)
                .map(rz -> AdminDashboardResponse.RiskZoneInfo.builder()
                        .city(rz.getCity())
                        .zone(rz.getZone())
                        .riskLevel(rz.getOverallRiskLevel())
                        .build())
                .collect(Collectors.toList());

        List<AdminDashboardResponse.FraudAlertInfo> fraudAlerts = fraudScoreRepository.findTop30ByOrderByEvaluatedAtDesc()
                .stream()
                .filter(f -> Boolean.TRUE.equals(f.getFraudFlag()))
                .limit(10)
                .map(f -> AdminDashboardResponse.FraudAlertInfo.builder()
                        .userName(f.getUser().getFullName())
                        .reason(f.getReason())
                        .score(f.getScore() != null ? f.getScore().doubleValue() : null)
                        .build())
                .collect(Collectors.toList());

        List<AdminDashboardResponse.RiskHeatPoint> riskHeatmap = riskScoreRepository.findTop30ByOrderByCalculatedAtDesc()
                .stream()
                .limit(30)
                .map(r -> AdminDashboardResponse.RiskHeatPoint.builder()
                        .city(r.getCity())
                        .zone(r.getZone())
                        .score(r.getScore() != null ? r.getScore().doubleValue() : 0.0)
                        .build())
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalWorkers(totalWorkers)
                .activePolicies(activePolicies)
                .totalClaims(totalClaims)
                .approvedClaims(approvedClaims)
                .rejectedClaims(rejectedClaims)
                .paidClaims(paidClaims)
                .pendingClaims(pendingClaims)
                .fraudDetectedCount(fraudDetectedCount)
                .totalPayouts(totalPayouts)
                .triggerCountByType(triggerCounts)
                .claimsByStatus(claimsByStatus)
                .topRiskyZones(topZones)
                .fraudAlerts(fraudAlerts)
                .riskHeatmap(riskHeatmap)
                .build();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AuthService::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public List<PolicyResponse> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(policyService::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ClaimResponse> getAllClaims() {
        return claimRepository.findAll().stream()
                .map(claimService::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RiskZone> getAllRiskZones() {
        return riskZoneRepository.findAll();
    }
}
