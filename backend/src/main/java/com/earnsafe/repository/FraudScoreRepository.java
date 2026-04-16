package com.earnsafe.repository;

import com.earnsafe.entity.FraudScore;
import com.earnsafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudScoreRepository extends JpaRepository<FraudScore, Long> {
    List<FraudScore> findTop30ByOrderByEvaluatedAtDesc();
    List<FraudScore> findByUserOrderByEvaluatedAtDesc(User user);
}
