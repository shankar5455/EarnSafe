package com.earnsafe.service;

import com.earnsafe.dto.response.AdminDashboardResponse;
import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.dto.response.PolicyResponse;
import com.earnsafe.dto.response.UserResponse;
import com.earnsafe.entity.Claim;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.RiskZone;
import com.earnsafe.entity.User;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.PolicyRepository;
import com.earnsafe.repository.RiskZoneRepository;
import com.earnsafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final RiskZoneRepository riskZoneRepository;
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
                + claimRepository.countByClaimStatus(Claim.ClaimStatus.UNDER_VALIDATION);

        // Trigger counts
        Map<String, Long> triggerCounts = new LinkedHashMap<>();
        claimRepository.countByTriggerType().forEach(row ->
                triggerCounts.put((String) row[0], (Long) row[1]));

        // Claims by status
        Map<String, Long> claimsByStatus = new LinkedHashMap<>();
        claimRepository.countByStatus().forEach(row ->
                claimsByStatus.put(row[0].toString(), (Long) row[1]));

        // Top risky zones
        List<AdminDashboardResponse.RiskZoneInfo> topZones = riskZoneRepository.findByOverallRiskLevel("HIGH")
                .stream()
                .limit(5)
                .map(rz -> AdminDashboardResponse.RiskZoneInfo.builder()
                        .city(rz.getCity())
                        .zone(rz.getZone())
                        .riskLevel(rz.getOverallRiskLevel())
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
                .triggerCountByType(triggerCounts)
                .claimsByStatus(claimsByStatus)
                .topRiskyZones(topZones)
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
