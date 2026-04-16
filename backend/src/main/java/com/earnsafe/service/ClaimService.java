package com.earnsafe.service;

import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.entity.Claim;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;
import com.earnsafe.entity.WeatherEvent;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final FraudService fraudService;
    private final PayoutService payoutService;
    private final AiInferenceService aiInferenceService;

    public List<ClaimResponse> getMyClaims(User user) {
        return claimRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ClaimResponse getClaimById(Long id, User user) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        if (!claim.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        return mapToResponse(claim);
    }

    public ClaimResponse triggerClaim(User user, WeatherEvent event) {
        // Find active policy
        Policy policy = policyRepository.findActiveByUser(user)
                .orElseThrow(() -> new RuntimeException("No active policy found for user"));

        LocalDate disruptionDate = event.getEventTimestamp() != null
                ? event.getEventTimestamp().toLocalDate()
                : LocalDate.now();

        // Duplicate prevention: skip if a claim already exists for this policy + date + trigger type
        boolean isDuplicate = claimRepository.existsByUserAndDisruptionDateAndTriggerType(
                user, disruptionDate, event.getEventType());

        if (isDuplicate) {
            log.info("Duplicate claim detected for user {} on {} (type: {}). Skipping creation.",
                    user.getEmail(), disruptionDate, event.getEventType());
            throw new RuntimeException("Duplicate claim: a claim already exists for this policy, date, and event type");
        }

        // AI fraud detection
        FraudService.FraudResult fraudResult = fraudService.evaluate(user, event);

        // Estimate lost hours and income
        BigDecimal avgWorkHours = user.getAverageWorkingHours() != null
                ? user.getAverageWorkingHours()
                : new BigDecimal("6");
        BigDecimal avgDailyEarnings = user.getAverageDailyEarnings() != null
                ? user.getAverageDailyEarnings()
                : new BigDecimal("500");

        BigDecimal lostHoursFactor = getImpactFactor(user, event);
        BigDecimal estimatedLostHours = avgWorkHours.multiply(lostHoursFactor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedLostIncome = avgDailyEarnings.multiply(lostHoursFactor).setScale(2, RoundingMode.HALF_UP);

        // If fraudScore >= 0.5 → UNDER_REVIEW, else TRIGGERED
        Claim.ClaimStatus initialStatus = fraudResult.fraudFlag()
                ? Claim.ClaimStatus.UNDER_REVIEW
                : Claim.ClaimStatus.TRIGGERED;

        Claim claim = Claim.builder()
                .claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .policy(policy)
                .triggerType(event.getEventType())
                .disruptionDate(disruptionDate)
                .city(event.getCity())
                .zone(event.getZone())
                .estimatedLostHours(estimatedLostHours)
                .estimatedLostIncome(estimatedLostIncome)
                .validationStatus("PENDING")
                .claimStatus(initialStatus)
                .fraudFlag(fraudResult.fraudFlag())
                .fraudReason(fraudResult.reason())
                .fraudScore(fraudResult.fraudScore())
                .payoutAmount(fraudResult.fraudFlag() ? BigDecimal.ZERO : estimatedLostIncome)
                .build();

        claim = claimRepository.save(claim);

        if (!fraudResult.fraudFlag()) {
            // Auto-approve non-fraud claims
            claim.setClaimStatus(Claim.ClaimStatus.APPROVED);
            claim.setValidationStatus("AUTO_APPROVED");
            claimRepository.save(claim);
        }

        return mapToResponse(claim);
    }

    public ClaimResponse approveClaim(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        claim.setClaimStatus(Claim.ClaimStatus.APPROVED);
        claim.setValidationStatus("MANUALLY_APPROVED");
        claim.setFraudFlag(false);
        return mapToResponse(claimRepository.save(claim));
    }

    public ClaimResponse rejectClaim(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        claim.setClaimStatus(Claim.ClaimStatus.REJECTED);
        claim.setValidationStatus("REJECTED");
        claim.setPayoutAmount(BigDecimal.ZERO);
        return mapToResponse(claimRepository.save(claim));
    }

    public ClaimResponse markPaid(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        Claim paid = payoutService.processPayout(claim);
        return mapToResponse(paid);
    }

    private BigDecimal getImpactFactor(User user, WeatherEvent event) {
        return aiInferenceService.predictImpactFactor(user, event);
    }

    public ClaimResponse mapToResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .userId(claim.getUser().getId())
                .userFullName(claim.getUser().getFullName())
                .policyId(claim.getPolicy().getId())
                .policyNumber(claim.getPolicy().getPolicyNumber())
                .triggerType(claim.getTriggerType())
                .disruptionDate(claim.getDisruptionDate())
                .city(claim.getCity())
                .zone(claim.getZone())
                .estimatedLostHours(claim.getEstimatedLostHours())
                .estimatedLostIncome(claim.getEstimatedLostIncome())
                .validationStatus(claim.getValidationStatus())
                .claimStatus(claim.getClaimStatus().name())
                .fraudFlag(claim.getFraudFlag())
                .fraudReason(claim.getFraudReason())
                .fraudScore(claim.getFraudScore())
                .transactionId(claim.getTransactionId())
                .payoutAmount(claim.getPayoutAmount())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
