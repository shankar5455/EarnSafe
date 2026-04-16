package com.earnsafe.service;

import com.earnsafe.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiInferenceService {

    private static final double DEFAULT_WEEKLY_PREMIUM = 79.0;

    @Value("${app.ai.base-url:http://localhost:8000}")
    private String aiBaseUrl;

    @Value("${app.ai.timeout-ms:8000}")
    private long timeoutMs;

    private final RestTemplateBuilder restTemplateBuilder;

    public RiskPrediction predictRisk(User user, double weatherSeverity, int pastClaims, double locationRisk, double workerActivity) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("locationRisk", locationRisk);
        payload.put("weatherSeverity", weatherSeverity);
        payload.put("pastClaims", pastClaims);
        payload.put("workerActivity", workerActivity);
        payload.put("city", user.getCity());
        payload.put("zone", user.getZone());

        Map<String, Object> response = post("/predict-risk", payload);

        double riskScore = clamp(toDouble(response.get("riskScore"), 50.0), 0.0, 100.0);
        BigDecimal premium = BigDecimal.valueOf(toDouble(response.get("premium"), DEFAULT_WEEKLY_PREMIUM)).setScale(2, RoundingMode.HALF_UP);
        String riskLevel = response.get("riskLevel") != null ? response.get("riskLevel").toString() : toRiskLevel(riskScore);

        return new RiskPrediction(riskScore, premium, riskLevel);
    }

    public FraudService.FraudResult predictFraud(
            User user,
            double weatherSeverity,
            int claimFrequency,
            int pastClaims,
            double workerActivity,
            double gpsDistanceKm,
            boolean inactivityMismatch,
            double suspiciousVelocity
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("locationRisk", inferLocationRisk(user));
        payload.put("weatherSeverity", weatherSeverity);
        payload.put("claimFrequency", claimFrequency);
        payload.put("pastClaims", pastClaims);
        payload.put("workerActivity", workerActivity);
        payload.put("gpsDistanceKm", gpsDistanceKm);
        payload.put("inactivityMismatch", inactivityMismatch ? 1 : 0);
        payload.put("suspiciousVelocity", suspiciousVelocity);
        payload.put("city", user.getCity());
        payload.put("zone", user.getZone());

        Map<String, Object> response = post("/detect-fraud", payload);
        double fraudScore = clamp(toDouble(response.get("fraudScore"), 0.0), 0.0, 1.0);
        boolean fraudFlag = toBoolean(response.get("fraudFlag"), fraudScore >= 0.55);
        String reason = response.get("reason") != null ? response.get("reason").toString() : "Model-evaluated fraud score";

        return new FraudService.FraudResult(fraudScore, fraudFlag, reason);
    }

    private double inferLocationRisk(User user) {
        if (user.getZone() == null) return 5.0;
        String zone = user.getZone().toLowerCase();
        if (zone.contains("industrial") || zone.contains("coastal") || zone.contains("old city")) return 8.0;
        if (zone.contains("central") || zone.contains("market")) return 6.0;
        return 4.5;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, Map<String, Object> payload) {
        RestTemplate client = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();

        String normalizedBase = aiBaseUrl.endsWith("/")
                ? aiBaseUrl.substring(0, aiBaseUrl.length() - 1)
                : aiBaseUrl;

        ResponseEntity<Map> response = client.postForEntity(normalizedBase + path, payload, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("AI service call failed for " + path);
        }
        return response.getBody();
    }

    private double toDouble(Object value, double fallback) {
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private boolean toBoolean(Object value, boolean fallback) {
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return Boolean.parseBoolean(s);
        return fallback;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String toRiskLevel(double riskScore) {
        if (riskScore < 33.0) return "LOW";
        if (riskScore < 66.0) return "MEDIUM";
        return "HIGH";
    }

    public record RiskPrediction(double riskScore, BigDecimal premium, String riskLevel) {}
}
