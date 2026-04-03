package com.earnsafe.service;

import com.earnsafe.dto.request.LoginRequest;
import com.earnsafe.dto.request.RegisterRequest;
import com.earnsafe.dto.response.AuthResponse;
import com.earnsafe.dto.response.UserResponse;
import com.earnsafe.entity.User;
import com.earnsafe.repository.UserRepository;
import com.earnsafe.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .deliveryPlatform(request.getDeliveryPlatform())
                .deliveryCategory(request.getDeliveryCategory())
                .city(request.getCity())
                .zone(request.getZone())
                .pincode(request.getPincode())
                .preferredShift(request.getPreferredShift())
                .averageDailyEarnings(request.getAverageDailyEarnings())
                .averageWorkingHours(request.getAverageWorkingHours())
                .vehicleType(request.getVehicleType())
                .role(User.Role.WORKER)
                .build();

        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    public UserResponse getMe(User user) {
        return mapToUserResponse(user);
    }

    public static UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .deliveryPlatform(user.getDeliveryPlatform())
                .deliveryCategory(user.getDeliveryCategory())
                .city(user.getCity())
                .zone(user.getZone())
                .pincode(user.getPincode())
                .preferredShift(user.getPreferredShift())
                .averageDailyEarnings(user.getAverageDailyEarnings())
                .averageWorkingHours(user.getAverageWorkingHours())
                .vehicleType(user.getVehicleType())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
