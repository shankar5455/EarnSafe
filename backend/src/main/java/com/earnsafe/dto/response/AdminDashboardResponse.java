package com.earnsafe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Map<String, Long> triggerCountByType;
    private Map<String, Long> claimsByStatus;
    private List<RiskZoneInfo> topRiskyZones;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskZoneInfo {
        private String city;
        private String zone;
        private String riskLevel;
    }
}
