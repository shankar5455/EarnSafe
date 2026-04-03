package com.earnsafe.repository;

import com.earnsafe.entity.Claim;
import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByUserOrderByCreatedAtDesc(User user);
    List<Claim> findByPolicy(Policy policy);
    long countByClaimStatus(Claim.ClaimStatus status);
    boolean existsByUserAndDisruptionDateAndTriggerType(User user, LocalDate date, String triggerType);

    @Query("SELECT c.triggerType, COUNT(c) FROM Claim c GROUP BY c.triggerType")
    List<Object[]> countByTriggerType();

    @Query("SELECT c.claimStatus, COUNT(c) FROM Claim c GROUP BY c.claimStatus")
    List<Object[]> countByStatus();
}
