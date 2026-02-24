package com.example.FinSight.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.FinSight.dto.FraudAlertDto;
import com.example.FinSight.model.FraudAlert;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.FraudAlertRepository;
import com.example.FinSight.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FraudAlertServiceTest {
    
    @Mock
    private FraudAlertRepository fraudAlertRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AuditLogService auditLogService;
    
    @InjectMocks
    private FraudAlertService fraudAlertService;
    
    private User testUser;
    private Transaction testTransaction;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        testTransaction = new Transaction();
        testTransaction.setId(100L);
        testTransaction.setAmount(new BigDecimal("500.00"));
        testTransaction.setType("DEBIT");
        testTransaction.setCategory("Shopping");
        testTransaction.setDescription("Suspicious purchase");
        testTransaction.setLocation("Unknown Location");
        testTransaction.setTransactionDate(LocalDateTime.now());
        testTransaction.setFraudulent(true);
        testTransaction.setFraudScore(85.0);
    }
    
    @Test
    void findByUser_WithValidUserId_ReturnsAlertsOrderedByCreatedAtDesc() {
        // Arrange
        FraudAlert alert1 = createFraudAlert(1L, "HIGH", false, LocalDateTime.now().minusDays(2));
        FraudAlert alert2 = createFraudAlert(2L, "MEDIUM", false, LocalDateTime.now().minusDays(1));
        FraudAlert alert3 = createFraudAlert(3L, "LOW", true, LocalDateTime.now());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fraudAlertRepository.findByUserOrderByCreatedAtDesc(testUser))
            .thenReturn(Arrays.asList(alert3, alert2, alert1)); // Newest first
        
        // Act
        List<FraudAlertDto> result = fraudAlertService.findByUser(1L);
        
        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(3L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(1L);
        assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
        assertThat(result.get(1).getCreatedAt()).isAfter(result.get(2).getCreatedAt());
    }
    
    @Test
    void findByUser_WithInvalidUserId_ThrowsException() {
        // Arrange
        Long invalidUserId = 999L;
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> fraudAlertService.findByUser(invalidUserId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found with id: " + invalidUserId);
    }
    
    @Test
    void findByUserAndResolved_WithResolvedTrue_ReturnsOnlyResolvedAlerts() {
        // Arrange
        FraudAlert resolvedAlert1 = createFraudAlert(1L, "HIGH", true, LocalDateTime.now().minusDays(1));
        FraudAlert resolvedAlert2 = createFraudAlert(2L, "MEDIUM", true, LocalDateTime.now());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fraudAlertRepository.findByUserAndResolvedOrderByCreatedAtDesc(testUser, true))
            .thenReturn(Arrays.asList(resolvedAlert2, resolvedAlert1));
        
        // Act
        List<FraudAlertDto> result = fraudAlertService.findByUserAndResolved(1L, true);
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(FraudAlertDto::isResolved);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
    }
    
    @Test
    void findByUserAndResolved_WithResolvedFalse_ReturnsOnlyUnresolvedAlerts() {
        // Arrange
        FraudAlert unresolvedAlert1 = createFraudAlert(1L, "HIGH", false, LocalDateTime.now().minusDays(1));
        FraudAlert unresolvedAlert2 = createFraudAlert(2L, "LOW", false, LocalDateTime.now());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fraudAlertRepository.findByUserAndResolvedOrderByCreatedAtDesc(testUser, false))
            .thenReturn(Arrays.asList(unresolvedAlert2, unresolvedAlert1));
        
        // Act
        List<FraudAlertDto> result = fraudAlertService.findByUserAndResolved(1L, false);
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).noneMatch(FraudAlertDto::isResolved);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
    }
    
    @Test
    void findByUserAndResolved_WithInvalidUserId_ThrowsException() {
        // Arrange
        Long invalidUserId = 999L;
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> fraudAlertService.findByUserAndResolved(invalidUserId, true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found with id: " + invalidUserId);
    }
    
    @Test
    void findByUserAndSeverity_WithHighSeverity_ReturnsOnlyHighSeverityAlerts() {
        // Arrange
        FraudAlert highAlert1 = createFraudAlert(1L, "HIGH", false, LocalDateTime.now().minusDays(1));
        FraudAlert highAlert2 = createFraudAlert(2L, "HIGH", true, LocalDateTime.now());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fraudAlertRepository.findByUserAndSeverityOrderByCreatedAtDesc(testUser, "HIGH"))
            .thenReturn(Arrays.asList(highAlert2, highAlert1));
        
        // Act
        List<FraudAlertDto> result = fraudAlertService.findByUserAndSeverity(1L, "HIGH");
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(dto -> dto.getSeverity().equals("HIGH"));
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
    }
    
    @Test
    void findByUserAndSeverity_WithMediumSeverity_ReturnsOnlyMediumSeverityAlerts() {
        // Arrange
        FraudAlert mediumAlert = createFraudAlert(1L, "MEDIUM", false, LocalDateTime.now());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fraudAlertRepository.findByUserAndSeverityOrderByCreatedAtDesc(testUser, "MEDIUM"))
            .thenReturn(Arrays.asList(mediumAlert));
        
        // Act
        List<FraudAlertDto> result = fraudAlertService.findByUserAndSeverity(1L, "MEDIUM");
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeverity()).isEqualTo("MEDIUM");
    }
    
    @Test
    void findByUserAndSeverity_WithLowSeverity_ReturnsOnlyLowSeverityAlerts() {
        // Arrange
        FraudAlert lowAlert = createFraudAlert(1L, "LOW", true, LocalDateTime.now());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fraudAlertRepository.findByUserAndSeverityOrderByCreatedAtDesc(testUser, "LOW"))
            .thenReturn(Arrays.asList(lowAlert));
        
        // Act
        List<FraudAlertDto> result = fraudAlertService.findByUserAndSeverity(1L, "LOW");
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeverity()).isEqualTo("LOW");
    }
    
    @Test
    void findByUserAndSeverity_WithInvalidUserId_ThrowsException() {
        // Arrange
        Long invalidUserId = 999L;
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> fraudAlertService.findByUserAndSeverity(invalidUserId, "HIGH"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found with id: " + invalidUserId);
    }
    
    @Test
    void findByUserAndResolvedAndSeverity_WithResolvedFalseAndHighSeverity_ReturnsFilteredAlerts() {
        // Arrange
        FraudAlert unresolvedHighAlert = createFraudAlert(1L, "HIGH", false, LocalDateTime.now());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fraudAlertRepository.findByUserAndResolvedAndSeverityOrderByCreatedAtDesc(testUser, false, "HIGH"))
            .thenReturn(Arrays.asList(unresolvedHighAlert));
        
        // Act
        List<FraudAlertDto> result = fraudAlertService.findByUserAndResolvedAndSeverity(1L, false, "HIGH");
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isResolved()).isFalse();
        assertThat(result.get(0).getSeverity()).isEqualTo("HIGH");
    }
    
    @Test
    void findByUserAndResolvedAndSeverity_WithResolvedTrueAndMediumSeverity_ReturnsFilteredAlerts() {
        // Arrange
        FraudAlert resolvedMediumAlert = createFraudAlert(1L, "MEDIUM", true, LocalDateTime.now());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fraudAlertRepository.findByUserAndResolvedAndSeverityOrderByCreatedAtDesc(testUser, true, "MEDIUM"))
            .thenReturn(Arrays.asList(resolvedMediumAlert));
        
        // Act
        List<FraudAlertDto> result = fraudAlertService.findByUserAndResolvedAndSeverity(1L, true, "MEDIUM");
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isResolved()).isTrue();
        assertThat(result.get(0).getSeverity()).isEqualTo("MEDIUM");
    }
    
    @Test
    void findByUserAndResolvedAndSeverity_WithInvalidUserId_ThrowsException() {
        // Arrange
        Long invalidUserId = 999L;
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> fraudAlertService.findByUserAndResolvedAndSeverity(invalidUserId, false, "HIGH"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found with id: " + invalidUserId);
    }
    
    @Test
    void resolveAlert_WithValidAlertId_UpdatesResolvedFlagAndLogsAction() {
        // Arrange
        FraudAlert unresolvedAlert = createFraudAlert(1L, "HIGH", false, LocalDateTime.now());
        FraudAlert resolvedAlert = createFraudAlert(1L, "HIGH", true, LocalDateTime.now());
        
        when(fraudAlertRepository.findById(1L)).thenReturn(Optional.of(unresolvedAlert));
        when(fraudAlertRepository.save(any(FraudAlert.class))).thenReturn(resolvedAlert);
        
        // Act
        FraudAlertDto result = fraudAlertService.resolveAlert(1L);
        
        // Assert
        assertThat(result.isResolved()).isTrue();
        
        // Verify the alert was saved with resolved flag set to true
        ArgumentCaptor<FraudAlert> alertCaptor = ArgumentCaptor.forClass(FraudAlert.class);
        verify(fraudAlertRepository).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().isResolved()).isTrue();
        
        // Verify audit log was created
        verify(auditLogService).logAction(
            eq(1L),
            eq("RESOLVE_FRAUD_ALERT"),
            eq("FRAUD_ALERT"),
            eq(1L),
            eq("{\"alertId\": 1, \"severity\": \"HIGH\"}")
        );
    }
    
    @Test
    void resolveAlert_WithInvalidAlertId_ThrowsException() {
        // Arrange
        Long invalidAlertId = 999L;
        when(fraudAlertRepository.findById(invalidAlertId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> fraudAlertService.resolveAlert(invalidAlertId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Fraud alert not found with id: " + invalidAlertId);
    }
    
    @Test
    void resolveAlert_WithAlreadyResolvedAlert_StillUpdatesAndLogs() {
        // Arrange
        FraudAlert alreadyResolvedAlert = createFraudAlert(1L, "MEDIUM", true, LocalDateTime.now());
        
        when(fraudAlertRepository.findById(1L)).thenReturn(Optional.of(alreadyResolvedAlert));
        when(fraudAlertRepository.save(any(FraudAlert.class))).thenReturn(alreadyResolvedAlert);
        
        // Act
        FraudAlertDto result = fraudAlertService.resolveAlert(1L);
        
        // Assert
        assertThat(result.isResolved()).isTrue();
        verify(fraudAlertRepository).save(any(FraudAlert.class));
        verify(auditLogService).logAction(
            eq(1L),
            eq("RESOLVE_FRAUD_ALERT"),
            eq("FRAUD_ALERT"),
            eq(1L),
            eq("{\"alertId\": 1, \"severity\": \"MEDIUM\"}")
        );
    }
    
    @Test
    void findByUser_WithTransactionDetails_IncludesTransactionInDto() {
        // Arrange
        FraudAlert alertWithTransaction = createFraudAlert(1L, "HIGH", false, LocalDateTime.now());
        alertWithTransaction.setTransaction(testTransaction);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fraudAlertRepository.findByUserOrderByCreatedAtDesc(testUser))
            .thenReturn(Arrays.asList(alertWithTransaction));
        
        // Act
        List<FraudAlertDto> result = fraudAlertService.findByUser(1L);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransaction()).isNotNull();
        assertThat(result.get(0).getTransaction().getId()).isEqualTo(100L);
        assertThat(result.get(0).getTransaction().getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.get(0).getTransaction().isFraudulent()).isTrue();
        assertThat(result.get(0).getTransaction().getFraudScore()).isEqualTo(85.0);
        assertThat(result.get(0).getTransaction().getRiskLevel()).isEqualTo("HIGH");
    }
    
    @Test
    void findByUser_WithoutTransactionDetails_ReturnsAlertWithoutTransaction() {
        // Arrange
        FraudAlert alertWithoutTransaction = createFraudAlert(1L, "LOW", false, LocalDateTime.now());
        alertWithoutTransaction.setTransaction(null);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(fraudAlertRepository.findByUserOrderByCreatedAtDesc(testUser))
            .thenReturn(Arrays.asList(alertWithoutTransaction));
        
        // Act
        List<FraudAlertDto> result = fraudAlertService.findByUser(1L);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransaction()).isNull();
    }
    
    @Test
    void resolveAlert_WithDifferentSeverityLevels_LogsCorrectSeverity() {
        // Arrange
        FraudAlert lowSeverityAlert = createFraudAlert(1L, "LOW", false, LocalDateTime.now());
        
        when(fraudAlertRepository.findById(1L)).thenReturn(Optional.of(lowSeverityAlert));
        when(fraudAlertRepository.save(any(FraudAlert.class))).thenReturn(lowSeverityAlert);
        
        // Act
        fraudAlertService.resolveAlert(1L);
        
        // Assert
        verify(auditLogService).logAction(
            eq(1L),
            eq("RESOLVE_FRAUD_ALERT"),
            eq("FRAUD_ALERT"),
            eq(1L),
            eq("{\"alertId\": 1, \"severity\": \"LOW\"}")
        );
    }
    
    // Helper method to create FraudAlert test objects
    private FraudAlert createFraudAlert(Long id, String severity, boolean resolved, LocalDateTime createdAt) {
        FraudAlert alert = new FraudAlert();
        alert.setId(id);
        alert.setUser(testUser);
        alert.setMessage("Test fraud alert message");
        alert.setSeverity(severity);
        alert.setResolved(resolved);
        alert.setCreatedAt(createdAt);
        return alert;
    }
}
