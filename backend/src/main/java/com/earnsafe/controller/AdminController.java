package com.earnsafe.controller;

import com.earnsafe.dto.response.AdminDashboardResponse;
import com.earnsafe.dto.response.ClaimResponse;
import com.earnsafe.dto.response.PolicyResponse;
import com.earnsafe.dto.response.UserResponse;
import com.earnsafe.entity.RiskZone;
import com.earnsafe.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/policies")
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {
        return ResponseEntity.ok(adminService.getAllPolicies());
    }

    @GetMapping("/claims")
    public ResponseEntity<List<ClaimResponse>> getAllClaims() {
        return ResponseEntity.ok(adminService.getAllClaims());
    }

    @GetMapping("/risk-zones")
    public ResponseEntity<List<RiskZone>> getRiskZones() {
        return ResponseEntity.ok(adminService.getAllRiskZones());
    }
}
