package com.earnsafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String policyNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String planName;

    @Column(precision = 10, scale = 2)
    private BigDecimal weeklyPremium;

    @Column(precision = 10, scale = 2)
    private BigDecimal weeklyCoverageAmount;

    private Integer coveredHours;

    @Column(columnDefinition = "TEXT")
    private String coveredDisruptions;

    private String zoneCovered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    private String riskScore;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum PolicyStatus {
        ACTIVE, INACTIVE, PAUSED, EXPIRED
    }
}
