package com.earnsafe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalWorkers;
    private long activePolicies;
    private long totalClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private long paidClaims;
    private long pendingClaims;
    private long fraudDetectedCount;
    private BigDecimal totalPayouts;
    private Map<String, Long> triggerCountByType;
    private Map<String, Long> claimsByStatus;
    private List<RiskZoneInfo> topRiskyZones;
    private List<FraudAlertInfo> fraudAlerts;
    private List<RiskHeatPoint> riskHeatmap;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskZoneInfo {
        private String city;
        private String zone;
        private String riskLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FraudAlertInfo {
        private String userName;
        private String reason;
        private Double score;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskHeatPoint {
        private String city;
        private String zone;
        private Double score;
    }
}
