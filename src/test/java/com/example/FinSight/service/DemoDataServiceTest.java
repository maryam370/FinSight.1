package com.example.FinSight.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.FinSight.model.RiskLevel;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.TransactionRepository;
import com.example.FinSight.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("DemoDataService Tests")
class DemoDataServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private FraudDetectionService fraudDetectionService;
        
    @InjectMocks
    private DemoDataService demoDataService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }
    
    @Test
    @DisplayName("Should seed demo data when user has zero transactions")
    void shouldSeedDemoDataWhenUserHasZeroTransactions() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.countByUser(testUser)).thenReturn(0L);
        when(fraudDetectionService.analyzeTransaction(any(Transaction.class)))
            .thenReturn(new FraudDetectionResult(false, 0.0, RiskLevel.LOW, List.of()));
        
        // Act
        int count = demoDataService.seedUserIfEmpty(1L);
        
        // Assert
        assertThat(count).isBetween(25, 50);
        
        ArgumentCaptor<List<Transaction>> transactionCaptor = ArgumentCaptor.forClass(List.class);
        verify(transactionRepository).saveAll(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getValue();
        assertThat(savedTransactions).hasSize(count);
        
        // Verify fraud detection was called for each transaction
        verify(fraudDetectionService, times(count)).analyzeTransaction(any(Transaction.class));
        

    }
    
    @Test
    @DisplayName("Should not seed demo data when user already has transactions")
    void shouldNotSeedDemoDataWhenUserAlreadyHasTransactions() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.countByUser(testUser)).thenReturn(10L);
        
        // Act
        int count = demoDataService.seedUserIfEmpty(1L);
        
        // Assert
        assertThat(count).isEqualTo(0);
        verify(transactionRepository, never()).saveAll(any());
        verify(fraudDetectionService, never()).analyzeTransaction(any());
    }
    
    @Test
    @DisplayName("Should generate transactions within 60-90 days range")
    void shouldGenerateTransactionsWithin60To90DaysRange() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.countByUser(testUser)).thenReturn(0L);
        when(fraudDetectionService.analyzeTransaction(any(Transaction.class)))
            .thenReturn(new FraudDetectionResult(false, 0.0, RiskLevel.LOW, List.of()));
        
        // Act
        demoDataService.seedUserIfEmpty(1L);
        
        // Assert
        ArgumentCaptor<List<Transaction>> transactionCaptor = ArgumentCaptor.forClass(List.class);
        verify(transactionRepository).saveAll(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getValue();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minDate = now.minusDays(90);
        LocalDateTime maxDate = now.minusDays(60);
        
        for (Transaction txn : savedTransactions) {
            assertThat(txn.getTransactionDate()).isBetween(minDate, now);
        }
    }
    
    @Test
    @DisplayName("Should generate transactions with varied categories")
    void shouldGenerateTransactionsWithVariedCategories() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.countByUser(testUser)).thenReturn(0L);
        when(fraudDetectionService.analyzeTransaction(any(Transaction.class)))
            .thenReturn(new FraudDetectionResult(false, 0.0, RiskLevel.LOW, List.of()));
        
        // Act
        demoDataService.seedUserIfEmpty(1L);
        
        // Assert
        ArgumentCaptor<List<Transaction>> transactionCaptor = ArgumentCaptor.forClass(List.class);
        verify(transactionRepository).saveAll(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getValue();
        
        // Check that we have multiple categories
        long distinctCategories = savedTransactions.stream()
            .map(Transaction::getCategory)
            .distinct()
            .count();
        
        assertThat(distinctCategories).isGreaterThanOrEqualTo(3);
        
        // Check that we have both INCOME and EXPENSE types
        boolean hasIncome = savedTransactions.stream()
            .anyMatch(t -> "INCOME".equals(t.getType()));
        boolean hasExpense = savedTransactions.stream()
            .anyMatch(t -> "EXPENSE".equals(t.getType()));
        
        assertThat(hasIncome).isTrue();
        assertThat(hasExpense).isTrue();
    }
    
    @Test
    @DisplayName("Should generate transactions with realistic amounts")
    void shouldGenerateTransactionsWithRealisticAmounts() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.countByUser(testUser)).thenReturn(0L);
        when(fraudDetectionService.analyzeTransaction(any(Transaction.class)))
            .thenReturn(new FraudDetectionResult(false, 0.0, RiskLevel.LOW, List.of()));
        
        // Act
        demoDataService.seedUserIfEmpty(1L);
        
        // Assert
        ArgumentCaptor<List<Transaction>> transactionCaptor = ArgumentCaptor.forClass(List.class);
        verify(transactionRepository).saveAll(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getValue();
        
        // All amounts should be positive
        for (Transaction txn : savedTransactions) {
            assertThat(txn.getAmount()).isGreaterThan(BigDecimal.ZERO);
            
            // Check realistic ranges based on category
            String category = txn.getCategory();
            BigDecimal amount = txn.getAmount();
            
            switch (category) {
                case "groceries":
                    // Should be within reasonable range (allowing for fraud triggers)
                    assertThat(amount).isGreaterThanOrEqualTo(BigDecimal.valueOf(20));
                    break;
                case "utilities":
                    assertThat(amount).isGreaterThanOrEqualTo(BigDecimal.valueOf(50));
                    break;
                case "entertainment":
                    assertThat(amount).isGreaterThanOrEqualTo(BigDecimal.valueOf(10));
                    break;
                case "salary":
                    assertThat(amount).isGreaterThanOrEqualTo(BigDecimal.valueOf(2000));
                    break;
                case "rent":
                    assertThat(amount).isGreaterThanOrEqualTo(BigDecimal.valueOf(800));
                    break;
                case "subscriptions":
                    assertThat(amount).isGreaterThanOrEqualTo(BigDecimal.valueOf(5));
                    break;
                case "transport":
                    assertThat(amount).isGreaterThanOrEqualTo(BigDecimal.valueOf(10));
                    break;
            }
        }
    }
    
    @Test
    @DisplayName("Should generate deterministic data for same user ID")
    void shouldGenerateDeterministicDataForSameUserId() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.countByUser(testUser)).thenReturn(0L);
        when(fraudDetectionService.analyzeTransaction(any(Transaction.class)))
            .thenReturn(new FraudDetectionResult(false, 0.0, RiskLevel.LOW, List.of()));
        
        // Act - First run
        int count1 = demoDataService.seedUserIfEmpty(1L);
        
        ArgumentCaptor<List<Transaction>> captor1 = ArgumentCaptor.forClass(List.class);
        verify(transactionRepository, times(1)).saveAll(captor1.capture());
        List<Transaction> transactions1 = captor1.getValue();
        
        // Reset mocks for second run
        when(transactionRepository.countByUser(testUser)).thenReturn(0L);
        
        // Act - Second run
        int count2 = demoDataService.seedUserIfEmpty(1L);
        
        ArgumentCaptor<List<Transaction>> captor2 = ArgumentCaptor.forClass(List.class);
        verify(transactionRepository, times(2)).saveAll(captor2.capture());
        List<Transaction> transactions2 = captor2.getAllValues().get(1);
        
        // Assert - Both runs should produce same count
        assertThat(count1).isEqualTo(count2);
        assertThat(transactions1).hasSameSizeAs(transactions2);
        
        // Verify same categories and amounts (deterministic)
        for (int i = 0; i < transactions1.size(); i++) {
            Transaction t1 = transactions1.get(i);
            Transaction t2 = transactions2.get(i);
            
            assertThat(t1.getCategory()).isEqualTo(t2.getCategory());
            assertThat(t1.getAmount()).isEqualByComparingTo(t2.getAmount());
            assertThat(t1.getType()).isEqualTo(t2.getType());
        }
    }
}
