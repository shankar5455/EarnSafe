package com.earnsafe.service;

import com.earnsafe.dto.request.PolicyCreateRequest;
import com.earnsafe.dto.response.PolicyResponse;
import com.earnsafe.dto.response.PremiumCalculationResponse;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;
import com.earnsafe.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PremiumService premiumService;

    public PolicyResponse createPolicy(User user, PolicyCreateRequest request) {
        // Deactivate any existing active policy
        policyRepository.findActiveByUser(user).ifPresent(existing -> {
            existing.setStatus(Policy.PolicyStatus.INACTIVE);
            policyRepository.save(existing);
        });

        // Calculate premium
        PremiumCalculationResponse premium = premiumService.calculate(user);

        Policy policy = Policy.builder()
                .policyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .planName(request.getPlanName() != null ? request.getPlanName() : "Standard Weekly Plan")
                .weeklyPremium(request.getWeeklyPremium() != null ? request.getWeeklyPremium() : premium.getFinalWeeklyPremium())
                .weeklyCoverageAmount(request.getWeeklyCoverageAmount() != null ? request.getWeeklyCoverageAmount() : premium.getFinalWeeklyPremium().multiply(new java.math.BigDecimal("10")))
                .coveredHours(request.getCoveredHours() != null ? request.getCoveredHours() : 40)
                .coveredDisruptions(request.getCoveredDisruptions() != null ? request.getCoveredDisruptions() : "HEAVY_RAIN,FLOOD_ALERT,HEATWAVE,POLLUTION_SPIKE,ZONE_CLOSURE")
                .zoneCovered(user.getZone() != null ? user.getZone() : user.getCity())
                .status(Policy.PolicyStatus.ACTIVE)
                .riskScore(premium.getRiskScore())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(4))
                .build();

        policy = policyRepository.save(policy);
        return mapToResponse(policy);
    }

    public List<PolicyResponse> getMyPolicies(User user) {
        return policyRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public PolicyResponse getPolicyById(Long id, User user) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        if (!policy.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        return mapToResponse(policy);
    }

    public PolicyResponse renewPolicy(Long id, User user) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        policy.setStatus(Policy.PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(LocalDate.now().plusWeeks(4));
        return mapToResponse(policyRepository.save(policy));
    }

    public PolicyResponse pausePolicy(Long id, User user) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        policy.setStatus(Policy.PolicyStatus.PAUSED);
        return mapToResponse(policyRepository.save(policy));
    }

    public PolicyResponse deactivatePolicy(Long id, User user) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        policy.setStatus(Policy.PolicyStatus.INACTIVE);
        return mapToResponse(policyRepository.save(policy));
    }

    public PolicyResponse mapToResponse(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .userId(policy.getUser().getId())
                .userFullName(policy.getUser().getFullName())
                .planName(policy.getPlanName())
                .weeklyPremium(policy.getWeeklyPremium())
                .weeklyCoverageAmount(policy.getWeeklyCoverageAmount())
                .coveredHours(policy.getCoveredHours())
                .coveredDisruptions(policy.getCoveredDisruptions())
                .zoneCovered(policy.getZoneCovered())
                .status(policy.getStatus().name())
                .riskScore(policy.getRiskScore())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .createdAt(policy.getCreatedAt())
                .build();
    }
}
