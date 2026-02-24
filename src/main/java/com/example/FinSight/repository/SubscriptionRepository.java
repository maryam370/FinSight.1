package com.example.FinSight.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.FinSight.model.Subscription;
import com.example.FinSight.model.SubscriptionStatus;
import com.example.FinSight.model.User;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUser(User user);
    
    List<Subscription> findByUserAndStatus(User user, SubscriptionStatus status);
    
    @Query("SELECT s FROM Subscription s WHERE s.user = :user " +
           "AND s.status = 'ACTIVE' " +
           "AND s.nextDueDate BETWEEN :start AND :end")
    List<Subscription> findDueSoon(@Param("user") User user, 
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end);
}
