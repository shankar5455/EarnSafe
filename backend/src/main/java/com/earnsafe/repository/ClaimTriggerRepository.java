package com.earnsafe.repository;

import com.earnsafe.entity.ClaimTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimTriggerRepository extends JpaRepository<ClaimTrigger, Long> {
    List<ClaimTrigger> findTop50ByOrderByTriggeredAtDesc();
}
