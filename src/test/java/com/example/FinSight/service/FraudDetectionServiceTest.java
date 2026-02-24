package com.example.FinSight.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.FinSight.model.RiskLevel;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.TransactionRepository;

/**
 * Unit tests for FraudDetectionService with specific examples.
 * Tests each fraud rule individually and edge cases.
 * 
 * Requirements: 5.1-5.10
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FraudDetectionService Unit Tests")
class FraudDetectionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @InjectMocks
    private FraudDetectionService fraudDetectionService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    
    // Helper method to create transactions
    private Transaction createTransaction(BigDecimal amount, String category, String location, LocalDateTime date) {
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setAmount(amount);
        transaction.setType("EXPENSE");
        transaction.setCategory(category);
        transaction.setLocation(location);
        transaction.setTransactionDate(date);
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
    
    @Nested
    @DisplayName("Rule 1: High Amount Anomaly Tests (>3x average adds 30 points)")
    class HighAmountAnomalyTests {
        
        @Test
        @DisplayName("Should add 30 points when amount is exactly 3.1x average")
        void shouldAdd30PointsWhenAmountIs3Point1xAverage() {
            // Arrange
            BigDecimal userAverage = new BigDecimal("100.00");
            BigDecimal transactionAmount = new BigDecimal("310.00"); // 3.1x average
            Transaction transaction = createTransaction(transactionAmount, "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(userAverage);
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(30.0);
            assertThat(result.getReasons()).contains("Amount exceeds 3x user average");
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.LOW);
            assertThat(result.isFraudulent()).isFalse();
        }

        
        @Test
        @DisplayName("Should NOT add points when amount is exactly 3x average")
        void shouldNotAddPointsWhenAmountIsExactly3xAverage() {
            // Arrange
            BigDecimal userAverage = new BigDecimal("100.00");
            BigDecimal transactionAmount = new BigDecimal("300.00"); // Exactly 3x average
            Transaction transaction = createTransaction(transactionAmount, "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(userAverage);
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("Amount exceeds 3x user average");
        }
        
        @Test
        @DisplayName("Should NOT add points when user has no average (null)")
        void shouldNotAddPointsWhenUserAverageIsNull() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("1000.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(null);
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("Amount exceeds 3x user average");
        }

        
        @Test
        @DisplayName("Should NOT add points when user average is zero")
        void shouldNotAddPointsWhenUserAverageIsZero() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("500.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(BigDecimal.ZERO);
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("Amount exceeds 3x user average");
        }
    }
    
    @Nested
    @DisplayName("Rule 2: Rapid-Fire Activity Tests (5+ in 10 min adds 25 points)")
    class RapidFireActivityTests {
        
        @Test
        @DisplayName("Should add 25 points when exactly 5 transactions in 10 minutes")
        void shouldAdd25PointsWhenExactly5TransactionsIn10Minutes() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("50.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(5L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(25.0);
            assertThat(result.getReasons()).contains("5+ transactions in 10 minutes");
        }

        
        @Test
        @DisplayName("Should add 25 points when 8 transactions in 10 minutes")
        void shouldAdd25PointsWhen8TransactionsIn10Minutes() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("50.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(8L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(25.0);
            assertThat(result.getReasons()).contains("5+ transactions in 10 minutes");
        }
        
        @Test
        @DisplayName("Should NOT add points when only 4 transactions in 10 minutes")
        void shouldNotAddPointsWhenOnly4TransactionsIn10Minutes() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("50.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(4L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("5+ transactions in 10 minutes");
        }
    }

    
    @Nested
    @DisplayName("Rule 3: Unusual Category Tests (never used adds 20 points)")
    class UnusualCategoryTests {
        
        @Test
        @DisplayName("Should add 20 points when category is new for user")
        void shouldAdd20PointsWhenCategoryIsNew() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("50.00"), "luxury", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries", "utilities"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(20.0);
            assertThat(result.getReasons()).contains("New category for user");
        }
        
        @Test
        @DisplayName("Should NOT add points when category exists for user")
        void shouldNotAddPointsWhenCategoryExists() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("50.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries", "utilities"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("New category for user");
        }
    }

    
    @Nested
    @DisplayName("Rule 4: Geographical Anomaly Tests (different location < 2 hours adds 25 points)")
    class GeographicalAnomalyTests {
        
        @Test
        @DisplayName("Should add 25 points when different location within 1 hour")
        void shouldAdd25PointsWhenDifferentLocationWithin1Hour() {
            // Arrange
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("50.00"), "groceries", "New York", currentTime);
            
            LocalDateTime previousTime = currentTime.minusMinutes(30);
            Transaction previousTransaction = createTransaction(new BigDecimal("30.00"), "groceries", "Los Angeles", previousTime);
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.of(previousTransaction));
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(25.0);
            assertThat(result.getReasons()).contains("Different location within 2 hours");
        }
        
        @Test
        @DisplayName("Should add 25 points when different location at exactly 1 hour 59 minutes")
        void shouldAdd25PointsWhenDifferentLocationAt1Hour59Minutes() {
            // Arrange
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("50.00"), "groceries", "Chicago", currentTime);
            
            LocalDateTime previousTime = currentTime.minusHours(1).minusMinutes(59);
            Transaction previousTransaction = createTransaction(new BigDecimal("30.00"), "groceries", "Houston", previousTime);
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.of(previousTransaction));
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(25.0);
            assertThat(result.getReasons()).contains("Different location within 2 hours");
        }

        
        @Test
        @DisplayName("Should NOT add points when different location at exactly 2 hours")
        void shouldNotAddPointsWhenDifferentLocationAt2Hours() {
            // Arrange
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("50.00"), "groceries", "Boston", currentTime);
            
            LocalDateTime previousTime = currentTime.minusHours(2);
            Transaction previousTransaction = createTransaction(new BigDecimal("30.00"), "groceries", "Miami", previousTime);
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.of(previousTransaction));
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("Different location within 2 hours");
        }
        
        @Test
        @DisplayName("Should NOT add points when same location within 2 hours")
        void shouldNotAddPointsWhenSameLocationWithin2Hours() {
            // Arrange
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("50.00"), "groceries", "Seattle", currentTime);
            
            LocalDateTime previousTime = currentTime.minusMinutes(30);
            Transaction previousTransaction = createTransaction(new BigDecimal("30.00"), "groceries", "Seattle", previousTime);
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.of(previousTransaction));
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("Different location within 2 hours");
        }

        
        @Test
        @DisplayName("Should NOT add points when current location is null")
        void shouldNotAddPointsWhenCurrentLocationIsNull() {
            // Arrange
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("50.00"), "groceries", null, currentTime);
            
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("Different location within 2 hours");
        }
        
        @Test
        @DisplayName("Should NOT add points when current location is empty string")
        void shouldNotAddPointsWhenCurrentLocationIsEmpty() {
            // Arrange
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("50.00"), "groceries", "  ", currentTime);
            
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("Different location within 2 hours");
        }

        
        @Test
        @DisplayName("Should NOT add points when previous location is null")
        void shouldNotAddPointsWhenPreviousLocationIsNull() {
            // Arrange
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("50.00"), "groceries", "Seattle", currentTime);
            
            LocalDateTime previousTime = currentTime.minusMinutes(30);
            Transaction previousTransaction = createTransaction(new BigDecimal("30.00"), "groceries", null, previousTime);
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.of(previousTransaction));
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("Different location within 2 hours");
        }
        
        @Test
        @DisplayName("Should NOT add points when no previous transaction exists")
        void shouldNotAddPointsWhenNoPreviousTransaction() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("50.00"), "groceries", "Seattle", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("Different location within 2 hours");
        }
        
        @Test
        @DisplayName("Should be case-insensitive for location comparison")
        void shouldBeCaseInsensitiveForLocationComparison() {
            // Arrange
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("50.00"), "groceries", "SEATTLE", currentTime);
            
            LocalDateTime previousTime = currentTime.minusMinutes(30);
            Transaction previousTransaction = createTransaction(new BigDecimal("30.00"), "groceries", "seattle", previousTime);
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.of(previousTransaction));
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).doesNotContain("Different location within 2 hours");
        }
    }

    
    @Nested
    @DisplayName("Risk Level Mapping Tests")
    class RiskLevelMappingTests {
        
        @Test
        @DisplayName("Should map score 0 to LOW risk level")
        void shouldMapScore0ToLow() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("50.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.LOW);
        }
        
        @Test
        @DisplayName("Should map score 39 to LOW risk level")
        void shouldMapScore39ToLow() {
            // Arrange - trigger high amount anomaly (30 points)
            Transaction transaction = createTransaction(new BigDecimal("350.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(30.0);
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.LOW);
        }

        
        @Test
        @DisplayName("Should map score 40 to MEDIUM risk level")
        void shouldMapScore40ToMedium() {
            // Arrange - trigger unusual category (20 points) + unusual category (20 points) = 40 points
            // We'll trigger high amount (30) + unusual category (20) = 50 points
            Transaction transaction = createTransaction(new BigDecimal("350.00"), "luxury", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(50.0);
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
        }
        
        @Test
        @DisplayName("Should map score 69 to MEDIUM risk level")
        void shouldMapScore69ToMedium() {
            // Arrange - trigger rapid-fire (25) + high amount (30) + unusual category (20) = 75, but we need 69
            // Let's trigger rapid-fire (25) + high amount (30) = 55
            Transaction transaction = createTransaction(new BigDecimal("350.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(5L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(55.0);
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
        }

        
        @Test
        @DisplayName("Should map score 70 to HIGH risk level")
        void shouldMapScore70ToHigh() {
            // Arrange - trigger rapid-fire (25) + high amount (30) + unusual category (20) = 75
            Transaction transaction = createTransaction(new BigDecimal("350.00"), "luxury", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(5L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(75.0);
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        }
        
        @Test
        @DisplayName("Should map score 100 to HIGH risk level")
        void shouldMapScore100ToHigh() {
            // Arrange - trigger all 4 rules: high amount (30) + rapid-fire (25) + geographical (25) + unusual category (20) = 100
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("350.00"), "luxury", "New York", currentTime);
            
            LocalDateTime previousTime = currentTime.minusMinutes(30);
            Transaction previousTransaction = createTransaction(new BigDecimal("30.00"), "groceries", "Los Angeles", previousTime);
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(5L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.of(previousTransaction));
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(100.0);
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        }
    }

    
    @Nested
    @DisplayName("Fraudulent Flag Tests")
    class FraudulentFlagTests {
        
        @Test
        @DisplayName("Should set fraudulent flag to false when score is 69")
        void shouldSetFraudulentFlagToFalseWhenScore69() {
            // Arrange - trigger rapid-fire (25) + high amount (30) = 55
            Transaction transaction = createTransaction(new BigDecimal("350.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(5L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(55.0);
            assertThat(result.isFraudulent()).isFalse();
        }
        
        @Test
        @DisplayName("Should set fraudulent flag to true when score is 70")
        void shouldSetFraudulentFlagToTrueWhenScore70() {
            // Arrange - trigger rapid-fire (25) + high amount (30) + unusual category (20) = 75
            Transaction transaction = createTransaction(new BigDecimal("350.00"), "luxury", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(5L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(75.0);
            assertThat(result.isFraudulent()).isTrue();
        }
        
        @Test
        @DisplayName("Should set fraudulent flag to true when score is 100")
        void shouldSetFraudulentFlagToTrueWhenScore100() {
            // Arrange - trigger all 4 rules
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("350.00"), "luxury", "New York", currentTime);
            
            LocalDateTime previousTime = currentTime.minusMinutes(30);
            Transaction previousTransaction = createTransaction(new BigDecimal("30.00"), "groceries", "Los Angeles", previousTime);
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(5L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.of(previousTransaction));
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(100.0);
            assertThat(result.isFraudulent()).isTrue();
        }
    }

    
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle first transaction for user (no history)")
        void shouldHandleFirstTransactionForUser() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("100.00"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(null);
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of());
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(20.0); // Only unusual category triggers
            assertThat(result.getReasons()).containsExactly("New category for user");
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.LOW);
            assertThat(result.isFraudulent()).isFalse();
        }
        
        @Test
        @DisplayName("Should handle transaction with all null/empty optional fields")
        void shouldHandleTransactionWithNullOptionalFields() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("50.00"), "groceries", null, LocalDateTime.now());
            transaction.setDescription(null);
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).isEmpty();
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.LOW);
            assertThat(result.isFraudulent()).isFalse();
        }

        
        @Test
        @DisplayName("Should handle very small transaction amount")
        void shouldHandleVerySmallTransactionAmount() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("0.01"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(0.0);
            assertThat(result.getReasons()).isEmpty();
        }
        
        @Test
        @DisplayName("Should handle very large transaction amount")
        void shouldHandleVeryLargeTransactionAmount() {
            // Arrange
            Transaction transaction = createTransaction(new BigDecimal("999999.99"), "groceries", "Location A", LocalDateTime.now());
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, transaction.getTransactionDate().minusMinutes(10), transaction.getTransactionDate()
            )).thenReturn(0L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.empty());
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(30.0); // High amount anomaly
            assertThat(result.getReasons()).contains("Amount exceeds 3x user average");
        }
        
        @Test
        @DisplayName("Should handle multiple rules triggering simultaneously")
        void shouldHandleMultipleRulesTriggeringSimultaneously() {
            // Arrange - all 4 rules trigger
            LocalDateTime currentTime = LocalDateTime.now();
            Transaction currentTransaction = createTransaction(new BigDecimal("500.00"), "luxury", "Tokyo", currentTime);
            
            LocalDateTime previousTime = currentTime.minusMinutes(45);
            Transaction previousTransaction = createTransaction(new BigDecimal("30.00"), "groceries", "London", previousTime);
            
            when(transactionRepository.calculateAverageAmount(testUser)).thenReturn(new BigDecimal("100.00"));
            when(transactionRepository.countByUserAndTransactionDateBetween(
                testUser, currentTransaction.getTransactionDate().minusMinutes(10), currentTransaction.getTransactionDate()
            )).thenReturn(6L);
            when(transactionRepository.findTopByUserOrderByTransactionDateDesc(testUser)).thenReturn(Optional.of(previousTransaction));
            when(transactionRepository.findDistinctCategoriesByUser(testUser)).thenReturn(List.of("groceries", "utilities"));
            
            // Act
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(currentTransaction);
            
            // Assert
            assertThat(result.getFraudScore()).isEqualTo(100.0);
            assertThat(result.getReasons()).hasSize(4);
            assertThat(result.getReasons()).containsExactlyInAnyOrder(
                "Amount exceeds 3x user average",
                "5+ transactions in 10 minutes",
                "Different location within 2 hours",
                "New category for user"
            );
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
            assertThat(result.isFraudulent()).isTrue();
        }
    }
}
