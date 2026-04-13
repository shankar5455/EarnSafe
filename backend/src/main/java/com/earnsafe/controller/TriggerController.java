package com.earnsafe.controller;


import com.earnsafe.service.TriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.earnsafe.dto.request.MockEventRequest;
import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.entity.User;
import com.earnsafe.entity.WeatherEvent;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/triggers")
@RequiredArgsConstructor
public class TriggerController {

    private final TriggerService triggerService;

    @GetMapping("/live")
    public ResponseEntity<List<WeatherEvent>> getLive() {
        return ResponseEntity.ok(triggerService.getLiveEvents());
    }

    @PostMapping("/mock-event")
    public ResponseEntity<WeatherEvent> createMockEvent(@RequestBody MockEventRequest request) {
        return ResponseEntity.ok(triggerService.createMockEvent(request));
    }

    @PostMapping("/evaluate/{userId}")
    public ResponseEntity<List<ClaimResponse>> evaluateForUser(@PathVariable Long userId,
                                                               @RequestBody MockEventRequest request) {
        WeatherEvent event = triggerService.createMockEvent(request);
        return ResponseEntity.ok(triggerService.evaluateForUser(userId, event));
    }

    @PostMapping("/evaluate-all")
    public ResponseEntity<Map<String, Object>> evaluateAll(@RequestBody MockEventRequest request) {
        List<ClaimResponse> claims = triggerService.processMockEventAndEvaluateAll(request);
        return ResponseEntity.ok(Map.of(
                "triggeredClaims", claims.size(),
                "claims", claims,
                "eventType", request.getEventType(),
                "city", request.getCity()
        ));
    }

    @PostMapping("/simulate-feed")
    public ResponseEntity<Map<String, Object>> simulateRealtimeFeed() {
        List<ClaimResponse> claims = triggerService.simulateRealtimeTriggerFeed();
        return ResponseEntity.ok(Map.of(
                "message", "Realtime trigger feed simulated for all active-policy cities",
                "triggeredClaims", claims.size(),
                "claims", claims
        ));
    }
}
