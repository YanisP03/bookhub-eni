package com.example.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "LIVRE")
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livre")
    private Integer id;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(length = 255)
    private String auteur;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(length = 300)
    private String description;

    @Column(length = 255)
    private String couverture;

    private LocalDate datePublication;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int nbExemplaires = 0;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int nbDisponibles = 0;

    @Column(precision = 3, scale = 2, insertable = false, updatable = false)
    private BigDecimal noteMoyenne;

    @Column(columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime dateAjout = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_statut", nullable = false)
    private Statut statut;

    @JsonIgnore
    @OneToMany(mappedBy = "livre", fetch = FetchType.LAZY)
    private List<Emprunt> emprunts;

    @JsonIgnore
    @OneToMany(mappedBy = "livre", fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    @JsonIgnore
    @OneToMany(mappedBy = "livre", fetch = FetchType.LAZY)
    private List<Avis> avis;

    public Livre() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCouverture() { return couverture; }
    public void setCouverture(String couverture) { this.couverture = couverture; }

    public LocalDate getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }

    public int getNbExemplaires() { return nbExemplaires; }
    public void setNbExemplaires(int nbExemplaires) { this.nbExemplaires = nbExemplaires; }

    public int getNbDisponibles() { return nbDisponibles; }
    public void setNbDisponibles(int nbDisponibles) { this.nbDisponibles = nbDisponibles; }

    public BigDecimal getNoteMoyenne() { return noteMoyenne; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public List<Emprunt> getEmprunts() { return emprunts; }
    public List<Reservation> getReservations() { return reservations; }
    public List<Avis> getAvis() { return avis; }
}
