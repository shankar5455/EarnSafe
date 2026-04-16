package com.earnsafe.service;

import com.earnsafe.entity.Claim;
import com.earnsafe.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private static final String SIMULATED_TX_PREFIX = "SIM-";
    private static final int SIMULATED_TX_ID_LENGTH = 12;

    private final ClaimRepository claimRepository;
    private final StripePayoutGateway stripePayoutGateway;

    @Value("${app.payout.retry.max-attempts:3}")
    private int maxRetryAttempts;

    public Claim processPayout(Claim claim) {
        if (claim.getClaimStatus() != Claim.ClaimStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED claims can be paid out. Current status: " + claim.getClaimStatus());
        }
        return attemptPayoutWithFallback(claim, false);
    }

    public int retryPendingPayouts() {
        List<Claim> retryableClaims = claimRepository
                .findTop50ByPayoutRetryPendingTrueAndPayoutRetryCountLessThanOrderByUpdatedAtAsc(maxRetryAttempts);

        int successCount = 0;
        for (Claim claim : retryableClaims) {
            Claim processed = attemptPayoutWithFallback(claim, true);
            if (processed.getPayoutStatus() == Claim.PayoutStatus.STRIPE_SUCCESS && Boolean.FALSE.equals(processed.getPayoutRetryPending())) {
                successCount++;
            }
        }
        return successCount;
    }

    private Claim attemptPayoutWithFallback(Claim claim, boolean retryAttempt) {
        try {
            String txId = stripePayoutGateway.triggerPayout(claim);
            claim.setClaimStatus(Claim.ClaimStatus.PAID);
            claim.setValidationStatus(retryAttempt ? "PAID_RECONCILED_STRIPE" : "PAID");
            claim.setTransactionId(txId);
            claim.setPayoutStatus(Claim.PayoutStatus.STRIPE_SUCCESS);
            claim.setPayoutRetryPending(false);
            claim.setPayoutFailureReason(null);
            claim.setPayoutLastAttemptAt(LocalDateTime.now());
            Claim saved = claimRepository.save(claim);
            log.info("[PayoutService] Claim {} paid out via Stripe transferId={} amount={}",
                    saved.getClaimNumber(), txId, saved.getPayoutAmount());
            return saved;
        } catch (Exception ex) {
            int nextRetryCount = claim.getPayoutRetryCount() == null ? 1 : claim.getPayoutRetryCount() + 1;
            claim.setClaimStatus(Claim.ClaimStatus.PAID);
            claim.setValidationStatus("PAID_SIMULATED_FALLBACK");
            claim.setPayoutStatus(Claim.PayoutStatus.SIMULATED_SUCCESS);
            claim.setPayoutRetryPending(nextRetryCount < maxRetryAttempts);
            claim.setPayoutRetryCount(nextRetryCount);
            claim.setPayoutLastAttemptAt(LocalDateTime.now());
            claim.setPayoutFailureReason(ex.getMessage());
            String existingTxId = claim.getTransactionId();
            boolean hasSimulatedTxId = existingTxId != null && !existingTxId.isBlank() && existingTxId.startsWith(SIMULATED_TX_PREFIX);
            if (!hasSimulatedTxId) {
                claim.setTransactionId(SIMULATED_TX_PREFIX + UUID.randomUUID().toString().substring(0, SIMULATED_TX_ID_LENGTH).toUpperCase());
            }
            Claim saved = claimRepository.save(claim);
            log.warn("[PayoutService] Stripe payout failed for claim {}. Fallback simulated success applied. retryPending={} retries={} reason={}",
                    saved.getClaimNumber(), saved.getPayoutRetryPending(), saved.getPayoutRetryCount(), ex.getMessage());
            return saved;
        }
    }
}
