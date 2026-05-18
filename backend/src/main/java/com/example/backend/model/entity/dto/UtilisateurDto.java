package com.example.backend.model.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UtilisateurDto {

    private Integer id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String mail;

    // Le libellé du rôle (ex: "LECTEUR", "LIBRARIAN") plutôt que l'objet Role entier
    private String roleLibelle;

    // Constructeur vide (Obligatoire pour Spring / Jackson)
    public UtilisateurDto() {}

    // Constructeur complet (Pratique pour transformer rapidement une entité en DTO)
    public UtilisateurDto(Integer id, String nom, String prenom, String mail, String roleLibelle) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.roleLibelle = roleLibelle;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getRoleLibelle() { return roleLibelle; }
    public void setRoleLibelle(String roleLibelle) { this.roleLibelle = roleLibelle; }
}