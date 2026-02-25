package com.example.FinSight.service;

import java.util.List;

import com.example.FinSight.model.RiskLevel;

public class FraudDetectionResult {
    private boolean fraudulent;
    private double fraudScore; // 0-100
    private RiskLevel riskLevel;
    private List<String> reasons;
    
    public FraudDetectionResult(boolean fraudulent, double fraudScore, RiskLevel riskLevel, List<String> reasons) {
        this.fraudulent = fraudulent;
        this.fraudScore = fraudScore;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
    }
    
    public boolean isFraudulent() {
        return fraudulent;
    }
    
    public void setFraudulent(boolean fraudulent) {
        this.fraudulent = fraudulent;
    }
    
    public double getFraudScore() {
        return fraudScore;
    }
    
    public void setFraudScore(double fraudScore) {
        this.fraudScore = fraudScore;
    }
    
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public List<String> getReasons() {
        return reasons;
    }
    
    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}
