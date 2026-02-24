package com.example.FinSight.repository;



import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserAndFraudulentTrue(User user);
    List<Transaction> findByUserAndTransactionDateAfter(User user, LocalDateTime date);
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);
}