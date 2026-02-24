package com.example.FinSight.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.FinSight.model.Subscription;
import com.example.FinSight.model.SubscriptionStatus;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.SubscriptionRepository;
import com.example.FinSight.repository.TransactionRepository;
import com.example.FinSight.repository.UserRepository;

@Service
public class SubscriptionDetectorService {
    
    private final TransactionRepository transactionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    
    public SubscriptionDetectorService(TransactionRepository transactionRepository,
                                      SubscriptionRepository subscriptionRepository,
                                      UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Detects subscriptions from user's transaction history.
     * Groups by merchant, finds recurring patterns (25-35 days apart).
     * 
     * @param userId The user to analyze
     * @return List of detected subscriptions
     */
    public List<Subscription> detectSubscriptions(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Fetch all expense transactions for user
        List<Transaction> expenses = transactionRepository.findByUser(user).stream()
            .filter(t -> "EXPENSE".equals(t.getType()))
            .collect(Collectors.toList());
        
        // Group by normalized merchant
        Map<String, List<Transaction>> byMerchant = groupByMerchant(expenses);
        
        List<Subscription> subscriptions = new ArrayList<>();
        
        // Analyze each merchant group for recurring patterns
        for (Map.Entry<String, List<Transaction>> entry : byMerchant.entrySet()) {
            String normalizedMerchant = entry.getKey();
            List<Transaction> txns = entry.getValue();
            
            // Need at least 2 transactions to detect a pattern
            if (txns.size() < 2) {
                continue;
            }
            
            // Sort by date
            txns.sort(Comparator.comparing(Transaction::getTransactionDate));
            
            // Count qualifying recurring patterns (25-35 days apart)
            int recurringCount = 0;
            for (int i = 1; i < txns.size(); i++) {
                long daysBetween = ChronoUnit.DAYS.between(
                    txns.get(i - 1).getTransactionDate(),
                    txns.get(i).getTransactionDate()
                );
                if (daysBetween >= 25 && daysBetween <= 35) {
                    recurringCount++;
                }
            }
            
            // Require at least 2 qualifying occurrences
            if (recurringCount >= 2) {
                Subscription sub = createSubscription(user, normalizedMerchant, txns);
                subscriptions.add(sub);
            }
        }
        
        return subscriptionRepository.saveAll(subscriptions);
    }
    
    /**
     * Finds subscriptions due within specified days.
     * 
     * @param userId The user
     * @param days Number of days to look ahead
     * @return List of due-soon subscriptions
     */
    public List<Subscription> findDueSoon(Long userId, int days) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        return subscriptionRepository.findDueSoon(user, today, endDate);
    }
    
    /**
     * Normalizes merchant name for matching.
     * Converts to lowercase and removes special characters.
     */
    private String normalizeMerchant(String merchant) {
        if (merchant == null) {
            return "";
        }
        return merchant.toLowerCase()
            .replaceAll("[^a-z0-9]", "")
            .trim();
    }
    
    /**
     * Groups transactions by normalized merchant.
     */
    private Map<String, List<Transaction>> groupByMerchant(List<Transaction> transactions) {
        return transactions.stream()
            .filter(t -> t.getDescription() != null)
            .collect(Collectors.groupingBy(t -> normalizeMerchant(t.getDescription())));
    }
    
    /**
     * Creates a Subscription entity from a list of recurring transactions.
     */
    private Subscription createSubscription(User user, String normalizedMerchant, List<Transaction> txns) {
        Subscription sub = new Subscription();
        sub.setUser(user);
        
        // Use the original merchant name from the most recent transaction
        sub.setMerchant(txns.get(txns.size() - 1).getDescription());
        
        // Calculate average amount
        BigDecimal totalAmount = txns.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgAmount = totalAmount.divide(
            BigDecimal.valueOf(txns.size()), 
            2, 
            RoundingMode.HALF_UP
        );
        sub.setAvgAmount(avgAmount);
        
        // Last payment date
        Transaction lastTxn = txns.get(txns.size() - 1);
        sub.setLastPaidDate(lastTxn.getTransactionDate().toLocalDate());
        
        // Next due date (last + 30 days)
        sub.setNextDueDate(lastTxn.getTransactionDate().toLocalDate().plusDays(30));
        
        // Status
        sub.setStatus(SubscriptionStatus.ACTIVE);
        
        // Created timestamp
        sub.setCreatedAt(LocalDateTime.now());
        
        return sub;
    }
}
