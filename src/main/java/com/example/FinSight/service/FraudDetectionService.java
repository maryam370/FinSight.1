package com.example.FinSight.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.FinSight.model.RiskLevel;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {
    private final TransactionRepository transactionRepository;

    /**
     * Analyzes a transaction and computes fraud score using rule-based algorithm.
     * 
     * Rules:
     * - High amount anomaly (>3x avg): +30 points
     * - Rapid-fire activity (5+ in 10 min): +25 points
     * - Geographical anomaly (different location < 2 hours): +25 points
     * - Unusual category (never used): +20 points
     * 
     * @param transaction The transaction to analyze
     * @return FraudDetectionResult with score, risk level, and reasons
     */
    public FraudDetectionResult analyzeTransaction(Transaction transaction) {
        double score = 0.0;
        List<String> reasons = new ArrayList<>();
        
        if (transaction == null || transaction.getUser() == null) {
            return new FraudDetectionResult(false, 0.0, RiskLevel.LOW, reasons);
        }
        
        User user = transaction.getUser();
        
        // Rule 1: High Amount Anomaly (>3x average) adds 30 points
        try {
            if (hasHighAmountAnomaly(user, transaction.getAmount())) {
                score += 30;
                reasons.add("Amount exceeds 3x user average");
            }
        } catch (Exception e) {
            // Log but continue with other rules
            System.err.println("Error in high amount anomaly check: " + e.getMessage());
        }
        
        // Rule 2: Rapid-Fire Activity (5+ in 10 min) adds 25 points
        try {
            if (transaction.getTransactionDate() != null && hasRapidFireActivity(user, transaction.getTransactionDate())) {
                score += 25;
                reasons.add("5+ transactions in 10 minutes");
            }
        } catch (Exception e) {
            System.err.println("Error in rapid-fire activity check: " + e.getMessage());
        }
        
        // Rule 3: Geographical Anomaly (different location < 2 hours) adds 25 points
        try {
            if (transaction.getTransactionDate() != null && hasGeographicalAnomaly(user, transaction.getLocation(), transaction.getTransactionDate())) {
                score += 25;
                reasons.add("Different location within 2 hours");
            }
        } catch (Exception e) {
            System.err.println("Error in geographical anomaly check: " + e.getMessage());
        }
        
        // Rule 4: Unusual Category (never used) adds 20 points
        try {
            if (transaction.getCategory() != null && isUnusualCategory(user, transaction.getCategory())) {
                score += 20;
                reasons.add("New category for user");
            }
        } catch (Exception e) {
            System.err.println("Error in unusual category check: " + e.getMessage());
        }
        
        RiskLevel riskLevel = calculateRiskLevel(score);
        boolean fraudulent = score >= 70;
        
        return new FraudDetectionResult(fraudulent, score, riskLevel, reasons);
    }
    
    /**
     * Rule 1: Checks if transaction amount exceeds 3x user's average.
     */
    private boolean hasHighAmountAnomaly(User user, BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        
        BigDecimal userAvg = calculateUserAverage(user);
        if (userAvg == null || userAvg.compareTo(BigDecimal.ZERO) == 0) {
            return false; // No average to compare against
        }
        
        BigDecimal threshold = userAvg.multiply(BigDecimal.valueOf(3));
        return amount.compareTo(threshold) > 0;
    }
    
    /**
     * Calculates user's average transaction amount.
     */
    private BigDecimal calculateUserAverage(User user) {
        if (user == null) {
            return null;
        }
        try {
            return transactionRepository.calculateAverageAmount(user);
        } catch (Exception e) {
            System.err.println("Error calculating average amount: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Rule 2: Checks for rapid-fire transactions (5+ in 10 minutes).
     */
    private boolean hasRapidFireActivity(User user, LocalDateTime transactionTime) {
        if (user == null || transactionTime == null) {
            return false;
        }
        
        try {
            LocalDateTime tenMinutesAgo = transactionTime.minusMinutes(10);
            long recentCount = transactionRepository.countByUserAndTransactionDateBetween(
                user, tenMinutesAgo, transactionTime
            );
            return recentCount >= 5;
        } catch (Exception e) {
            System.err.println("Error checking rapid-fire activity: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Rule 3: Checks for geographical anomalies (different location within 2 hours).
     */
    private boolean hasGeographicalAnomaly(User user, String location, LocalDateTime transactionTime) {
        if (user == null || location == null || location.trim().isEmpty() || transactionTime == null) {
            return false; // No location to compare
        }
        
        try {
            Optional<Transaction> lastTransaction = transactionRepository.findTopByUserOrderByTransactionDateDesc(user);
            if (lastTransaction.isEmpty()) {
                return false; // No previous transaction to compare
            }
            
            Transaction last = lastTransaction.get();
            if (last.getLocation() == null || last.getLocation().trim().isEmpty()) {
                return false; // No previous location to compare
            }
            
            if (last.getTransactionDate() == null) {
                return false;
            }
            
            long hoursBetween = ChronoUnit.HOURS.between(last.getTransactionDate(), transactionTime);
            
            // Different location AND less than 2 hours apart
            return hoursBetween < 2 && !location.equalsIgnoreCase(last.getLocation());
        } catch (Exception e) {
            System.err.println("Error checking geographical anomaly: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Rule 4: Checks if category is new for user.
     */
    private boolean isUnusualCategory(User user, String category) {
        if (user == null || category == null || category.trim().isEmpty()) {
            return false;
        }
        
        try {
            List<String> userCategories = transactionRepository.findDistinctCategoriesByUser(user);
            if (userCategories == null) {
                return false;
            }
            return !userCategories.contains(category);
        } catch (Exception e) {
            System.err.println("Error checking unusual category: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Converts fraud score to risk level.
     * LOW: 0-39
     * MEDIUM: 40-69
     * HIGH: 70-100
     */
    private RiskLevel calculateRiskLevel(double score) {
        if (score >= 70) {
            return RiskLevel.HIGH;
        } else if (score >= 40) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }
}