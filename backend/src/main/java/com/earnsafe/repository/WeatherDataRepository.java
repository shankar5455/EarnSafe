package com.earnsafe.repository;

import com.earnsafe.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    List<WeatherData> findTop20ByOrderByObservedAtDesc();
    List<WeatherData> findByCityOrderByObservedAtDesc(String city);
}
