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
public class PolicyResponse {
    private Long id;
    private String policyNumber;
    private Long userId;
    private String userFullName;
    private String planName;
    private BigDecimal weeklyPremium;
    private BigDecimal weeklyCoverageAmount;
    private Integer coveredHours;
    private String coveredDisruptions;
    private String zoneCovered;
    private String status;
    private String riskScore;
    /** Numeric risk score in [0.0, 1.0] from RiskService */
    private Double riskScoreNumeric;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
}
