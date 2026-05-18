package com.example.backend.model.entity.dto;

import java.time.LocalDateTime;

public class AvisResponseDTO {

    private Integer id;
    private String pseudo;
    private int note;
    private String commentaire;
    private LocalDateTime datePublication;
    private String statut; // EN_ATTENTE, APPROUVE, REJETE
    private String nomLivre;
    private Integer idLivre;
    private String nomUtilisateur;

    // Getters / Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getNomLivre() { return nomLivre; }
    public void setNomLivre(String nomLivre) { this.nomLivre = nomLivre; }

    public Integer getIdLivre() { return idLivre; }
    public void setIdLivre(Integer idLivre) { this.idLivre = idLivre; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }
}