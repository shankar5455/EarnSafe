package com.gigshield.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "risk_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String zone;

    private Integer floodRiskScore;
    private Integer rainRiskScore;
    private Integer heatRiskScore;
    private Integer pollutionRiskScore;
    private Integer closureRiskScore;

    private String overallRiskLevel;
}
