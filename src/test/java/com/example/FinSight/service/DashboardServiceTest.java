package com.example.FinSight.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.FinSight.dto.DashboardSummary;
import com.example.FinSight.dto.TimeSeriesPoint;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.TransactionRepository;
import com.example.FinSight.repository.UserRepository;

@SpringBootTest
@DisplayName("DashboardService Unit Tests")
public class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setup() {
        transactionRepository.deleteAll();
        userRepository.deleteAll();
        
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);
    }

    @Nested
    @DisplayName("Income/Expense/Balance Calculations Tests")
    class FinancialCalculationsTests {

        @Test
        @DisplayName("Should calculate total income correctly")
        void shouldCalculateTotalIncome() {
            // Create income transactions
            createTransaction("INCOME", "salary", new BigDecimal("5000.00"), 10);
            createTransaction("INCOME", "bonus", new BigDecimal("1000.00"), 5);
            createTransaction("EXPENSE", "groceries", new BigDecimal("200.00"), 3);

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            assertThat(summary.getTotalIncome()).isEqualByComparingTo(new BigDecimal("6000.00"));
        }

        @Test
        @DisplayName("Should calculate total expenses correctly")
        void shouldCalculateTotalExpenses() {
            // Create expense transactions
            createTransaction("EXPENSE", "groceries", new BigDecimal("200.00"), 10);
            createTransaction("EXPENSE", "utilities", new BigDecimal("150.00"), 5);
            createTransaction("INCOME", "salary", new BigDecimal("5000.00"), 3);

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            assertThat(summary.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("350.00"));
        }

        @Test
        @DisplayName("Should calculate current balance correctly")
        void shouldCalculateCurrentBalance() {
            // Create mixed transactions
            createTransaction("INCOME", "salary", new BigDecimal("5000.00"), 10);
            createTransaction("EXPENSE", "rent", new BigDecimal("1500.00"), 8);
            createTransaction("EXPENSE", "groceries", new BigDecimal("300.00"), 5);

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            assertThat(summary.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("3200.00"));
        }

        @Test
        @DisplayName("Should handle zero transactions")
        void shouldHandleZeroTransactions() {
            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.getCurrentBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Category Aggregations Tests")
    class CategoryAggregationsTests {

        @Test
        @DisplayName("Should aggregate spending by category")
        void shouldAggregateSpendingByCategory() {
            createTransaction("EXPENSE", "groceries", new BigDecimal("100.00"), 10);
            createTransaction("EXPENSE", "groceries", new BigDecimal("150.00"), 8);
            createTransaction("EXPENSE", "utilities", new BigDecimal("200.00"), 5);
            createTransaction("INCOME", "salary", new BigDecimal("5000.00"), 3);

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            Map<String, BigDecimal> spendingByCategory = summary.getSpendingByCategory();
            assertThat(spendingByCategory.get("groceries")).isEqualByComparingTo(new BigDecimal("250.00"));
            assertThat(spendingByCategory.get("utilities")).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(spendingByCategory.containsKey("salary")).isFalse(); // Income should not be in spending
        }

        @Test
        @DisplayName("Should aggregate fraud incidents by category")
        void shouldAggregateFraudByCategory() {
            createFraudulentTransaction("EXPENSE", "groceries", new BigDecimal("100.00"), 10, 75.0);
            createFraudulentTransaction("EXPENSE", "groceries", new BigDecimal("150.00"), 8, 80.0);
            createFraudulentTransaction("EXPENSE", "utilities", new BigDecimal("200.00"), 5, 85.0);
            createTransaction("EXPENSE", "entertainment", new BigDecimal("50.00"), 3);

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            Map<String, Long> fraudByCategory = summary.getFraudByCategory();
            assertThat(fraudByCategory.get("groceries")).isEqualTo(2L);
            assertThat(fraudByCategory.get("utilities")).isEqualTo(1L);
            assertThat(fraudByCategory.containsKey("entertainment")).isFalse();
        }

        @Test
        @DisplayName("Should handle empty category aggregations")
        void shouldHandleEmptyCategoryAggregations() {
            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            assertThat(summary.getSpendingByCategory()).isEmpty();
            assertThat(summary.getFraudByCategory()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Fraud Metrics Tests")
    class FraudMetricsTests {

        @Test
        @DisplayName("Should count total flagged transactions")
        void shouldCountTotalFlaggedTransactions() {
            createFraudulentTransaction("EXPENSE", "groceries", new BigDecimal("100.00"), 10, 75.0);
            createFraudulentTransaction("EXPENSE", "utilities", new BigDecimal("200.00"), 8, 80.0);
            createTransaction("EXPENSE", "entertainment", new BigDecimal("50.00"), 5);

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            assertThat(summary.getTotalFlaggedTransactions()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should calculate average fraud score")
        void shouldCalculateAverageFraudScore() {
            createTransactionWithScore("EXPENSE", "groceries", new BigDecimal("100.00"), 10, 60.0);
            createTransactionWithScore("EXPENSE", "utilities", new BigDecimal("200.00"), 8, 80.0);
            createTransactionWithScore("EXPENSE", "entertainment", new BigDecimal("50.00"), 5, 70.0);

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            // Average: (60 + 80 + 70) / 3 = 70.0
            assertThat(summary.getAverageFraudScore()).isEqualTo(70.0);
        }

        @Test
        @DisplayName("Should handle zero fraud score when no transactions")
        void shouldHandleZeroFraudScoreWhenNoTransactions() {
            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            assertThat(summary.getTotalFlaggedTransactions()).isEqualTo(0L);
            assertThat(summary.getAverageFraudScore()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Date Range Filtering Tests")
    class DateRangeFilteringTests {

        @Test
        @DisplayName("Should filter transactions by date range")
        void shouldFilterTransactionsByDateRange() {
            createTransaction("INCOME", "salary", new BigDecimal("5000.00"), 30);
            createTransaction("EXPENSE", "groceries", new BigDecimal("200.00"), 15);
            createTransaction("EXPENSE", "utilities", new BigDecimal("150.00"), 5);

            LocalDate startDate = LocalDate.now().minusDays(20);
            LocalDate endDate = LocalDate.now().minusDays(10);

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), startDate, endDate);

            // Only the transaction from 15 days ago should be included
            assertThat(summary.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should include transactions on boundary dates")
        void shouldIncludeTransactionsOnBoundaryDates() {
            LocalDate today = LocalDate.now();
            createTransactionOnDate("EXPENSE", "groceries", new BigDecimal("100.00"), today.minusDays(10));
            createTransactionOnDate("EXPENSE", "utilities", new BigDecimal("150.00"), today.minusDays(5));

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), today.minusDays(10), today.minusDays(5));

            assertThat(summary.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("250.00"));
        }

        @Test
        @DisplayName("Should return all transactions when no date range specified")
        void shouldReturnAllTransactionsWhenNoDateRange() {
            createTransaction("EXPENSE", "groceries", new BigDecimal("100.00"), 30);
            createTransaction("EXPENSE", "utilities", new BigDecimal("150.00"), 15);
            createTransaction("EXPENSE", "entertainment", new BigDecimal("50.00"), 5);

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            assertThat(summary.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("300.00"));
        }
    }

    @Nested
    @DisplayName("Spending Trends Tests")
    class SpendingTrendsTests {

        @Test
        @DisplayName("Should aggregate spending trends by date")
        void shouldAggregateSpendingTrendsByDate() {
            LocalDate today = LocalDate.now();
            createTransactionOnDate("EXPENSE", "groceries", new BigDecimal("100.00"), today.minusDays(2));
            createTransactionOnDate("EXPENSE", "utilities", new BigDecimal("150.00"), today.minusDays(2));
            createTransactionOnDate("EXPENSE", "entertainment", new BigDecimal("50.00"), today.minusDays(1));

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            List<TimeSeriesPoint> trends = summary.getSpendingTrends();
            assertThat(trends).hasSize(2);
            
            // Find the point for 2 days ago
            TimeSeriesPoint point1 = trends.stream()
                .filter(p -> p.getDate().equals(today.minusDays(2)))
                .findFirst()
                .orElseThrow();
            assertThat(point1.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
            
            // Find the point for 1 day ago
            TimeSeriesPoint point2 = trends.stream()
                .filter(p -> p.getDate().equals(today.minusDays(1)))
                .findFirst()
                .orElseThrow();
            assertThat(point2.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("Should sort spending trends by date")
        void shouldSortSpendingTrendsByDate() {
            LocalDate today = LocalDate.now();
            createTransactionOnDate("EXPENSE", "groceries", new BigDecimal("100.00"), today.minusDays(5));
            createTransactionOnDate("EXPENSE", "utilities", new BigDecimal("150.00"), today.minusDays(10));
            createTransactionOnDate("EXPENSE", "entertainment", new BigDecimal("50.00"), today.minusDays(1));

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            List<TimeSeriesPoint> trends = summary.getSpendingTrends();
            assertThat(trends).hasSize(3);
            
            // Verify sorted order (oldest to newest)
            assertThat(trends.get(0).getDate()).isEqualTo(today.minusDays(10));
            assertThat(trends.get(1).getDate()).isEqualTo(today.minusDays(5));
            assertThat(trends.get(2).getDate()).isEqualTo(today.minusDays(1));
        }

        @Test
        @DisplayName("Should exclude income from spending trends")
        void shouldExcludeIncomeFromSpendingTrends() {
            LocalDate today = LocalDate.now();
            createTransactionOnDate("INCOME", "salary", new BigDecimal("5000.00"), today.minusDays(5));
            createTransactionOnDate("EXPENSE", "groceries", new BigDecimal("100.00"), today.minusDays(5));

            DashboardSummary summary = dashboardService.getSummary(testUser.getId(), null, null);

            List<TimeSeriesPoint> trends = summary.getSpendingTrends();
            assertThat(trends).hasSize(1);
            assertThat(trends.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    // Helper methods
    private void createTransaction(String type, String category, BigDecimal amount, int daysAgo) {
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setTransactionDate(LocalDateTime.now().minusDays(daysAgo));
        transaction.setFraudulent(false);
        transaction.setFraudScore(0.0);
        transaction.setDescription("Test transaction");
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    private void createTransactionOnDate(String type, String category, BigDecimal amount, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setTransactionDate(date.atStartOfDay());
        transaction.setFraudulent(false);
        transaction.setFraudScore(0.0);
        transaction.setDescription("Test transaction");
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    private void createFraudulentTransaction(String type, String category, BigDecimal amount, int daysAgo, double fraudScore) {
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setTransactionDate(LocalDateTime.now().minusDays(daysAgo));
        transaction.setFraudulent(true);
        transaction.setFraudScore(fraudScore);
        transaction.setDescription("Test transaction");
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    private void createTransactionWithScore(String type, String category, BigDecimal amount, int daysAgo, double fraudScore) {
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setTransactionDate(LocalDateTime.now().minusDays(daysAgo));
        transaction.setFraudulent(fraudScore >= 70.0);
        transaction.setFraudScore(fraudScore);
        transaction.setDescription("Test transaction");
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}
