package com.earnsafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String zone;
    private String eventType;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 10, scale = 2)
    private BigDecimal rainfallMm;

    private Integer aqi;
    private Boolean floodAlert;
    private Boolean closureAlert;

    private LocalDateTime eventTimestamp;

    private String sourceType;
}
