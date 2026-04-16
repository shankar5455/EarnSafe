package com.earnsafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "claim_triggers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String city;
    private String zone;
    private String eventType;

    private Boolean weatherSevere;
    private Boolean workerInactive;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private Boolean claimCreated;

    private LocalDateTime triggeredAt;

    @PrePersist
    public void onCreate() {
        if (triggeredAt == null) {
            triggeredAt = LocalDateTime.now();
        }
    }
}
