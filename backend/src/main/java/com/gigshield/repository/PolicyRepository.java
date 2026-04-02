package com.gigshield.repository;

import com.gigshield.entity.Policy;
import com.gigshield.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByUser(User user);
    List<Policy> findByUserOrderByCreatedAtDesc(User user);
    Optional<Policy> findByUserAndStatus(User user, Policy.PolicyStatus status);
    List<Policy> findByStatus(Policy.PolicyStatus status);
    long countByStatus(Policy.PolicyStatus status);

    @Query("SELECT p FROM Policy p WHERE p.user = :user AND p.status = 'ACTIVE'")
    Optional<Policy> findActiveByUser(User user);
}
