package com.earnsafe.controller;


import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.entity.User;
import com.earnsafe.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @GetMapping("/my")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(claimService.getMyClaims(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long id,
                                                       @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(claimService.getClaimById(id, user));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> approveClaim(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.approveClaim(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> rejectClaim(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.rejectClaim(id));
    }

    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.markPaid(id));
    }
}
