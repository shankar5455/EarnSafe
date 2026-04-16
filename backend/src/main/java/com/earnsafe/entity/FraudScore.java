package com.earnsafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @Column(precision = 5, scale = 4)
    private BigDecimal score;

    private Boolean fraudFlag;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private LocalDateTime evaluatedAt;

    @PrePersist
    public void onCreate() {
        if (evaluatedAt == null) {
            evaluatedAt = LocalDateTime.now();
        }
    }
}
