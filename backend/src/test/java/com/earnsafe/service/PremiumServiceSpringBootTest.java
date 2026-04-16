package com.earnsafe.service;

import com.earnsafe.dto.response.PremiumCalculationResponse;
import com.earnsafe.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class PremiumServiceSpringBootTest {

    @Autowired
    private PremiumService premiumService;

    @MockBean
    private RiskService riskService;

    @Test
    void calculateUsesAiRiskPredictionForWeeklyPremium() {
        when(riskService.calculateRiskForUser(any(User.class)))
                .thenReturn(new AiInferenceService.RiskPrediction(82.0, new BigDecimal("79.00"), "HIGH"));

        User user = User.builder()
                .fullName("Worker")
                .email("worker1@earnsafe.com")
                .phone("9234567890")
                .city("Mumbai")
                .zone("Andheri")
                .role(User.Role.WORKER)
                .build();

        PremiumCalculationResponse response = premiumService.calculate(user);

        assertThat(response.getBasePremium()).isEqualByComparingTo("39.00");
        assertThat(response.getFinalWeeklyPremium()).isEqualByComparingTo("79.00");
        assertThat(response.getRiskScore()).isEqualTo("HIGH");
        assertThat(response.getRiskScoreNumeric()).isEqualTo(0.82);
    }
}
