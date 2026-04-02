package com.gigshield.repository;

import com.gigshield.entity.RiskZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskZoneRepository extends JpaRepository<RiskZone, Long> {
    Optional<RiskZone> findByCityAndZone(String city, String zone);
    List<RiskZone> findByCity(String city);
    List<RiskZone> findByOverallRiskLevel(String riskLevel);
}
