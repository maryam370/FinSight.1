package com.example.FinSight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FinSight.model.AuditLog;
import com.example.FinSight.model.User;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserOrderByTimestampDesc(User user);
    
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
