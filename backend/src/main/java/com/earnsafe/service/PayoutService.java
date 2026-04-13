package com.earnsafe.service;

import com.earnsafe.entity.Claim;
import com.earnsafe.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Payout processing service.
 * Transitions an APPROVED claim to PAID and generates a unique transactionId.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private final ClaimRepository claimRepository;

    /**
     * Process a payout for an approved claim.
     * Sets claimStatus = PAID and generates a UUID-based transactionId.
     *
     * @param claim the claim to pay out (must be APPROVED)
     * @return the updated claim entity
     */
    public Claim processPayout(Claim claim) {
        if (claim.getClaimStatus() != Claim.ClaimStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED claims can be paid out. Current status: " + claim.getClaimStatus());
        }

        String txId = "TXN-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 12);
        claim.setClaimStatus(Claim.ClaimStatus.PAID);
        claim.setValidationStatus("PAID");
        claim.setTransactionId(txId);

        Claim saved = claimRepository.save(claim);
        log.info("[PayoutService] Claim {} paid out → transactionId={}, amount={}",
                saved.getClaimNumber(), txId, saved.getPayoutAmount());
        return saved;
    }
}
