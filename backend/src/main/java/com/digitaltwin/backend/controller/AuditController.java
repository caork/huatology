package com.digitaltwin.backend.controller;

import com.digitaltwin.backend.model.AuditLog;
import com.digitaltwin.backend.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentLogs(@RequestParam(defaultValue = "50") int limit) {
        List<AuditLog> logs = auditService.getRecentLogs(limit);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<AuditLog>> getLogsByUsername(@PathVariable String username) {
        List<AuditLog> logs = auditService.getLogsByUsername(username);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getLogsByAction(@PathVariable String action) {
        List<AuditLog> logs = auditService.getLogsByAction(action);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/failed")
    public ResponseEntity<List<AuditLog>> getFailedActions() {
        List<AuditLog> logs = auditService.getFailedActions();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> logs = auditService.getLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAuditStats() {
        // This could be expanded to provide more detailed statistics
        Map<String, Object> stats = Map.of(
            "totalLogs", auditService.getRecentLogs(1000).size(),
            "failedActions", auditService.getFailedActions().size(),
            "recentLogins", auditService.getLogsByAction("LOGIN").stream()
                .filter(log -> log.getTimestamp().isAfter(LocalDateTime.now().minusDays(1)))
                .count()
        );
        return ResponseEntity.ok(stats);
    }
}