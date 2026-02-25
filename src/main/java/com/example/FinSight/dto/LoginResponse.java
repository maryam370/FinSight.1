package com.example.FinSight.dto;

public class LoginResponse {
    private String token;
    private UserDto user;
    private boolean demoSeeded;
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public UserDto getUser() {
        return user;
    }
    
    public void setUser(UserDto user) {
        this.user = user;
    }
    
    public boolean isDemoSeeded() {
        return demoSeeded;
    }
    
    public void setDemoSeeded(boolean demoSeeded) {
        this.demoSeeded = demoSeeded;
    }
}
