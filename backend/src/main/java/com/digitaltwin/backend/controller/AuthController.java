package com.digitaltwin.backend.controller;

import com.digitaltwin.backend.annotation.RateLimited;
import com.digitaltwin.backend.dto.AuthResponse;
import com.digitaltwin.backend.dto.LoginRequest;
import com.digitaltwin.backend.dto.RegisterRequest;
import com.digitaltwin.backend.model.User;
import com.digitaltwin.backend.repository.UserRepository;
import com.digitaltwin.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    @RateLimited(value = 5, timeWindow = 300, keyStrategy = RateLimited.KeyStrategy.IP) // 5 requests per 5 minutes per IP
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid username/email or password");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/register")
    @RateLimited(value = 3, timeWindow = 3600, keyStrategy = RateLimited.KeyStrategy.IP) // 3 registrations per hour per IP
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse authResponse = authService.registerUser(registerRequest);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Refresh token is required");
                return ResponseEntity.badRequest().body(error);
            }

            String refreshToken = authHeader.substring(7);
            AuthResponse authResponse = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid refresh token");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userRepository.findByUsername(username).orElseThrow();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("firstName", user.getFirstName());
            userInfo.put("lastName", user.getLastName());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("roles", user.getRoles());
            userInfo.put("enabled", user.isEnabled());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("updatedAt", user.getUpdatedAt());

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unable to retrieve user information");
            return ResponseEntity.badRequest().body(error);
        }
    }
}