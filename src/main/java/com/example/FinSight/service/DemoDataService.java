package com.example.FinSight.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.TransactionRepository;
import com.example.FinSight.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DemoDataService {
    
    private static final Logger log = LoggerFactory.getLogger(DemoDataService.class);
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final FraudDetectionService fraudDetectionService;
    
    /**
     * Seeds demo transactions for a user if they have zero transactions.
     * Uses deterministic random generation based on userId.
     * 
     * @param userId The user to seed data for
     * @return Number of transactions created
     */
    @Transactional
    public int seedUserIfEmpty(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        Long count = transactionRepository.countByUser(user);
        
        if (count > 0) {
            log.info("User {} already has {} transactions, skipping demo data seeding", userId, count);
            return 0;
        }
        
        // Deterministic seed based on userId
        Random random = new Random(userId.hashCode());
        
        List<Transaction> demoTransactions = generateDemoTransactions(user, random);
        
        // Run fraud detection on each transaction
        for (Transaction txn : demoTransactions) {
            FraudDetectionResult result = fraudDetectionService.analyzeTransaction(txn);
            txn.setFraudulent(result.isFraudulent());
            txn.setFraudScore(result.getFraudScore());
        }
        
        transactionRepository.saveAll(demoTransactions);
    
        
        log.info("Generated {} demo transactions for user {}", demoTransactions.size(), userId);
        
        return demoTransactions.size();
    }
    
    /**
     * Generates a deterministic set of demo transactions.
     * 
     * @param user The user entity
     * @param random Seeded random generator
     * @return List of generated transactions
     */
    private List<Transaction> generateDemoTransactions(User user, Random random) {
        List<Transaction> transactions = new ArrayList<>();
        
        // Generate 25-50 transactions
        int count = 25 + random.nextInt(26);
        
        // Distribute across 60-90 days
        int daysBack = 60 + random.nextInt(31);
        LocalDateTime startDate = LocalDateTime.now().minusDays(daysBack);
        
        // Category distribution: 40% groceries, 15% utilities, 15% entertainment, 
        // 10% transport, 10% subscriptions, 5% salary, 5% rent
        String[] categories = {
            "groceries", "groceries", "groceries", "groceries",  // 40%
            "utilities", "utilities", "entertainment", "entertainment",  // 15% each
            "transport", "subscriptions",  // 10% each
            "salary", "rent"  // 5% each
        };
        
        for (int i = 0; i < count; i++) {
            Transaction txn = new Transaction();
            txn.setUser(user);
            
            // Random date within range
            long randomDays = random.nextInt(daysBack);
            long randomHours = random.nextInt(24);
            long randomMinutes = random.nextInt(60);
            txn.setTransactionDate(startDate.plusDays(randomDays).plusHours(randomHours).plusMinutes(randomMinutes));
            
            // Random category
            String category = categories[random.nextInt(categories.length)];
            txn.setCategory(category);
            
            // Generate amount based on category
            BigDecimal amount = generateAmountForCategory(category, random);
            txn.setAmount(amount);
            
            // Set type based on category
            txn.setType(category.equals("salary") ? "INCOME" : "EXPENSE");
            
            txn.setDescription("Demo " + category);
            txn.setLocation("Demo Location " + (random.nextInt(5) + 1));
            txn.setCreatedAt(LocalDateTime.now());
            
            transactions.add(txn);
        }
        
        // Add fraud triggers
        addFraudTriggers(transactions, random);
        
        return transactions;
    }
    
    /**
     * Generates realistic amounts based on category.
     */
    private BigDecimal generateAmountForCategory(String category, Random random) {
        return switch (category) {
            case "groceries" -> BigDecimal.valueOf(20 + random.nextInt(131)); // $20-$150
            case "utilities" -> BigDecimal.valueOf(50 + random.nextInt(251)); // $50-$300
            case "entertainment" -> BigDecimal.valueOf(10 + random.nextInt(91)); // $10-$100
            case "salary" -> BigDecimal.valueOf(2000 + random.nextInt(3001)); // $2000-$5000
            case "rent" -> BigDecimal.valueOf(800 + random.nextInt(1201)); // $800-$2000
            case "subscriptions" -> BigDecimal.valueOf(5 + random.nextInt(46)); // $5-$50
            case "transport" -> BigDecimal.valueOf(10 + random.nextInt(71)); // $10-$80
            default -> BigDecimal.valueOf(50 + random.nextInt(101)); // $50-$150
        };
    }
    
    /**
     * Adds fraud triggers to the transaction set:
     * - 1-2 transactions with amounts > 3x average
     * - 1 cluster of 5+ transactions within 10 minutes
     * - 1-2 transactions with unusual categories
     */
    private void addFraudTriggers(List<Transaction> transactions, Random random) {
        if (transactions.isEmpty()) {
            return;
        }
        
        // Calculate average amount
        BigDecimal avgAmount = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(transactions.size()), 2, java.math.RoundingMode.HALF_UP);
        
        // Trigger 1: Add 1-2 high amount transactions (>3x average)
        int highAmountCount = 1 + random.nextInt(2);
        for (int i = 0; i < highAmountCount && i < transactions.size(); i++) {
            Transaction txn = transactions.get(random.nextInt(transactions.size()));
            BigDecimal highAmount = avgAmount.multiply(BigDecimal.valueOf(3.5 + random.nextDouble()));
            txn.setAmount(highAmount);
        }
        
        // Trigger 2: Create a cluster of 5+ transactions within 10 minutes (rapid-fire)
        if (transactions.size() >= 5) {
            LocalDateTime clusterTime = LocalDateTime.now().minusDays(random.nextInt(30));
            for (int i = 0; i < 5; i++) {
                Transaction txn = transactions.get(i);
                txn.setTransactionDate(clusterTime.plusMinutes(i * 2)); // 2 minutes apart
            }
        }
        
        // Trigger 3: Add 1-2 transactions with unusual categories
        String[] unusualCategories = {"jewelry", "casino", "crypto"};
        int unusualCount = 1 + random.nextInt(2);
        for (int i = 0; i < unusualCount && i < transactions.size(); i++) {
            Transaction txn = transactions.get(random.nextInt(transactions.size()));
            txn.setCategory(unusualCategories[random.nextInt(unusualCategories.length)]);
            txn.setAmount(BigDecimal.valueOf(100 + random.nextInt(401))); // $100-$500
            txn.setDescription("Demo " + txn.getCategory());
        }
    }
}
