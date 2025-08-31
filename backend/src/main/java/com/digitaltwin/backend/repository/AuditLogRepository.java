package com.digitaltwin.backend.repository;

import com.digitaltwin.backend.model.AuditLog;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends Neo4jRepository<AuditLog, Long> {

    List<AuditLog> findByUsername(String username);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByResource(String resource);

    List<AuditLog> findByStatus(String status);

    @Query("MATCH (a:AuditLog) WHERE a.timestamp >= $startDate AND a.timestamp <= $endDate RETURN a ORDER BY a.timestamp DESC")
    List<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("MATCH (a:AuditLog) WHERE a.username = $username AND a.timestamp >= $startDate AND a.timestamp <= $endDate RETURN a ORDER BY a.timestamp DESC")
    List<AuditLog> findByUsernameAndTimestampBetween(String username, LocalDateTime startDate, LocalDateTime endDate);

    @Query("MATCH (a:AuditLog) RETURN a ORDER BY a.timestamp DESC LIMIT $limit")
    List<AuditLog> findRecentLogs(int limit);

    @Query("MATCH (a:AuditLog) WHERE a.status = 'FAILURE' OR a.status = 'ERROR' RETURN a ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedActions();

    @Query("MATCH (a:AuditLog) WHERE a.ipAddress = $ipAddress RETURN a ORDER BY a.timestamp DESC")
    List<AuditLog> findByIpAddress(String ipAddress);
}