package com.digitaltwin.backend.controller;

import com.digitaltwin.backend.model.User;
import com.digitaltwin.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = (List<User>) userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (!optionalUser.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            User user = optionalUser.get();

            // Update allowed fields
            if (userDetails.getFirstName() != null) {
                user.setFirstName(userDetails.getFirstName());
            }
            if (userDetails.getLastName() != null) {
                user.setLastName(userDetails.getLastName());
            }
            if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
                // Check if email is already taken
                if (userRepository.existsByEmail(userDetails.getEmail())) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Email is already in use");
                    return ResponseEntity.badRequest().body(error);
                }
                user.setEmail(userDetails.getEmail());
            }

            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error updating user: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("@userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> changePassword(@PathVariable Long id,
                                           @RequestBody Map<String, String> passwordData) {
        try {
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Current password and new password are required");
                return ResponseEntity.badRequest().body(error);
            }

            Optional<User> optionalUser = userRepository.findById(id);
            if (!optionalUser.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            User user = optionalUser.get();

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Current password is incorrect");
                return ResponseEntity.badRequest().body(error);
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error changing password: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (!user.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            userRepository.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error deleting user: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id,
                                             @RequestBody Map<String, Boolean> statusData) {
        try {
            Boolean enabled = statusData.get("enabled");
            if (enabled == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Enabled status is required");
                return ResponseEntity.badRequest().body(error);
            }

            Optional<User> optionalUser = userRepository.findById(id);
            if (!optionalUser.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            User user = optionalUser.get();
            user.setEnabled(enabled);
            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "User status updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error updating user status: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}