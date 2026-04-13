package com.earnsafe.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.earnsafe.entity.Policy;
import com.earnsafe.entity.User;

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

<<<<<<< HEAD
    /**
     * FIX for LazyInitializationException:
     * Fetch policy + user together so scheduler can safely access user.city and user.zone
     */
    @Query("SELECT p FROM Policy p JOIN FETCH p.user WHERE p.status = 'ACTIVE'")
    List<Policy> findActivePoliciesWithUser();
}
=======
    @Query("SELECT p FROM Policy p JOIN FETCH p.user WHERE p.status = 'ACTIVE'")
    List<Policy> findActivePoliciesWithUser();
}
>>>>>>> 84d46b715f50900d5fcf9292b90b3f41007422be
