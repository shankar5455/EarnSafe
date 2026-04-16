package com.earnsafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String zone;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(precision = 10, scale = 2)
    private BigDecimal suggestedPremium;

    private String riskLevel;

    private LocalDateTime calculatedAt;

    @PrePersist
    public void onCreate() {
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }
}
