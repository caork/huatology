package com.digitaltwin.backend.service;

import com.digitaltwin.backend.dto.AuthResponse;
import com.digitaltwin.backend.dto.LoginRequest;
import com.digitaltwin.backend.dto.RegisterRequest;
import com.digitaltwin.backend.model.User;
import com.digitaltwin.backend.repository.UserRepository;
import com.digitaltwin.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuditService auditService;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());
            String refreshToken = jwtUtil.generateRefreshToken((UserDetails) authentication.getPrincipal());

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

            // Audit successful login
            auditService.logSuccessfulAction("LOGIN", "USER", user.getUsername());

            return new AuthResponse(
                    jwt,
                    refreshToken,
                    jwtUtil.getExpirationTime(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRoles()
            );
        } catch (Exception e) {
            // Audit failed login attempt
            auditService.logFailedAction("LOGIN", "USER", "Failed login attempt for: " + loginRequest.getUsernameOrEmail());
            throw e;
        }
    }

    public AuthResponse registerUser(RegisterRequest registerRequest) {
        try {
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                auditService.logFailedAction("REGISTER", "USER", "Username already taken: " + registerRequest.getUsername());
                throw new RuntimeException("Username is already taken!");
            }

            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                auditService.logFailedAction("REGISTER", "USER", "Email already in use: " + registerRequest.getEmail());
                throw new RuntimeException("Email is already in use!");
            }

            // Create new user account
            User user = new User(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    passwordEncoder.encode(registerRequest.getPassword())
            );

            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setRoles(Set.of("USER"));

            userRepository.save(user);

            // Generate tokens
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String jwt = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Audit successful registration
            auditService.logSuccessfulAction("REGISTER", "USER", user.getUsername());

            return new AuthResponse(
                    jwt,
                    refreshToken,
                    jwtUtil.getExpirationTime(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRoles()
            );
        } catch (RuntimeException e) {
            // Re-throw validation errors
            throw e;
        } catch (Exception e) {
            auditService.logError("REGISTER", "USER", "Registration failed: " + e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtUtil.generateToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        User user = userRepository.findByUsername(username).orElseThrow();

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                jwtUtil.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles()
        );
    }
}