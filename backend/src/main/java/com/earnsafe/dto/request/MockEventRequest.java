package com.earnsafe.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MockEventRequest {
    private String city;
    private String zone;
    private String eventType;
    private BigDecimal temperature;
    private BigDecimal rainfallMm;
    private Integer aqi;
    private Boolean floodAlert;
    private Boolean closureAlert;
}
