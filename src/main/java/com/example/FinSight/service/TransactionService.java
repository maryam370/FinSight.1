package com.example.FinSight.service;

import com.example.FinSight.dto.TransactionRequest;
import com.example.FinSight.dto.TransactionResponse;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.model.FraudAlert;
import com.example.FinSight.repository.TransactionRepository;
import com.example.FinSight.repository.UserRepository;
import com.example.FinSight.repository.FraudAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final FraudDetectionService fraudDetectionService;
    
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
        transaction.setTransactionDate(LocalDateTime.now());
        
        // Detect fraud
        FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);
        transaction.setFraudulent(result.isFraudulent());
        transaction.setFraudScore(result.getFraudScore());
        
        Transaction saved = transactionRepository.save(transaction);
        
        // Create alert if fraud detected
        if (result.isFraudulent()) {
            FraudAlert alert = new FraudAlert();
            alert.setUser(user);
            alert.setTransaction(saved);
            alert.setMessage("Fraud detected: " + result.getReasons());
            alert.setSeverity(result.getFraudScore() > 0.7 ? "HIGH" : "MEDIUM");
            alert.setResolved(false);
            alert.setCreatedAt(LocalDateTime.now());
            fraudAlertRepository.save(alert);
        }
        
        return mapToResponse(saved);
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
        r.setStatus(t.isFraudulent() ? "FLAGGED" : "COMPLETED");
        return r;
    }
}
