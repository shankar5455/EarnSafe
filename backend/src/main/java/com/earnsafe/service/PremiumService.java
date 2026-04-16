package com.earnsafe.service;

import com.earnsafe.dto.response.PremiumCalculationResponse;
import com.earnsafe.entity.User;
import com.earnsafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PremiumService {

    private final UserRepository userRepository;
    private final AiInferenceService aiInferenceService;

    public PremiumCalculationResponse calculate(User user) {
        return aiInferenceService.predictPremium(user);
    }

    public PremiumCalculationResponse calculateForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return calculate(user);
    }
}
