package com.example.backend.model.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format invalide")
    private String mail;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "8 caractères minimum")
    private String motDePasse;

    // Constructeur vide (obligatoire pour Spring)
    public RegisterRequestDto() {}

    // Getters
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getMail() { return mail; }
    public String getMotDePasse() { return motDePasse; }

    // Setters
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setMail(String mail) { this.mail = mail; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}
