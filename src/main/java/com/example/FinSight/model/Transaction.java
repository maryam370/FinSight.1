package com.example.FinSight.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private User user;
    
    private BigDecimal amount;
    
    private String type;
    
    private String category;
    
    private String description;
    
    private String location;
    
    private LocalDateTime transactionDate;
    
    private boolean fraudulent;
    
    private Double fraudScore;
}