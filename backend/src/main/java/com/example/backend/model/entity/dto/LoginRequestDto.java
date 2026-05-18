package com.example.backend.model.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public class LoginRequestDto {
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format d'email est invalide")
    private String mail;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    public LoginRequestDto(){}

    public String getMail() {
        return mail;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

}
