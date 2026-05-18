package com.example.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AVIS")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avis")
    private Integer id;

    @Column(length = 100)
    private String pseudo;

    @Column(nullable = false)
    private int note; // 1 à 5

    @Column(length = 1000)
    private String commentaire;

    // ajout du statut pour un avis
    @Column(length = 20, nullable = false)
    private String statut = "EN_ATTENTE"; // EN_ATTENTE | APPROUVE | REJETE

    @Column(columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime datePublication = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_livre", nullable = false)
    private Livre livre;

    public Avis() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime d) { this.datePublication = d; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public Livre getLivre() { return livre; }
    public void setLivre(Livre livre) { this.livre = livre; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}