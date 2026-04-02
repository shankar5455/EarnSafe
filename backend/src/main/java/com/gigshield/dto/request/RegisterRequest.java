package com.gigshield.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Delivery platform is required")
    private String deliveryPlatform;

    @NotBlank(message = "Delivery category is required")
    private String deliveryCategory;

    @NotBlank(message = "City is required")
    private String city;

    private String zone;
    private String pincode;

    @NotBlank(message = "Preferred shift is required")
    private String preferredShift;

    @NotNull(message = "Average daily earnings is required")
    @DecimalMin(value = "0.0", message = "Earnings cannot be negative")
    private BigDecimal averageDailyEarnings;

    @NotNull(message = "Average working hours is required")
    @DecimalMin(value = "0.0", message = "Hours cannot be negative")
    private BigDecimal averageWorkingHours;

    private String vehicleType;
}
