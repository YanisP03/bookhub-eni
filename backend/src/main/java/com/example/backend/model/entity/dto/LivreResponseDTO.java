package com.bookhub.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO retourné au client après création/modification ou consultation d'un livre.
 */
public class LivreResponseDTO {

    private Integer id;
    private String titre;
    private String auteur;
    private String isbn;
    private String description;
    private String couverture;
    private LocalDate datePublication;
    private Integer nbExemplaires;
    private Integer nbDisponibles;
    private BigDecimal noteMoyenne;
    private LocalDateTime dateAjout;
    private Integer idCategorie;
    private String nomCategorie;
    private Integer idStatut;
    private String libelleStatut;

    // ── Getters / Setters ──────────────────────────────────────────────────

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

    public Integer getNbExemplaires() { return nbExemplaires; }
    public void setNbExemplaires(Integer nbExemplaires) { this.nbExemplaires = nbExemplaires; }

    public Integer getNbDisponibles() { return nbDisponibles; }
    public void setNbDisponibles(Integer nbDisponibles) { this.nbDisponibles = nbDisponibles; }

    public BigDecimal getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(BigDecimal noteMoyenne) { this.noteMoyenne = noteMoyenne; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }

    public Integer getIdCategorie() { return idCategorie; }
    public void setIdCategorie(Integer idCategorie) { this.idCategorie = idCategorie; }

    public String getNomCategorie() { return nomCategorie; }
    public void setNomCategorie(String nomCategorie) { this.nomCategorie = nomCategorie; }

    public Integer getIdStatut() { return idStatut; }
    public void setIdStatut(Integer idStatut) { this.idStatut = idStatut; }

    public String getLibelleStatut() { return libelleStatut; }
    public void setLibelleStatut(String libelleStatut) { this.libelleStatut = libelleStatut; }
}