package com.example.FinSight.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FraudAlertDto {
    private Long id;
    private Long userId;
    private TransactionResponse transaction;
    private String message;
    private String severity;
    private boolean resolved;
    private LocalDateTime createdAt;
    
    // Explicit getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public TransactionResponse getTransaction() {
        return transaction;
    }
    
    public void setTransaction(TransactionResponse transaction) {
        this.transaction = transaction;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public boolean isResolved() {
        return resolved;
    }
    
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
