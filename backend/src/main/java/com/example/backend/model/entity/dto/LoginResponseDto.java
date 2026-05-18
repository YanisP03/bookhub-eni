package com.example.backend.model.entity.dto;

public class LoginResponseDto {
    private String token;
    private String type = "Bearer";
    private String mail;
    private String nom;
    private String prenom;
    private String role;

    public LoginResponseDto(String token, String mail, String nom, String prenom, String role) {
        this.token = token;
        this.mail = mail;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }

    public String getToken()  { return token; }
    public String getType()   { return type; }
    public String getMail()   { return mail; }
    public String getNom()    { return nom; }
    public String getPrenom() { return prenom; }
    public String getRole()   { return role; }
}
