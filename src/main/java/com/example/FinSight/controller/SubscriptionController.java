package com.example.FinSight.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.FinSight.model.Subscription;
import com.example.FinSight.model.SubscriptionStatus;
import com.example.FinSight.repository.SubscriptionRepository;
import com.example.FinSight.repository.UserRepository;
import com.example.FinSight.service.SubscriptionDetectorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    
    private final SubscriptionDetectorService subscriptionDetectorService;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    
    @PostMapping("/detect")
    public ResponseEntity<List<Subscription>> detectSubscriptions(@RequestBody DetectRequest request) {
        List<Subscription> subscriptions = subscriptionDetectorService.detectSubscriptions(request.getUserId());
        return ResponseEntity.ok(subscriptions);
    }
    
    @GetMapping
    public ResponseEntity<List<Subscription>> getSubscriptions(
            @RequestParam Long userId,
            @RequestParam(required = false) String status) {
        
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Subscription> subscriptions;
        if (status != null) {
            subscriptions = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.valueOf(status));
        } else {
            subscriptions = subscriptionRepository.findByUser(user);
        }
        
        return ResponseEntity.ok(subscriptions);
    }
    
    @PutMapping("/{id}/ignore")
    public ResponseEntity<Subscription> ignoreSubscription(@PathVariable Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        subscription.setStatus(SubscriptionStatus.IGNORED);
        Subscription saved = subscriptionRepository.save(subscription);
        
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/due-soon")
    public ResponseEntity<List<Subscription>> getDueSoon(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "7") int days) {
        
        List<Subscription> dueSoon = subscriptionDetectorService.findDueSoon(userId, days);
        return ResponseEntity.ok(dueSoon);
    }
    
    // Inner class for request body
    public static class DetectRequest {
        private Long userId;
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}
