package com.gigshield.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PolicyCreateRequest {
    private String planName;
    private BigDecimal weeklyPremium;
    private BigDecimal weeklyCoverageAmount;
    private Integer coveredHours;
    private String coveredDisruptions;
    private String zoneCovered;
}
