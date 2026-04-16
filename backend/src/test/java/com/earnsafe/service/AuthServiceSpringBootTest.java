package com.earnsafe.service;

import com.earnsafe.dto.request.RegisterRequest;
import com.earnsafe.dto.response.AuthResponse;
import com.earnsafe.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthServiceSpringBootTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerCreatesWorkerAndReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test Worker");
        request.setPhone("9123456789");
        request.setEmail("test.worker@earnsafe.com");
        request.setPassword("Worker@123");
        request.setDeliveryPlatform("Swiggy");
        request.setDeliveryCategory("Food");
        request.setCity("Mumbai");
        request.setZone("Andheri");
        request.setPincode("400053");
        request.setPreferredShift("DAY");
        request.setAverageDailyEarnings(new BigDecimal("900"));
        request.setAverageWorkingHours(new BigDecimal("8"));
        request.setVehicleType("BIKE");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getRole()).isEqualTo("WORKER");
        assertThat(userRepository.existsByEmail(request.getEmail())).isTrue();
    }
}
