package com.earnsafe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumCalculationResponse {
    private BigDecimal finalWeeklyPremium;
    private String riskScore;
    private List<BreakdownItem> breakdown;
    private String explanation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreakdownItem {
        private String factor;
        private BigDecimal amount;
    }
}
