package com.gigshield.controller;

import com.gigshield.dto.response.UserResponse;
import com.gigshield.entity.User;
import com.gigshield.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(AuthService.mapToUserResponse(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal User user,
                                                       @RequestBody java.util.Map<String, Object> updates) {
        // Simple profile updates
        return ResponseEntity.ok(AuthService.mapToUserResponse(user));
    }
}
