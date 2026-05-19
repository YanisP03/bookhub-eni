package com.example.backend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO utilisé pour la création ET la modification d'un livre.
 * Si id est null → création, sinon → modification.
 */
public class LivreRequestDTO {

    // Null pour un INSERT, renseigné pour un UPDATE
    private Integer id;

    @NotBlank(message = "Le titre est obligatoire.")
    @Size(max = 255, message = "Le titre ne doit pas dépasser 255 caractères.")
    private String titre;

    @Size(max = 255, message = "L'auteur ne doit pas dépasser 255 caractères.")
    private String auteur;

    @Size(max = 20, message = "L'ISBN ne doit pas dépasser 20 caractères.")
    private String isbn;

    @Size(max = 300, message = "La description ne doit pas dépasser 300 caractères.")
    private String description;

    @Size(max = 255, message = "L'URL de couverture ne doit pas dépasser 255 caractères.")
    private String couverture;

    private LocalDate datePublication;

    @NotNull(message = "Le nombre d'exemplaires est obligatoire.")
    @Min(value = 1, message = "Il doit y avoir au moins 1 exemplaire.")
    @Max(value = 50, message = "Le nombre d'exemplaires ne peut pas dépasser 50.")
    private Integer nbExemplaires;

    @NotNull(message = "Le nombre d'exemplaires disponibles est obligatoire.")
    @Min(value = 0, message = "Le nombre d'exemplaires disponibles ne peut pas être négatif.")
    private Integer nbDisponibles;

    @NotNull(message = "La catégorie est obligatoire.")
    private Integer idCategorie;

    @NotNull(message = "Le statut est obligatoire.")
    private Integer idStatut;

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

    public Integer getIdCategorie() { return idCategorie; }
    public void setIdCategorie(Integer idCategorie) { this.idCategorie = idCategorie; }

    public Integer getIdStatut() { return idStatut; }
    public void setIdStatut(Integer idStatut) { this.idStatut = idStatut; }
}
