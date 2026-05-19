package com.example.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RETOUR")
public class Retour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_retour")
    private Integer id;

    @Column(nullable = false)
    private LocalDateTime dateEmprunt;

    @Column(nullable = false)
    private LocalDateTime dateRetourPrevue;

    private LocalDateTime dateRetourReelle;

    private Integer joursDeRetard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livre", nullable = false)
    private Livre livre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_statut", nullable = false)
    private Statut statut;

    public Retour() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getDateEmprunt() { return dateEmprunt; }
    public void setDateEmprunt(LocalDateTime d) { this.dateEmprunt = d; }

    public LocalDateTime getDateRetourPrevue() { return dateRetourPrevue; }
    public void setDateRetourPrevue(LocalDateTime d) { this.dateRetourPrevue = d; }

    public LocalDateTime getDateRetourReelle() { return dateRetourReelle; }
    public void setDateRetourReelle(LocalDateTime d) { this.dateRetourReelle = d; }

    public Integer getJoursDeRetard() { return joursDeRetard; }
    public void setJoursDeRetard(Integer j) { this.joursDeRetard = j; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur u) { this.utilisateur = u; }

    public Livre getLivre() { return livre; }
    public void setLivre(Livre l) { this.livre = l; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut s) { this.statut = s; }
}
