package com.example.FinSight.repository;



import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserAndFraudulentTrue(User user);
    List<Transaction> findByUserAndTransactionDateAfter(User user, LocalDateTime date);
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);
    
    Optional<Transaction> findTopByUserOrderByTransactionDateDesc(User user);
    
    Long countByUser(User user);
    
    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.user = :user")
    BigDecimal calculateAverageAmount(@Param("user") User user);
    
    @Query("SELECT DISTINCT t.category FROM Transaction t WHERE t.user = :user")
    List<String> findDistinctCategoriesByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.transactionDate BETWEEN :start AND :end")
    long countByUserAndTransactionDateBetween(@Param("user") User user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}