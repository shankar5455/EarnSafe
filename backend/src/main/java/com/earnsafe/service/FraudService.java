package com.earnsafe.service;

import com.earnsafe.entity.Claim;
import com.earnsafe.entity.User;
import com.earnsafe.entity.WeatherEvent;
import com.earnsafe.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI-like fraud detection service.
 * Evaluates three independent signals and returns a composite fraudScore in [0.0, 1.0].
 *
 *  Signal 1 – Multiple claims in 24 hours  (weight 0.40)
 *  Signal 2 – Duplicate claim detection    (weight 0.30)
 *  Signal 3 – Location / zone mismatch    (weight 0.30)
 *
 * fraudScore >= 0.5 → fraudFlag = true → claim goes to UNDER_REVIEW
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudService {

    private final ClaimRepository claimRepository;

    public FraudResult evaluate(User user, WeatherEvent event) {
        double score = 0.0;
        StringBuilder reasons = new StringBuilder();

        // Signal 1: multiple claims in the last 24 hours
        LocalDateTime since24h = LocalDateTime.now().minusHours(24);
        List<Claim> recentClaims = claimRepository.findByUserAndCreatedAtAfter(user, since24h);
        if (!recentClaims.isEmpty()) {
            score += 0.40;
            reasons.append("Multiple claims submitted within 24 hours. ");
            log.info("[FraudService] Signal 1 triggered for user {} – {} claims in last 24h",
                    user.getEmail(), recentClaims.size());
        }

        // Signal 2: duplicate claim (same event type on the same date)
        LocalDate eventDate = event.getEventTimestamp() != null
                ? event.getEventTimestamp().toLocalDate()
                : LocalDate.now();
        boolean duplicate = claimRepository.existsByUserAndDisruptionDateAndTriggerType(
                user, eventDate, event.getEventType());
        if (duplicate) {
            score += 0.30;
            reasons.append("Duplicate claim for the same event and date. ");
            log.info("[FraudService] Signal 2 triggered for user {} – duplicate claim", user.getEmail());
        }

        // Signal 3: location / zone mismatch
        String userCity = user.getCity() != null ? user.getCity().trim().toLowerCase() : "";
        String eventCity = event.getCity() != null ? event.getCity().trim().toLowerCase() : "";
        String userZone = user.getZone() != null ? user.getZone().trim().toLowerCase() : userCity;
        String eventZone = event.getZone() != null ? event.getZone().trim().toLowerCase() : eventCity;

        boolean cityMismatch = !userCity.isEmpty() && !eventCity.isEmpty() && !userCity.equals(eventCity);
        boolean zoneMismatch = !userZone.isEmpty() && !eventZone.isEmpty() && !userZone.equals(eventZone);

        if (cityMismatch || zoneMismatch) {
            score += 0.30;
            reasons.append("Location mismatch between policy zone and event zone. ");
            log.info("[FraudService] Signal 3 triggered for user {} – zone mismatch", user.getEmail());
        }

        double fraudScore = Math.min(1.0, Math.round(score * 100.0) / 100.0);
        boolean fraudFlag = fraudScore >= 0.5;
        String reason = reasons.length() > 0 ? reasons.toString().trim() : null;

        log.info("[FraudService] fraudScore={} fraudFlag={} for user {}", fraudScore, fraudFlag, user.getEmail());
        return new FraudResult(fraudScore, fraudFlag, reason);
    }

    public record FraudResult(double fraudScore, boolean fraudFlag, String reason) {}
}
