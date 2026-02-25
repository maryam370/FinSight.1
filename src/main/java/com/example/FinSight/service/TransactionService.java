package com.example.FinSight.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FinSight.dto.TransactionRequest;
import com.example.FinSight.dto.TransactionResponse;
import com.example.FinSight.model.FraudAlert;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.FraudAlertRepository;
import com.example.FinSight.repository.TransactionRepository;
import com.example.FinSight.repository.UserRepository;
import com.example.FinSight.specification.TransactionSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final FraudDetectionService fraudDetectionService;

    
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setLocation(request.getLocation());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCreatedAt(LocalDateTime.now());
        
        // Detect fraud
        FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
        transaction.setFraudulent(result.isFraudulent());
        transaction.setFraudScore(result.getFraudScore());
        
        Transaction saved = transactionRepository.save(transaction);
        
        // Create alert if fraud detected
        if (result.isFraudulent() && result.getReasons() != null && !result.getReasons().isEmpty()) {
            FraudAlert alert = new FraudAlert();
            alert.setUser(user);
            alert.setTransaction(saved);
            
            // Build message safely
            String reasonsText = String.join(", ", result.getReasons());
            String message = "Fraud detected: " + reasonsText;
            
            // Ensure message doesn't exceed database column length (if any)
            if (message.length() > 255) {
                message = message.substring(0, 252) + "...";
            }
            
            alert.setMessage(message);
            alert.setSeverity(result.getRiskLevel() != null ? result.getRiskLevel().name() : "MEDIUM");
            alert.setResolved(false);
            alert.setCreatedAt(LocalDateTime.now());
            
            try {
                fraudAlertRepository.save(alert);
            } catch (Exception e) {
                // Log error but don't fail transaction creation
                System.err.println("Failed to create fraud alert: " + e.getMessage());
            }
        }

        return mapToResponse(saved, result);
    }
    
    public List<TransactionResponse> getUserTransactions(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return transactionRepository.findByUserOrderByTransactionDateDesc(user)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<TransactionResponse> getFraudulentTransactions(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return transactionRepository.findByUserAndFraudulentTrue(user)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public Page<TransactionResponse> findWithFilters(Long userId, Specification<Transaction> spec, Pageable pageable) {
        return transactionRepository.findAll(spec, pageable)
            .map(this::mapToResponse);
    }
    
    public Page<TransactionResponse> findWithFilters(
            Long userId,
            String type,
            String category,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Boolean fraudulent,
            Pageable pageable) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Specification<Transaction> spec = TransactionSpecification.withFilters(
            user, type, category, startDate, endDate, fraudulent
        );
        
        return transactionRepository.findAll(spec, pageable)
            .map(this::mapToResponse);
    }
    
    private TransactionResponse mapToResponse(Transaction t) {
        TransactionResponse r = new TransactionResponse();
        r.setId(t.getId());
        r.setAmount(t.getAmount());
        r.setType(t.getType());
        r.setCategory(t.getCategory());
        r.setDescription(t.getDescription());
        r.setLocation(t.getLocation());
        r.setTransactionDate(t.getTransactionDate());
        r.setFraudulent(t.isFraudulent());
        r.setFraudScore(t.getFraudScore());
        r.setRiskLevel(calculateRiskLevelFromScore(t.getFraudScore()));
        r.setStatus(t.isFraudulent() ? "FLAGGED" : "COMPLETED");
        return r;
    }
    
    private TransactionResponse mapToResponse(Transaction t, FraudDetectionResult result) {
        TransactionResponse r = new TransactionResponse();
        r.setId(t.getId());
        r.setAmount(t.getAmount());
        r.setType(t.getType());
        r.setCategory(t.getCategory());
        r.setDescription(t.getDescription());
        r.setLocation(t.getLocation());
        r.setTransactionDate(t.getTransactionDate());
        r.setFraudulent(t.isFraudulent());
        r.setFraudScore(t.getFraudScore());
        r.setRiskLevel(result.getRiskLevel().name());
        r.setStatus(t.isFraudulent() ? "FLAGGED" : "COMPLETED");
        return r;
    }
    
    private String calculateRiskLevelFromScore(Double score) {
        if (score == null) {
            return "LOW";
        }
        if (score >= 70) {
            return "HIGH";
        } else if (score >= 40) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}
