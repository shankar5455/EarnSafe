package com.earnsafe.service;

import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.entity.Claim;
import com.earnsafe.entity.DeliveryActivity;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;
import com.earnsafe.entity.WeatherEvent;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.DeliveryActivityRepository;
import com.earnsafe.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final DeliveryActivityRepository deliveryActivityRepository;
    private final FraudService fraudService;
    private final PayoutService payoutService;
    private final WeatherService weatherService;

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
        Policy policy = policyRepository.findActiveByUser(user)
                .orElseThrow(() -> new RuntimeException("No active policy found for user"));

        LocalDate disruptionDate = event.getEventTimestamp() != null
                ? event.getEventTimestamp().toLocalDate()
                : LocalDate.now();

        boolean isDuplicate = claimRepository.existsByUserAndDisruptionDateAndTriggerType(
                user, disruptionDate, event.getEventType());

        if (isDuplicate) {
            throw new RuntimeException("Duplicate claim: claim already exists for this date and event type");
        }

        FraudService.FraudResult fraudResult = fraudService.evaluate(user, event);

        BigDecimal avgWorkHours = user.getAverageWorkingHours() != null ? user.getAverageWorkingHours() : new BigDecimal("6");
        BigDecimal avgDailyEarnings = user.getAverageDailyEarnings() != null ? user.getAverageDailyEarnings() : new BigDecimal("500");

        BigDecimal lostHoursFactor = getImpactFactor(user, event);
        BigDecimal estimatedLostHours = avgWorkHours.multiply(lostHoursFactor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedLostIncome = avgDailyEarnings.multiply(lostHoursFactor).setScale(2, RoundingMode.HALF_UP);

        Claim.ClaimStatus initialStatus = fraudResult.fraudFlag()
                ? Claim.ClaimStatus.REJECTED
                : Claim.ClaimStatus.APPROVED;

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
                .validationStatus(fraudResult.fraudFlag() ? "AUTO_REJECTED_FRAUD" : "AUTO_APPROVED")
                .claimStatus(initialStatus)
                .fraudFlag(fraudResult.fraudFlag())
                .fraudReason(fraudResult.reason())
                .fraudScore(fraudResult.fraudScore())
                .payoutAmount(fraudResult.fraudFlag() ? BigDecimal.ZERO : estimatedLostIncome)
                .payoutStatus(Claim.PayoutStatus.PENDING)
                .payoutRetryPending(false)
                .payoutRetryCount(0)
                .build();

        claim = claimRepository.save(claim);

        if (!fraudResult.fraudFlag()) {
            claim = payoutService.processPayout(claim);
        }

        return mapToResponse(claim);
    }

    private BigDecimal getImpactFactor(User user, WeatherEvent event) {
        BigDecimal severity = weatherService.calculateSeverity(event);
        Optional<DeliveryActivity> latest = deliveryActivityRepository.findTopByUserOrderByDateDesc(user);

        double activityPenalty = latest.map(a -> {
            int deliveries = a.getCompletedDeliveries() != null ? a.getCompletedDeliveries() : 0;
            if (Boolean.FALSE.equals(a.getActiveStatus()) || deliveries == 0) return 0.25;
            return 0.1;
        }).orElse(0.2);

        double factor = Math.min(1.0, Math.max(0.2, (severity.doubleValue() / 10.0) + activityPenalty));
        return BigDecimal.valueOf(factor).setScale(2, RoundingMode.HALF_UP);
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
                .payoutStatus(claim.getPayoutStatus() != null ? claim.getPayoutStatus().name() : null)
                .payoutRetryPending(claim.getPayoutRetryPending())
                .payoutRetryCount(claim.getPayoutRetryCount())
                .payoutFailureReason(claim.getPayoutFailureReason())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
