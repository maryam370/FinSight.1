package com.example.FinSight.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FinSight.model.AuditLog;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.AuditLogRepository;
import com.example.FinSight.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    
    /**
     * Logs a user action for compliance tracking.
     * 
     * @param userId The user performing the action
     * @param action Action type
     * @param entityType Entity being acted upon
     * @param entityId ID of the entity
     * @param details Additional details in JSON format
     */
    @Transactional
    public void logAction(Long userId, String action, String entityType, 
                         Long entityId, String details) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        AuditLog auditLog = createAuditLog(user, action, entityType, entityId, details);
        
        auditLogRepository.save(auditLog);
        
        log.info("Audit log created: user={}, action={}, entityType={}, entityId={}", 
            userId, action, entityType, entityId);
    }
    
    private AuditLog createAuditLog(User user, String action, String entityType, 
                                     Long entityId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
        return auditLog;
    }
}
