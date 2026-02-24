package com.example.FinSight.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FraudDetectionResult {
    private boolean fraudulent;
    private double fraudScore;
    private String reasons;
}