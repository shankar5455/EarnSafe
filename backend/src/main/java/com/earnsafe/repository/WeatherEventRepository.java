package com.earnsafe.repository;

import com.earnsafe.entity.WeatherEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherEventRepository extends JpaRepository<WeatherEvent, Long> {
    List<WeatherEvent> findTop10ByOrderByEventTimestampDesc();
    List<WeatherEvent> findByCityAndZone(String city, String zone);
    List<WeatherEvent> findByCity(String city);
}
