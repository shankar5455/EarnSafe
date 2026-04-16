package com.earnsafe.repository;

import com.earnsafe.entity.DeliveryActivity;
import com.earnsafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryActivityRepository extends JpaRepository<DeliveryActivity, Long> {
    List<DeliveryActivity> findByUser(User user);
    Optional<DeliveryActivity> findByUserAndDate(User user, LocalDate date);
    List<DeliveryActivity> findByUserOrderByDateDesc(User user);
    Optional<DeliveryActivity> findTopByUserOrderByDateDesc(User user);
}
