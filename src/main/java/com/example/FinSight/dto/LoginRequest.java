package com.example.FinSight.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    
    // Explicit getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
