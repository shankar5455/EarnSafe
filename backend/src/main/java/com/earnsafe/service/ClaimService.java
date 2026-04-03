package com.earnsafe.service;

import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.entity.Claim;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;
import com.earnsafe.entity.WeatherEvent;
import com.earnsafe.repository.ClaimRepository;
import com.earnsafe.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;

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

        // Fraud checks
        boolean isDuplicate = claimRepository.existsByUserAndDisruptionDateAndTriggerType(
                user, disruptionDate, event.getEventType());

        boolean fraudFlag = false;
        String fraudReason = null;

        if (isDuplicate) {
            fraudFlag = true;
            fraudReason = "Duplicate claim for same date and trigger type";
        }

        // Zone mismatch check
        String userZone = user.getZone() != null ? user.getZone() : user.getCity();
        String eventZone = event.getZone() != null ? event.getZone() : event.getCity();
        if (!userZone.equalsIgnoreCase(eventZone) && !user.getCity().equalsIgnoreCase(event.getCity())) {
            fraudFlag = true;
            fraudReason = "Zone mismatch between policy and event";
        }

        // Estimate lost hours and income
        BigDecimal avgWorkHours = user.getAverageWorkingHours() != null
                ? user.getAverageWorkingHours()
                : new BigDecimal("6");
        BigDecimal avgDailyEarnings = user.getAverageDailyEarnings() != null
                ? user.getAverageDailyEarnings()
                : new BigDecimal("500");

        BigDecimal lostHoursFactor = getImpactFactor(event);
        BigDecimal estimatedLostHours = avgWorkHours.multiply(lostHoursFactor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedLostIncome = avgDailyEarnings.multiply(lostHoursFactor).setScale(2, RoundingMode.HALF_UP);

        Claim.ClaimStatus initialStatus = fraudFlag
                ? Claim.ClaimStatus.UNDER_VALIDATION
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
                .fraudFlag(fraudFlag)
                .fraudReason(fraudReason)
                .payoutAmount(fraudFlag ? BigDecimal.ZERO : estimatedLostIncome)
                .build();

        claim = claimRepository.save(claim);

        if (!fraudFlag) {
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
        if (claim.getClaimStatus() != Claim.ClaimStatus.APPROVED) {
            throw new RuntimeException("Only approved claims can be marked as paid");
        }
        claim.setClaimStatus(Claim.ClaimStatus.PAID);
        claim.setValidationStatus("PAID");
        return mapToResponse(claimRepository.save(claim));
    }

    private BigDecimal getImpactFactor(WeatherEvent event) {
        return switch (event.getEventType()) {
            case "FLOOD_ALERT" -> new BigDecimal("0.9");
            case "HEAVY_RAIN" -> new BigDecimal("0.7");
            case "ZONE_CLOSURE" -> new BigDecimal("1.0");
            case "HEATWAVE" -> new BigDecimal("0.5");
            case "POLLUTION_SPIKE" -> new BigDecimal("0.4");
            default -> new BigDecimal("0.6");
        };
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
                .payoutAmount(claim.getPayoutAmount())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
