package com.example.FinSight.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.FinSight.model.Subscription;
import com.example.FinSight.model.SubscriptionStatus;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.SubscriptionRepository;
import com.example.FinSight.repository.TransactionRepository;
import com.example.FinSight.repository.UserRepository;

/**
 * Unit tests for SubscriptionDetectorService with specific examples.
 * Tests subscription detection patterns, merchant normalization, and due-soon filtering.
 * 
 * Requirements: 8.1-8.10
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionDetectorService Unit Tests")
class SubscriptionDetectorServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private SubscriptionRepository subscriptionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private SubscriptionDetectorService subscriptionDetectorService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setCreatedAt(LocalDateTime.now());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }
    
    // Helper method to create a transaction
    private Transaction createTransaction(String merchant, BigDecimal amount, LocalDateTime date) {
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setAmount(amount);
        transaction.setType("EXPENSE");
        transaction.setCategory("subscription");
        transaction.setDescription(merchant);
        transaction.setLocation("Online");
        transaction.setTransactionDate(date);
        transaction.setFraudulent(false);
        transaction.setFraudScore(0.0);
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
    
    @Nested
    @DisplayName("Detection with 2 Qualifying Payments Tests")
    class DetectionWith2QualifyingPaymentsTests {
        
        @Test
        @DisplayName("Should detect subscription with exactly 2 qualifying payments (30 days apart)")
        void shouldDetectSubscriptionWith2QualifyingPayments30DaysApart() {
            // Arrange
            String merchant = "Netflix";
            BigDecimal amount = new BigDecimal("15.99");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(60);
            LocalDateTime secondDate = firstDate.plusDays(30);
            LocalDateTime thirdDate = secondDate.plusDays(30);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(merchant, amount, firstDate));
            transactions.add(createTransaction(merchant, amount, secondDate));
            transactions.add(createTransaction(merchant, amount, thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).hasSize(1);
            Subscription sub = subscriptions.get(0);
            assertThat(sub.getMerchant()).isEqualTo(merchant);
            assertThat(sub.getAvgAmount()).isEqualByComparingTo(amount);
            assertThat(sub.getLastPaidDate()).isEqualTo(thirdDate.toLocalDate());
            assertThat(sub.getNextDueDate()).isEqualTo(thirdDate.toLocalDate().plusDays(30));
            assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }
        
        @Test
        @DisplayName("Should detect subscription with 2 qualifying payments at 25 days apart")
        void shouldDetectSubscriptionWith2QualifyingPayments25DaysApart() {
            // Arrange
            String merchant = "Spotify";
            BigDecimal amount = new BigDecimal("9.99");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(50);
            LocalDateTime secondDate = firstDate.plusDays(25);
            LocalDateTime thirdDate = secondDate.plusDays(25);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(merchant, amount, firstDate));
            transactions.add(createTransaction(merchant, amount, secondDate));
            transactions.add(createTransaction(merchant, amount, thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).hasSize(1);
            assertThat(subscriptions.get(0).getMerchant()).isEqualTo(merchant);
        }
        
        @Test
        @DisplayName("Should detect subscription with 2 qualifying payments at 35 days apart")
        void shouldDetectSubscriptionWith2QualifyingPayments35DaysApart() {
            // Arrange
            String merchant = "Amazon Prime";
            BigDecimal amount = new BigDecimal("12.99");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(70);
            LocalDateTime secondDate = firstDate.plusDays(35);
            LocalDateTime thirdDate = secondDate.plusDays(35);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(merchant, amount, firstDate));
            transactions.add(createTransaction(merchant, amount, secondDate));
            transactions.add(createTransaction(merchant, amount, thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).hasSize(1);
            assertThat(subscriptions.get(0).getMerchant()).isEqualTo(merchant);
        }
    }
    
    @Nested
    @DisplayName("Detection Ignores Payments Outside 25-35 Day Range Tests")
    class DetectionIgnoresPaymentsOutsideRangeTests {
        
        @Test
        @DisplayName("Should NOT detect subscription when payments are 24 days apart")
        void shouldNotDetectSubscriptionWhenPayments24DaysApart() {
            // Arrange
            String merchant = "Coffee Shop";
            BigDecimal amount = new BigDecimal("5.50");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(48);
            LocalDateTime secondDate = firstDate.plusDays(24);
            LocalDateTime thirdDate = secondDate.plusDays(24);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(merchant, amount, firstDate));
            transactions.add(createTransaction(merchant, amount, secondDate));
            transactions.add(createTransaction(merchant, amount, thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).isEmpty();
        }
        
        @Test
        @DisplayName("Should NOT detect subscription when payments are 36 days apart")
        void shouldNotDetectSubscriptionWhenPayments36DaysApart() {
            // Arrange
            String merchant = "Gym Membership";
            BigDecimal amount = new BigDecimal("49.99");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(72);
            LocalDateTime secondDate = firstDate.plusDays(36);
            LocalDateTime thirdDate = secondDate.plusDays(36);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(merchant, amount, firstDate));
            transactions.add(createTransaction(merchant, amount, secondDate));
            transactions.add(createTransaction(merchant, amount, thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).isEmpty();
        }
        
        @Test
        @DisplayName("Should NOT detect subscription with only 1 qualifying pair (need 2)")
        void shouldNotDetectSubscriptionWithOnly1QualifyingPair() {
            // Arrange
            String merchant = "Magazine Subscription";
            BigDecimal amount = new BigDecimal("19.99");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(66);
            LocalDateTime secondDate = firstDate.plusDays(30); // Qualifying
            LocalDateTime thirdDate = secondDate.plusDays(50); // Not qualifying (>35 days)
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(merchant, amount, firstDate));
            transactions.add(createTransaction(merchant, amount, secondDate));
            transactions.add(createTransaction(merchant, amount, thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("Merchant Name Normalization Tests")
    class MerchantNameNormalizationTests {
        
        @Test
        @DisplayName("Should normalize merchant names with different cases")
        void shouldNormalizeMerchantNamesWithDifferentCases() {
            // Arrange
            BigDecimal amount = new BigDecimal("15.99");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(60);
            LocalDateTime secondDate = firstDate.plusDays(30);
            LocalDateTime thirdDate = secondDate.plusDays(30);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction("Netflix", amount, firstDate));
            transactions.add(createTransaction("NETFLIX", amount, secondDate));
            transactions.add(createTransaction("netflix", amount, thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).hasSize(1);
            assertThat(subscriptions.get(0).getMerchant()).isEqualTo("netflix"); // Uses most recent
        }
        
        @Test
        @DisplayName("Should normalize merchant names with special characters")
        void shouldNormalizeMerchantNamesWithSpecialCharacters() {
            // Arrange
            BigDecimal amount = new BigDecimal("9.99");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(60);
            LocalDateTime secondDate = firstDate.plusDays(30);
            LocalDateTime thirdDate = secondDate.plusDays(30);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction("Spotify Inc.", amount, firstDate));
            transactions.add(createTransaction("Spotify-Inc", amount, secondDate));
            transactions.add(createTransaction("Spotify_Inc", amount, thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).hasSize(1);
            assertThat(subscriptions.get(0).getMerchant()).isEqualTo("Spotify_Inc"); // Uses most recent
        }
        
        @Test
        @DisplayName("Should normalize merchant names with spaces")
        void shouldNormalizeMerchantNamesWithSpaces() {
            // Arrange
            BigDecimal amount = new BigDecimal("12.99");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(60);
            LocalDateTime secondDate = firstDate.plusDays(30);
            LocalDateTime thirdDate = secondDate.plusDays(30);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction("Amazon Prime", amount, firstDate));
            transactions.add(createTransaction("AmazonPrime", amount, secondDate));
            transactions.add(createTransaction("Amazon  Prime", amount, thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).hasSize(1);
            assertThat(subscriptions.get(0).getMerchant()).isEqualTo("Amazon  Prime"); // Uses most recent
        }
    }
    
    @Nested
    @DisplayName("Average Amount Calculation Tests")
    class AverageAmountCalculationTests {
        
        @Test
        @DisplayName("Should calculate average amount correctly with same amounts")
        void shouldCalculateAverageAmountWithSameAmounts() {
            // Arrange
            String merchant = "Netflix";
            BigDecimal amount = new BigDecimal("15.99");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(60);
            LocalDateTime secondDate = firstDate.plusDays(30);
            LocalDateTime thirdDate = secondDate.plusDays(30);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(merchant, amount, firstDate));
            transactions.add(createTransaction(merchant, amount, secondDate));
            transactions.add(createTransaction(merchant, amount, thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).hasSize(1);
            assertThat(subscriptions.get(0).getAvgAmount()).isEqualByComparingTo(new BigDecimal("15.99"));
        }
        
        @Test
        @DisplayName("Should calculate average amount correctly with varying amounts")
        void shouldCalculateAverageAmountWithVaryingAmounts() {
            // Arrange
            String merchant = "Spotify";
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(60);
            LocalDateTime secondDate = firstDate.plusDays(30);
            LocalDateTime thirdDate = secondDate.plusDays(30);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(merchant, new BigDecimal("9.99"), firstDate));
            transactions.add(createTransaction(merchant, new BigDecimal("10.99"), secondDate));
            transactions.add(createTransaction(merchant, new BigDecimal("11.99"), thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).hasSize(1);
            // Average: (9.99 + 10.99 + 11.99) / 3 = 32.97 / 3 = 10.99
            assertThat(subscriptions.get(0).getAvgAmount()).isEqualByComparingTo(new BigDecimal("10.99"));
        }
        
        @Test
        @DisplayName("Should round average amount to 2 decimal places")
        void shouldRoundAverageAmountTo2DecimalPlaces() {
            // Arrange
            String merchant = "Amazon Prime";
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(60);
            LocalDateTime secondDate = firstDate.plusDays(30);
            LocalDateTime thirdDate = secondDate.plusDays(30);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(merchant, new BigDecimal("10.00"), firstDate));
            transactions.add(createTransaction(merchant, new BigDecimal("10.00"), secondDate));
            transactions.add(createTransaction(merchant, new BigDecimal("10.01"), thirdDate));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).hasSize(1);
            // Average: (10.00 + 10.00 + 10.01) / 3 = 30.01 / 3 = 10.003333... rounded to 10.00
            assertThat(subscriptions.get(0).getAvgAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        }
    }
    
    @Nested
    @DisplayName("Due-Soon Filtering Tests")
    class DueSoonFilteringTests {
        
        @Test
        @DisplayName("Should return subscriptions due within 7 days")
        void shouldReturnSubscriptionsDueWithin7Days() {
            // Arrange
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(7);
            
            Subscription sub1 = createSubscription("Netflix", new BigDecimal("15.99"), 
                today.plusDays(3), SubscriptionStatus.ACTIVE);
            Subscription sub2 = createSubscription("Spotify", new BigDecimal("9.99"), 
                today.plusDays(5), SubscriptionStatus.ACTIVE);
            
            List<Subscription> expectedResults = List.of(sub1, sub2);
            
            when(subscriptionRepository.findDueSoon(testUser, today, endDate))
                .thenReturn(expectedResults);
            
            // Act
            List<Subscription> results = subscriptionDetectorService.findDueSoon(testUser.getId(), 7);
            
            // Assert
            assertThat(results).hasSize(2);
            assertThat(results).containsExactlyInAnyOrder(sub1, sub2);
        }
        
        @Test
        @DisplayName("Should return subscriptions due today")
        void shouldReturnSubscriptionsDueToday() {
            // Arrange
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(7);
            
            Subscription sub = createSubscription("Amazon Prime", new BigDecimal("12.99"), 
                today, SubscriptionStatus.ACTIVE);
            
            when(subscriptionRepository.findDueSoon(testUser, today, endDate))
                .thenReturn(List.of(sub));
            
            // Act
            List<Subscription> results = subscriptionDetectorService.findDueSoon(testUser.getId(), 7);
            
            // Assert
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getNextDueDate()).isEqualTo(today);
        }
        
        @Test
        @DisplayName("Should NOT return subscriptions due after the specified days")
        void shouldNotReturnSubscriptionsDueAfterSpecifiedDays() {
            // Arrange
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(7);
            
            // Subscription due in 8 days (outside range)
            when(subscriptionRepository.findDueSoon(testUser, today, endDate))
                .thenReturn(List.of());
            
            // Act
            List<Subscription> results = subscriptionDetectorService.findDueSoon(testUser.getId(), 7);
            
            // Assert
            assertThat(results).isEmpty();
        }
        
        @Test
        @DisplayName("Should NOT return ignored subscriptions")
        void shouldNotReturnIgnoredSubscriptions() {
            // Arrange
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(7);
            
            // Only ACTIVE subscriptions should be returned by repository
            Subscription activeSub = createSubscription("Netflix", new BigDecimal("15.99"), 
                today.plusDays(3), SubscriptionStatus.ACTIVE);
            
            when(subscriptionRepository.findDueSoon(testUser, today, endDate))
                .thenReturn(List.of(activeSub));
            
            // Act
            List<Subscription> results = subscriptionDetectorService.findDueSoon(testUser.getId(), 7);
            
            // Assert
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }
        
        @Test
        @DisplayName("Should work with different day ranges")
        void shouldWorkWithDifferentDayRanges() {
            // Arrange
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(30);
            
            Subscription sub1 = createSubscription("Netflix", new BigDecimal("15.99"), 
                today.plusDays(15), SubscriptionStatus.ACTIVE);
            Subscription sub2 = createSubscription("Spotify", new BigDecimal("9.99"), 
                today.plusDays(25), SubscriptionStatus.ACTIVE);
            
            when(subscriptionRepository.findDueSoon(testUser, today, endDate))
                .thenReturn(List.of(sub1, sub2));
            
            // Act
            List<Subscription> results = subscriptionDetectorService.findDueSoon(testUser.getId(), 30);
            
            // Assert
            assertThat(results).hasSize(2);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle user with no transactions")
        void shouldHandleUserWithNoTransactions() {
            // Arrange
            when(transactionRepository.findByUser(testUser)).thenReturn(List.of());
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).isEmpty();
        }
        
        @Test
        @DisplayName("Should handle user with only 1 transaction")
        void shouldHandleUserWithOnly1Transaction() {
            // Arrange
            List<Transaction> transactions = List.of(
                createTransaction("Netflix", new BigDecimal("15.99"), LocalDateTime.now())
            );
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).isEmpty();
        }
        
        @Test
        @DisplayName("Should only process EXPENSE transactions")
        void shouldOnlyProcessExpenseTransactions() {
            // Arrange
            String merchant = "Salary";
            BigDecimal amount = new BigDecimal("3000.00");
            
            LocalDateTime firstDate = LocalDateTime.now().minusDays(60);
            LocalDateTime secondDate = firstDate.plusDays(30);
            LocalDateTime thirdDate = secondDate.plusDays(30);
            
            List<Transaction> transactions = new ArrayList<>();
            Transaction income1 = createTransaction(merchant, amount, firstDate);
            income1.setType("INCOME");
            Transaction income2 = createTransaction(merchant, amount, secondDate);
            income2.setType("INCOME");
            Transaction income3 = createTransaction(merchant, amount, thirdDate);
            income3.setType("INCOME");
            
            transactions.add(income1);
            transactions.add(income2);
            transactions.add(income3);
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).isEmpty();
        }
        
        @Test
        @DisplayName("Should handle transactions with null descriptions")
        void shouldHandleTransactionsWithNullDescriptions() {
            // Arrange
            LocalDateTime firstDate = LocalDateTime.now().minusDays(60);
            LocalDateTime secondDate = firstDate.plusDays(30);
            LocalDateTime thirdDate = secondDate.plusDays(30);
            
            List<Transaction> transactions = new ArrayList<>();
            Transaction txn1 = createTransaction("Netflix", new BigDecimal("15.99"), firstDate);
            txn1.setDescription(null);
            Transaction txn2 = createTransaction("Netflix", new BigDecimal("15.99"), secondDate);
            txn2.setDescription(null);
            Transaction txn3 = createTransaction("Netflix", new BigDecimal("15.99"), thirdDate);
            txn3.setDescription(null);
            
            transactions.add(txn1);
            transactions.add(txn2);
            transactions.add(txn3);
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).isEmpty();
        }
        
        @Test
        @DisplayName("Should detect multiple subscriptions for different merchants")
        void shouldDetectMultipleSubscriptionsForDifferentMerchants() {
            // Arrange
            LocalDateTime baseDate = LocalDateTime.now().minusDays(60);
            
            List<Transaction> transactions = new ArrayList<>();
            
            // Netflix subscription
            transactions.add(createTransaction("Netflix", new BigDecimal("15.99"), baseDate));
            transactions.add(createTransaction("Netflix", new BigDecimal("15.99"), baseDate.plusDays(30)));
            transactions.add(createTransaction("Netflix", new BigDecimal("15.99"), baseDate.plusDays(60)));
            
            // Spotify subscription
            transactions.add(createTransaction("Spotify", new BigDecimal("9.99"), baseDate));
            transactions.add(createTransaction("Spotify", new BigDecimal("9.99"), baseDate.plusDays(30)));
            transactions.add(createTransaction("Spotify", new BigDecimal("9.99"), baseDate.plusDays(60)));
            
            when(transactionRepository.findByUser(testUser)).thenReturn(transactions);
            when(subscriptionRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(testUser.getId());
            
            // Assert
            assertThat(subscriptions).hasSize(2);
            List<String> merchants = subscriptions.stream()
                .map(Subscription::getMerchant)
                .toList();
            assertThat(merchants).containsExactlyInAnyOrder("Netflix", "Spotify");
        }
    }
    
    // Helper method to create a subscription
    private Subscription createSubscription(String merchant, BigDecimal amount, 
                                           LocalDate nextDueDate, SubscriptionStatus status) {
        Subscription subscription = new Subscription();
        subscription.setUser(testUser);
        subscription.setMerchant(merchant);
        subscription.setAvgAmount(amount);
        subscription.setLastPaidDate(nextDueDate.minusDays(30));
        subscription.setNextDueDate(nextDueDate);
        subscription.setStatus(status);
        subscription.setCreatedAt(LocalDateTime.now());
        return subscription;
    }
}
