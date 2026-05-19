package com.example.backend.dto;

public class LoginResponseDto {
    private String token;
    private String type = "Bearer";
    private String email;
    private String role;

    public LoginResponseDto(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    // Getters
    public String getToken() { return token; }
    public String getType() { return type; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}