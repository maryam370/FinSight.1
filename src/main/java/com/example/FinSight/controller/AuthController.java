package com.example.FinSight.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FinSight.dto.LoginRequest;
import com.example.FinSight.dto.LoginResponse;
import com.example.FinSight.dto.RegisterRequest;
import com.example.FinSight.dto.UserDto;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.TransactionRepository;
import com.example.FinSight.repository.UserRepository;
import com.example.FinSight.service.DemoDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final DemoDataService demoDataService;
    
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // TODO: Hash password in production
        user.setFullName(request.getFullName());
        user.setCreatedAt(java.time.LocalDateTime.now());
        
        User saved = userRepository.save(user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(saved));
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElse(null);
        
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Check if demo data needs to be seeded
        Long transactionCount = transactionRepository.countByUser(user);
        boolean demoSeeded = false;
        
        if (transactionCount == 0) {
            demoDataService.seedUserIfEmpty(user.getId());
            demoSeeded = true;
        }
        
        LoginResponse response = new LoginResponse();
        response.setToken("demo-token-" + user.getId()); // Simplified token for hackathon
        response.setUser(mapToDto(user));
        response.setDemoSeeded(demoSeeded);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        // TODO: Get user from security context
        // For now, return mock response
        return ResponseEntity.ok(new UserDto());
    }
    
    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
