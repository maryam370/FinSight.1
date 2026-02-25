package com.example.FinSight.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FinSight.dto.FraudAlertDto;
import com.example.FinSight.dto.TransactionResponse;
import com.example.FinSight.model.FraudAlert;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.FraudAlertRepository;
import com.example.FinSight.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FraudAlertService {
    
    private static final Logger log = LoggerFactory.getLogger(FraudAlertService.class);
    
    private final FraudAlertRepository fraudAlertRepository;
    private final UserRepository userRepository;

    
    /**
     * Finds all fraud alerts for a user ordered by creation date descending.
     * 
     * @param userId The user ID
     * @return List of fraud alert DTOs with transaction details
     */
    public List<FraudAlertDto> findByUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        List<FraudAlert> alerts = fraudAlertRepository.findByUserOrderByCreatedAtDesc(user);
        
        return alerts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds fraud alerts filtered by resolved status.
     * 
     * @param userId The user ID
     * @param resolved The resolved status filter
     * @return List of filtered fraud alert DTOs
     */
    public List<FraudAlertDto> findByUserAndResolved(Long userId, boolean resolved) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        List<FraudAlert> alerts = fraudAlertRepository.findByUserAndResolvedOrderByCreatedAtDesc(user, resolved);
        
        return alerts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds fraud alerts filtered by severity level.
     * 
     * @param userId The user ID
     * @param severity The severity level filter
     * @return List of filtered fraud alert DTOs
     */
    public List<FraudAlertDto> findByUserAndSeverity(Long userId, String severity) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        List<FraudAlert> alerts = fraudAlertRepository.findByUserAndSeverityOrderByCreatedAtDesc(user, severity);
        
        return alerts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds fraud alerts filtered by both resolved status and severity level.
     * 
     * @param userId The user ID
     * @param resolved The resolved status filter
     * @param severity The severity level filter
     * @return List of filtered fraud alert DTOs
     */
    public List<FraudAlertDto> findByUserAndResolvedAndSeverity(Long userId, boolean resolved, String severity) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        List<FraudAlert> alerts = fraudAlertRepository.findByUserAndResolvedAndSeverityOrderByCreatedAtDesc(user, resolved, severity);
        
        return alerts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Resolves a fraud alert by setting the resolved flag to true.
     * 
     * @param alertId The fraud alert ID
     * @return The updated fraud alert DTO
     */
    @Transactional
    public FraudAlertDto resolveAlert(Long alertId) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Fraud alert not found with id: " + alertId));
        
        alert.setResolved(true);
        FraudAlert savedAlert = fraudAlertRepository.save(alert);
        
        
        log.info("Fraud alert {} resolved for user {}", alertId, alert.getUser().getId());
        
        return convertToDto(savedAlert);
    }
    
    /**
     * Converts a FraudAlert entity to a FraudAlertDto with transaction details.
     * 
     * @param alert The fraud alert entity
     * @return The fraud alert DTO
     */
    private FraudAlertDto convertToDto(FraudAlert alert) {
        FraudAlertDto dto = new FraudAlertDto();
        dto.setId(alert.getId());
        dto.setUserId(alert.getUser().getId());
        dto.setMessage(alert.getMessage());
        dto.setSeverity(alert.getSeverity());
        dto.setResolved(alert.isResolved());
        dto.setCreatedAt(alert.getCreatedAt());
        
        // Include transaction details
        if (alert.getTransaction() != null) {
            dto.setTransaction(convertTransactionToResponse(alert.getTransaction()));
        }
        
        return dto;
    }
    
    /**
     * Converts a Transaction entity to a TransactionResponse.
     * 
     * @param transaction The transaction entity
     * @return The transaction response DTO
     */
    private TransactionResponse convertTransactionToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setCategory(transaction.getCategory());
        response.setDescription(transaction.getDescription());
        response.setLocation(transaction.getLocation());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setFraudulent(transaction.isFraudulent());
        response.setFraudScore(transaction.getFraudScore());
        
        // Set risk level based on fraud score
        if (transaction.getFraudScore() != null) {
            if (transaction.getFraudScore() >= 70) {
                response.setRiskLevel("HIGH");
            } else if (transaction.getFraudScore() >= 40) {
                response.setRiskLevel("MEDIUM");
            } else {
                response.setRiskLevel("LOW");
            }
        }
        
        response.setStatus(transaction.isFraudulent() ? "FLAGGED" : "COMPLETED");
        
        return response;
    }
}
