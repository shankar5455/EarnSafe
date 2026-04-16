package com.earnsafe.repository;

import com.earnsafe.entity.RiskScore;
import com.earnsafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {
    List<RiskScore> findTop30ByOrderByCalculatedAtDesc();
    List<RiskScore> findByUserOrderByCalculatedAtDesc(User user);
}
