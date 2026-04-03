package com.earnsafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "delivery_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate date;

    @Column(precision = 5, scale = 2)
    private BigDecimal loginHours;

    private Integer completedDeliveries;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedDailyIncome;

    private Boolean activeStatus;
    private String sourceType;
}
