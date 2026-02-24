package com.example.FinSight.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FraudAlertDto {
    private Long id;
    private Long userId;
    private Long transactionId;
    private String message;
    private String severity;
    private boolean resolved;
    private LocalDateTime createdAt;
}