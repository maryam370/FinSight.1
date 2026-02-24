package com.example.FinSight.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.FinSight.model.Transaction;
import com.example.FinSight.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {
    private final TransactionRepository transactionRepository;

    public FraudDetectionResult analyzeTransaction(Transaction transaction) {
        double fraudScore = 0.0;
        StringBuilder reasons = new StringBuilder();

        // Rule 1: Large amount (> 10000)
        if (transaction.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            fraudScore += 0.4;
            reasons.append("Large amount, ");
        }

        // Rule 2: Multiple transactions in last hour (> 5)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Transaction> recentTxns = transactionRepository
            .findByUserAndTransactionDateAfter(transaction.getUser(), oneHourAgo);

        if (recentTxns.size() >= 5) {
            fraudScore += 0.3;
            reasons.append("Too many transactions in short time, ");
        }

        // Rule 3: Different location than usual
        List<Transaction> userTxns = transactionRepository.findByUser(transaction.getUser());
        if (!userTxns.isEmpty()) {
            String lastLocation = userTxns.get(userTxns.size() - 1).getLocation();
            if (!lastLocation.equals(transaction.getLocation())) {
                fraudScore += 0.2;
                reasons.append("Location changed, ");
            }
        }

        boolean isFraudulent = fraudScore > 0.5;

        return new FraudDetectionResult(isFraudulent, fraudScore, reasons.toString());
    }
}