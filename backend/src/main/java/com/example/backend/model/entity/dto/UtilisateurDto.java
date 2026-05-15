package com.example.backend.model.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class UtilisateurDto {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
}
