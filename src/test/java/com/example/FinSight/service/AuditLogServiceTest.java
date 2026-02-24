package com.example.FinSight.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.FinSight.model.AuditLog;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.AuditLogRepository;
import com.example.FinSight.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {
    
    @Mock
    private AuditLogRepository auditLogRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuditLogService auditLogService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }
    
    @Test
    void logAction_WithValidData_CreatesAuditLog() {
        // Arrange
        Long userId = 1L;
        String action = "CREATE_TRANSACTION";
        String entityType = "TRANSACTION";
        Long entityId = 100L;
        String details = "{\"amount\": 50.00, \"category\": \"groceries\"}";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        auditLogService.logAction(userId, action, entityType, entityId, details);
        
        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertThat(savedAuditLog.getUser()).isEqualTo(testUser);
        assertThat(savedAuditLog.getAction()).isEqualTo(action);
        assertThat(savedAuditLog.getEntityType()).isEqualTo(entityType);
        assertThat(savedAuditLog.getEntityId()).isEqualTo(entityId);
        assertThat(savedAuditLog.getDetails()).isEqualTo(details);
        assertThat(savedAuditLog.getTimestamp()).isNotNull();
    }
    
    @Test
    void logAction_WithUTCTimestamp_SavesCorrectTimestamp() {
        // Arrange
        Long userId = 1L;
        String action = "RESOLVE_ALERT";
        String entityType = "FRAUD_ALERT";
        Long entityId = 50L;
        String details = "{\"resolved\": true}";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        LocalDateTime beforeCall = LocalDateTime.now(ZoneOffset.UTC);
        
        // Act
        auditLogService.logAction(userId, action, entityType, entityId, details);
        
        LocalDateTime afterCall = LocalDateTime.now(ZoneOffset.UTC);
        
        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertThat(savedAuditLog.getTimestamp())
            .isAfterOrEqualTo(beforeCall)
            .isBeforeOrEqualTo(afterCall);
    }
    
    @Test
    void logAction_WithNullEntityId_CreatesAuditLog() {
        // Arrange
        Long userId = 1L;
        String action = "SEED_DEMO_DATA";
        String entityType = "TRANSACTION";
        Long entityId = null; // Null entity ID is valid for some actions
        String details = "{\"count\": 30}";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        auditLogService.logAction(userId, action, entityType, entityId, details);
        
        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertThat(savedAuditLog.getEntityId()).isNull();
        assertThat(savedAuditLog.getAction()).isEqualTo(action);
    }
    
    @Test
    void logAction_WithInvalidUserId_ThrowsException() {
        // Arrange
        Long invalidUserId = 999L;
        String action = "CREATE_TRANSACTION";
        String entityType = "TRANSACTION";
        Long entityId = 100L;
        String details = "{}";
        
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> 
            auditLogService.logAction(invalidUserId, action, entityType, entityId, details))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found with id: " + invalidUserId);
    }
    
    @Test
    void logAction_WithJSONDetails_PreservesJSONFormat() {
        // Arrange
        Long userId = 1L;
        String action = "IGNORE_SUBSCRIPTION";
        String entityType = "SUBSCRIPTION";
        Long entityId = 25L;
        String details = "{\"merchant\": \"Netflix\", \"amount\": 15.99, \"reason\": \"user_action\"}";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        auditLogService.logAction(userId, action, entityType, entityId, details);
        
        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertThat(savedAuditLog.getDetails()).isEqualTo(details);
        assertThat(savedAuditLog.getDetails()).contains("merchant", "Netflix", "amount", "15.99");
    }
}
