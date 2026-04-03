package com.earnsafe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String deliveryPlatform;
    private String deliveryCategory;
    private String city;
    private String zone;
    private String pincode;
    private String preferredShift;
    private BigDecimal averageDailyEarnings;
    private BigDecimal averageWorkingHours;
    private String vehicleType;
    private String role;
    private LocalDateTime createdAt;
}
