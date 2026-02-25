package com.example.FinSight.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.FinSight.dto.FraudAlertDto;
import com.example.FinSight.service.FraudAlertService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudAlertController {
    
    private final FraudAlertService fraudAlertService;
    
    @GetMapping("/alerts")
    public ResponseEntity<List<FraudAlertDto>> getAlerts(
            @RequestParam Long userId,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(required = false) String severity) {
        
        List<FraudAlertDto> alerts;
        
        if (resolved != null && severity != null) {
            alerts = fraudAlertService.findByUserAndResolvedAndSeverity(userId, resolved, severity);
        } else if (resolved != null) {
            alerts = fraudAlertService.findByUserAndResolved(userId, resolved);
        } else if (severity != null) {
            alerts = fraudAlertService.findByUserAndSeverity(userId, severity);
        } else {
            alerts = fraudAlertService.findByUser(userId);
        }
        
        return ResponseEntity.ok(alerts);
    }
    
    @PutMapping("/alerts/{id}/resolve")
    public ResponseEntity<FraudAlertDto> resolveAlert(@PathVariable Long id) {
        FraudAlertDto resolved = fraudAlertService.resolveAlert(id);
        return ResponseEntity.ok(resolved);
    }
}
