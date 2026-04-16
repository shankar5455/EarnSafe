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
    private final AiInferenceService aiInferenceService;

    public FraudResult evaluate(User user, WeatherEvent event) {
        LocalDateTime since24h = LocalDateTime.now().minusHours(24);
        List<Claim> recentClaims = claimRepository.findByUserAndCreatedAtAfter(user, since24h);

        LocalDate eventDate = event.getEventTimestamp() != null
                ? event.getEventTimestamp().toLocalDate()
                : LocalDate.now();
        boolean duplicate = claimRepository.existsByUserAndDisruptionDateAndTriggerType(
                user, eventDate, event.getEventType());

        FraudResult result = aiInferenceService.predictFraud(user, event, recentClaims.size(), duplicate);
        log.info("[FraudService] aiFraudScore={} aiFraudFlag={} for user {}", result.fraudScore(), result.fraudFlag(), user.getEmail());
        return result;
    }

    public record FraudResult(double fraudScore, boolean fraudFlag, String reason) {}
}
