package com.gigshield.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    private String triggerType;

    private LocalDate disruptionDate;

    private String city;
    private String zone;

    @Column(precision = 5, scale = 2)
    private BigDecimal estimatedLostHours;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedLostIncome;

    private String validationStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus claimStatus;

    @Builder.Default
    @Column(nullable = false)
    private Boolean fraudFlag = false;

    private String fraudReason;

    @Column(precision = 10, scale = 2)
    private BigDecimal payoutAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ClaimStatus {
        TRIGGERED, UNDER_VALIDATION, APPROVED, REJECTED, PAID
    }
}
