package com.earnsafe.controller;


import com.earnsafe.dto.request.PolicyCreateRequest;
import com.earnsafe.dto.response.PolicyResponse;
import com.earnsafe.entity.User;
import com.earnsafe.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping("/create")
    public ResponseEntity<PolicyResponse> createPolicy(@AuthenticationPrincipal User user,
                                                        @RequestBody PolicyCreateRequest request) {
        return ResponseEntity.ok(policyService.createPolicy(user, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PolicyResponse>> getMyPolicies(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(policyService.getMyPolicies(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable Long id,
                                                         @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(policyService.getPolicyById(id, user));
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<PolicyResponse> renewPolicy(@PathVariable Long id,
                                                       @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(policyService.renewPolicy(id, user));
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<PolicyResponse> pausePolicy(@PathVariable Long id,
                                                       @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(policyService.pausePolicy(id, user));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<PolicyResponse> deactivatePolicy(@PathVariable Long id,
                                                            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(policyService.deactivatePolicy(id, user));
    }
}
