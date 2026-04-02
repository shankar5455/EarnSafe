package com.gigshield.controller;

import com.gigshield.dto.response.PremiumCalculationResponse;
import com.gigshield.entity.User;
import com.gigshield.service.PremiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/premium")
@RequiredArgsConstructor
public class PremiumController {

    private final PremiumService premiumService;

    @PostMapping("/calculate")
    public ResponseEntity<PremiumCalculationResponse> calculate(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(premiumService.calculate(user));
    }

    @GetMapping("/explain/{userId}")
    public ResponseEntity<PremiumCalculationResponse> explain(@PathVariable Long userId) {
        return ResponseEntity.ok(premiumService.calculateForUser(userId));
    }
}
