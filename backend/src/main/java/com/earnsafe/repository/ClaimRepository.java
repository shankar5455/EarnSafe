package com.earnsafe.repository;

import com.earnsafe.entity.Claim;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByUserOrderByCreatedAtDesc(User user);

    List<Claim> findByPolicy(Policy policy);

    long countByUser(User user);

    long countByClaimStatus(Claim.ClaimStatus status);

    boolean existsByUserAndDisruptionDateAndTriggerType(User user, LocalDate date, String triggerType);

    // FraudService
    List<Claim> findByUserAndCreatedAtAfter(User user, LocalDateTime since);

    // RiskService
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.user = :user AND c.createdAt >= :since")
    int countByUserAndCreatedAfter(@Param("user") User user, @Param("since") LocalDateTime since);

    // Fraud count
    long countByFraudFlagTrue();

    // ✅ FIXED QUERY (IMPORTANT)
    @Query("SELECT COALESCE(SUM(c.payoutAmount), 0) FROM Claim c WHERE c.claimStatus = :status")
    BigDecimal sumPayoutAmountForPaidClaims(@Param("status") Claim.ClaimStatus status);

    // Analytics
    @Query("SELECT c.triggerType, COUNT(c) FROM Claim c GROUP BY c.triggerType")
    List<Object[]> countByTriggerType();

    @Query("SELECT c.claimStatus, COUNT(c) FROM Claim c GROUP BY c.claimStatus")
    List<Object[]> countByStatus();

    List<Claim> findTop50ByPayoutRetryPendingTrueAndPayoutRetryCountLessThanOrderByUpdatedAtAsc(int maxRetryCount);
}
