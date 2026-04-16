package com.earnsafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    private String zone;

    private Double latitude;
    private Double longitude;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 10, scale = 2)
    private BigDecimal rainfallMm;

    private Integer aqi;

    @Column(precision = 5, scale = 2)
    private BigDecimal windSpeed;

    private String weatherCondition;

    @Column(columnDefinition = "TEXT")
    private String alerts;

    @Column(precision = 5, scale = 2)
    private BigDecimal severityScore;

    private LocalDateTime observedAt;

    @PrePersist
    public void onCreate() {
        if (observedAt == null) {
            observedAt = LocalDateTime.now();
        }
    }
}
