package com.example.FinSight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.FinSight.model.FraudAlert;
import com.example.FinSight.model.User;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByUser(User user);
    List<FraudAlert> findByUserAndResolvedFalse(User user);
}