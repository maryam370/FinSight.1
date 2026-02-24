package com.example.FinSight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.FinSight.model.FraudAlert;
import com.example.FinSight.model.User;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByUserOrderByCreatedAtDesc(User user);
    List<FraudAlert> findByUser(User user);
    List<FraudAlert> findByUserAndResolvedFalse(User user);
    List<FraudAlert> findByUserAndResolvedOrderByCreatedAtDesc(User user, boolean resolved);
    List<FraudAlert> findByUserAndSeverityOrderByCreatedAtDesc(User user, String severity);
    List<FraudAlert> findByUserAndResolvedAndSeverityOrderByCreatedAtDesc(User user, boolean resolved, String severity);
}