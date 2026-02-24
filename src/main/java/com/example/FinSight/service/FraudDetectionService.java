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
        
        User user = transaction.getUser();
        
        // Rule 1: High Amount Anomaly (>3x average) adds 30 points
        if (hasHighAmountAnomaly(user, transaction.getAmount())) {
            score += 30;
            reasons.add("Amount exceeds 3x user average");
        }
        
        // Rule 2: Rapid-Fire Activity (5+ in 10 min) adds 25 points
        if (hasRapidFireActivity(user, transaction.getTransactionDate())) {
            score += 25;
            reasons.add("5+ transactions in 10 minutes");
        }
        
        // Rule 3: Geographical Anomaly (different location < 2 hours) adds 25 points
        if (hasGeographicalAnomaly(user, transaction.getLocation(), transaction.getTransactionDate())) {
            score += 25;
            reasons.add("Different location within 2 hours");
        }
        
        // Rule 4: Unusual Category (never used) adds 20 points
        if (isUnusualCategory(user, transaction.getCategory())) {
            score += 20;
            reasons.add("New category for user");
        }
        
        RiskLevel riskLevel = calculateRiskLevel(score);
        boolean fraudulent = score >= 70;
        
        return new FraudDetectionResult(fraudulent, score, riskLevel, reasons);
    }
    
    /**
     * Rule 1: Checks if transaction amount exceeds 3x user's average.
     */
    private boolean hasHighAmountAnomaly(User user, BigDecimal amount) {
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
        return transactionRepository.calculateAverageAmount(user);
    }
    
    /**
     * Rule 2: Checks for rapid-fire transactions (5+ in 10 minutes).
     */
    private boolean hasRapidFireActivity(User user, LocalDateTime transactionTime) {
        LocalDateTime tenMinutesAgo = transactionTime.minusMinutes(10);
        long recentCount = transactionRepository.countByUserAndTransactionDateBetween(
            user, tenMinutesAgo, transactionTime
        );
        return recentCount >= 5;
    }
    
    /**
     * Rule 3: Checks for geographical anomalies (different location within 2 hours).
     */
    private boolean hasGeographicalAnomaly(User user, String location, LocalDateTime transactionTime) {
        if (location == null || location.trim().isEmpty()) {
            return false; // No location to compare
        }
        
        Optional<Transaction> lastTransaction = transactionRepository.findTopByUserOrderByTransactionDateDesc(user);
        if (lastTransaction.isEmpty()) {
            return false; // No previous transaction to compare
        }
        
        Transaction last = lastTransaction.get();
        if (last.getLocation() == null || last.getLocation().trim().isEmpty()) {
            return false; // No previous location to compare
        }
        
        long hoursBetween = ChronoUnit.HOURS.between(last.getTransactionDate(), transactionTime);
        
        // Different location AND less than 2 hours apart
        return hoursBetween < 2 && !location.equalsIgnoreCase(last.getLocation());
    }
    
    /**
     * Rule 4: Checks if category is new for user.
     */
    private boolean isUnusualCategory(User user, String category) {
        List<String> userCategories = transactionRepository.findDistinctCategoriesByUser(user);
        return !userCategories.contains(category);
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