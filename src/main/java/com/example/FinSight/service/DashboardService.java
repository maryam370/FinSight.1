package com.example.FinSight.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.FinSight.dto.DashboardSummary;
import com.example.FinSight.dto.TimeSeriesPoint;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.TransactionRepository;
import com.example.FinSight.repository.UserRepository;

@Service
public class DashboardService {
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public DashboardService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generates dashboard summary with aggregated metrics.
     * 
     * @param userId The user
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return DashboardSummary with all metrics
     */
    public DashboardSummary getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get all transactions for the user
        List<Transaction> allTransactions = transactionRepository.findByUser(user);
        
        // Filter by date range if provided
        List<Transaction> transactions = filterByDateRange(allTransactions, startDate, endDate);
        
        // Calculate financial metrics
        BigDecimal totalIncome = calculateTotalIncome(transactions);
        BigDecimal totalExpenses = calculateTotalExpenses(transactions);
        BigDecimal currentBalance = totalIncome.subtract(totalExpenses);
        
        // Calculate fraud metrics
        Long totalFlaggedTransactions = countFlaggedTransactions(transactions);
        Double averageFraudScore = calculateAverageFraudScore(transactions);
        
        // Calculate aggregations
        Map<String, BigDecimal> spendingByCategory = getSpendingByCategory(transactions);
        Map<String, Long> fraudByCategory = getFraudByCategory(transactions);
        List<TimeSeriesPoint> spendingTrends = getSpendingTrends(transactions);
        
        return new DashboardSummary(
            totalIncome,
            totalExpenses,
            currentBalance,
            totalFlaggedTransactions,
            averageFraudScore,
            spendingByCategory,
            fraudByCategory,
            spendingTrends
        );
    }

    private List<Transaction> filterByDateRange(List<Transaction> transactions, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return transactions;
        }
        
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.MAX;
        
        return transactions.stream()
            .filter(t -> !t.getTransactionDate().isBefore(start) && !t.getTransactionDate().isAfter(end))
            .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalIncome(List<Transaction> transactions) {
        return transactions.stream()
            .filter(t -> "INCOME".equals(t.getType()))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalExpenses(List<Transaction> transactions) {
        return transactions.stream()
            .filter(t -> "EXPENSE".equals(t.getType()))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long countFlaggedTransactions(List<Transaction> transactions) {
        return transactions.stream()
            .filter(Transaction::isFraudulent)
            .count();
    }

    private Double calculateAverageFraudScore(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return 0.0;
        }
        
        double sum = transactions.stream()
            .filter(t -> t.getFraudScore() != null)
            .mapToDouble(Transaction::getFraudScore)
            .sum();
        
        long count = transactions.stream()
            .filter(t -> t.getFraudScore() != null)
            .count();
        
        if (count == 0) {
            return 0.0;
        }
        
        return BigDecimal.valueOf(sum / count)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    /**
     * Calculates spending by category.
     */
    private Map<String, BigDecimal> getSpendingByCategory(List<Transaction> transactions) {
        Map<String, BigDecimal> spendingByCategory = new HashMap<>();
        
        transactions.stream()
            .filter(t -> "EXPENSE".equals(t.getType()))
            .forEach(t -> {
                String category = t.getCategory();
                BigDecimal amount = t.getAmount();
                spendingByCategory.merge(category, amount, BigDecimal::add);
            });
        
        return spendingByCategory;
    }

    /**
     * Calculates fraud incidents by category.
     */
    private Map<String, Long> getFraudByCategory(List<Transaction> transactions) {
        return transactions.stream()
            .filter(Transaction::isFraudulent)
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.counting()
            ));
    }

    /**
     * Calculates spending trends over time.
     */
    private List<TimeSeriesPoint> getSpendingTrends(List<Transaction> transactions) {
        Map<LocalDate, BigDecimal> trendMap = new HashMap<>();
        
        transactions.stream()
            .filter(t -> "EXPENSE".equals(t.getType()))
            .forEach(t -> {
                LocalDate date = t.getTransactionDate().toLocalDate();
                BigDecimal amount = t.getAmount();
                trendMap.merge(date, amount, BigDecimal::add);
            });
        
        return trendMap.entrySet().stream()
            .map(entry -> new TimeSeriesPoint(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
            .collect(Collectors.toList());
    }
}
