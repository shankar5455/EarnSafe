package com.earnsafe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    private Long id;
    private String claimNumber;
    private Long userId;
    private String userFullName;
    private Long policyId;
    private String policyNumber;
    private String triggerType;
    private LocalDate disruptionDate;
    private String city;
    private String zone;
    private BigDecimal estimatedLostHours;
    private BigDecimal estimatedLostIncome;
    private String validationStatus;
    private String claimStatus;
    private Boolean fraudFlag;
    private String fraudReason;
    private BigDecimal payoutAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
