package com.earnsafe.service;

import com.earnsafe.entity.Claim;
import com.earnsafe.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private final ClaimRepository claimRepository;
    private final StripePayoutGateway stripePayoutGateway;

    public Claim processPayout(Claim claim) {
        if (claim.getClaimStatus() != Claim.ClaimStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED claims can be paid out. Current status: " + claim.getClaimStatus());
        }

        String txId = stripePayoutGateway.triggerPayout(claim);
        claim.setClaimStatus(Claim.ClaimStatus.PAID);
        claim.setValidationStatus("PAID");
        claim.setTransactionId(txId);

        Claim saved = claimRepository.save(claim);
        log.info("[PayoutService] Claim {} paid out via Stripe transferId={} amount={}",
                saved.getClaimNumber(), txId, saved.getPayoutAmount());
        return saved;
    }
}
