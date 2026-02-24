package com.example.FinSight.specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;

import jakarta.persistence.criteria.Predicate;

public class TransactionSpecification {
    
    public static Specification<Transaction> withFilters(
            User user,
            String type,
            String category,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Boolean fraudulent) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always filter by user
            predicates.add(criteriaBuilder.equal(root.get("user"), user));
            
            // Filter by type
            if (type != null && !type.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            
            // Filter by category
            if (category != null && !category.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }
            
            // Filter by date range
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }
            
            // Filter by fraudulent status
            if (fraudulent != null) {
                predicates.add(criteriaBuilder.equal(root.get("fraudulent"), fraudulent));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
