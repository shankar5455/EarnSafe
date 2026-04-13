package com.earnsafe.repository;

import com.earnsafe.entity.Claim;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByUserOrderByCreatedAtDesc(User user);
    List<Claim> findByPolicy(Policy policy);
    long countByClaimStatus(Claim.ClaimStatus status);
    boolean existsByUserAndDisruptionDateAndTriggerType(User user, LocalDate date, String triggerType);

    /** Claims submitted by a user after a specific timestamp – used by FraudService. */
    List<Claim> findByUserAndCreatedAtAfter(User user, LocalDateTime since);

    /** Count of claims created after a timestamp – used by RiskService. */
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.user = :user AND c.createdAt >= :since")
    int countByUserAndCreatedAfter(User user, LocalDateTime since);

    /** Count of fraud-flagged claims. */
    long countByFraudFlagTrue();

    /** Sum of all payouts for PAID claims. */
    @Query("SELECT COALESCE(SUM(c.payoutAmount), 0) FROM Claim c WHERE c.claimStatus = 'PAID'")
    BigDecimal sumPayoutAmountForPaidClaims();

    @Query("SELECT c.triggerType, COUNT(c) FROM Claim c GROUP BY c.triggerType")
    List<Object[]> countByTriggerType();

    @Query("SELECT c.claimStatus, COUNT(c) FROM Claim c GROUP BY c.claimStatus")
    List<Object[]> countByStatus();
}
