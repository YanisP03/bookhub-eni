package com.example.backend.dto;

import jakarta.validation.constraints.*;

public class AvisRequestDTO {

    private String pseudo; // optionnel, sinon nom utilisateur

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimale est 1")
    @Max(value = 5, message = "La note maximale est 5")
    private int note;

    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String commentaire;

    @NotNull(message = "L'id du livre est obligatoire")
    private Integer idLivre;

    // Getters / Setters
    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Integer getIdLivre() { return idLivre; }
    public void setIdLivre(Integer idLivre) { this.idLivre = idLivre; }
}