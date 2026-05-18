package com.example.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "EMPRUNT")
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_emprunt")
    private Integer id;

    @Column(nullable = false)
    private LocalDateTime dateEmprunt;

    // Null tant que la demande n'est pas validée
    private LocalDateTime dateRetourPrevue;

    private LocalDateTime dateRetour;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int joursRetard = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    @JsonIgnoreProperties({"emprunts","reservations","avis","motDePasse"})
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_livre", nullable = false)
    @JsonIgnoreProperties({"emprunts","reservations","avis"})
    private Livre livre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_statut", nullable = false)
    private Statut statut;

    public Emprunt() {}

    public boolean isEnRetard() {
        return dateRetour == null && dateRetourPrevue != null
                && LocalDateTime.now().isAfter(dateRetourPrevue);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getDateEmprunt() { return dateEmprunt; }
    public void setDateEmprunt(LocalDateTime d) { this.dateEmprunt = d; }

    public LocalDateTime getDateRetourPrevue() { return dateRetourPrevue; }
    public void setDateRetourPrevue(LocalDateTime d) { this.dateRetourPrevue = d; }

    public LocalDateTime getDateRetour() { return dateRetour; }
    public void setDateRetour(LocalDateTime d) { this.dateRetour = d; }

    public int getJoursRetard() { return joursRetard; }
    public void setJoursRetard(int joursRetard) { this.joursRetard = joursRetard; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public Livre getLivre() { return livre; }
    public void setLivre(Livre livre) { this.livre = livre; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
}
