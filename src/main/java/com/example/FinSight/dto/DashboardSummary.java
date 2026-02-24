package com.example.FinSight.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private Long totalFlaggedTransactions;
    private Double averageFraudScore;
    private Map<String, BigDecimal> spendingByCategory;
    private Map<String, Long> fraudByCategory;
    private List<TimeSeriesPoint> spendingTrends;

    public DashboardSummary() {
    }

    public DashboardSummary(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal currentBalance,
                           Long totalFlaggedTransactions, Double averageFraudScore,
                           Map<String, BigDecimal> spendingByCategory, Map<String, Long> fraudByCategory,
                           List<TimeSeriesPoint> spendingTrends) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.currentBalance = currentBalance;
        this.totalFlaggedTransactions = totalFlaggedTransactions;
        this.averageFraudScore = averageFraudScore;
        this.spendingByCategory = spendingByCategory;
        this.fraudByCategory = fraudByCategory;
        this.spendingTrends = spendingTrends;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Long getTotalFlaggedTransactions() {
        return totalFlaggedTransactions;
    }

    public void setTotalFlaggedTransactions(Long totalFlaggedTransactions) {
        this.totalFlaggedTransactions = totalFlaggedTransactions;
    }

    public Double getAverageFraudScore() {
        return averageFraudScore;
    }

    public void setAverageFraudScore(Double averageFraudScore) {
        this.averageFraudScore = averageFraudScore;
    }

    public Map<String, BigDecimal> getSpendingByCategory() {
        return spendingByCategory;
    }

    public void setSpendingByCategory(Map<String, BigDecimal> spendingByCategory) {
        this.spendingByCategory = spendingByCategory;
    }

    public Map<String, Long> getFraudByCategory() {
        return fraudByCategory;
    }

    public void setFraudByCategory(Map<String, Long> fraudByCategory) {
        this.fraudByCategory = fraudByCategory;
    }

    public List<TimeSeriesPoint> getSpendingTrends() {
        return spendingTrends;
    }

    public void setSpendingTrends(List<TimeSeriesPoint> spendingTrends) {
        this.spendingTrends = spendingTrends;
    }
}
