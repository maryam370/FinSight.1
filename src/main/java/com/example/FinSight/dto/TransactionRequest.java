package com.example.FinSight.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    private Long userId;
    private BigDecimal amount;
    private String type;
    private String category;
    private String description;
    private String location;
}