package com.earnsafe.controller;

import com.earnsafe.dto.request.WeatherScanRequest;
import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.entity.WeatherEvent;
import com.earnsafe.service.TriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/scan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> scanAndEvaluate(@RequestBody WeatherScanRequest request) {
        List<ClaimResponse> claims = triggerService.scanAndEvaluateCity(request.getCity());
        return ResponseEntity.ok(Map.of(
                "city", request.getCity(),
                "triggeredClaims", claims.size(),
                "claims", claims
        ));
    }

    @PostMapping("/scan-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> scanAll() {
        List<ClaimResponse> claims = triggerService.scanAndEvaluateAllCities();
        return ResponseEntity.ok(Map.of(
                "message", "OpenWeather scan completed for all active-policy cities",
                "triggeredClaims", claims.size(),
                "claims", claims
        ));
    }
}
