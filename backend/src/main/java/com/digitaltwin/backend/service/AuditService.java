package com.digitaltwin.backend.service;

import com.digitaltwin.backend.model.AuditLog;
import com.digitaltwin.backend.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private HttpServletRequest request;

    @Async
    public void logAction(String action, String resource, String resourceId, String status, String details) {
        try {
            AuditLog auditLog = new AuditLog();

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                auditLog.setUsername(authentication.getName());
            } else {
                auditLog.setUsername("anonymous");
            }

            auditLog.setAction(action);
            auditLog.setResource(resource);
            auditLog.setResourceId(resourceId);
            auditLog.setStatus(status);
            auditLog.setDetails(details);
            auditLog.setTimestamp(LocalDateTime.now());

            // Set request details
            if (request != null) {
                auditLog.setMethod(request.getMethod());
                auditLog.setEndpoint(request.getRequestURI());
                auditLog.setIpAddress(getClientIpAddress());
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log audit failure but don't throw exception to avoid breaking main flow
            System.err.println("Failed to log audit event: " + e.getMessage());
        }
    }

    @Async
    public void logSuccessfulAction(String action, String resource, String resourceId) {
        logAction(action, resource, resourceId, "SUCCESS", null);
    }

    @Async
    public void logFailedAction(String action, String resource, String details) {
        logAction(action, resource, null, "FAILURE", details);
    }

    @Async
    public void logError(String action, String resource, String errorMessage) {
        logAction(action, resource, null, "ERROR", errorMessage);
    }

    private String getClientIpAddress() {
        if (request == null) {
            return "unknown";
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    // Query methods
    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogRepository.findRecentLogs(limit);
    }

    public List<AuditLog> getLogsByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    public List<AuditLog> getFailedActions() {
        return auditLogRepository.findFailedActions();
    }

    public List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate);
    }
}