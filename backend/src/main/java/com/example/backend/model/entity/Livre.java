package com.example.backend.model.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "LIVRE")
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idLivre;

    @NotBlank
    @Size(max = 255)
    private String titre;

    @Size(max = 255)
    private String auteur;

    @Column(unique = true)
    @Size(max = 20)
    private String isbn;

    @Size(max = 300)
    private String description;

    private String couverture;

    private LocalDate datePublication;

    @PositiveOrZero
    private Integer nbExemplaires;

    @PositiveOrZero
    private Integer nbDisponibles;

    private Integer noteMoyenne;

    private LocalDateTime dateAjout;

    // Ajout date automatique
    @PrePersist
    public void prePersist() {
        this.dateAjout = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "id_categorie")
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "id_statut")
    private Statut statut;

    @OneToMany(mappedBy = "livre")
    private List<Emprunt> emprunts;

    @OneToMany(mappedBy = "livre")
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "livre")
    private List<Avis> avis;



    @Override
    public String toString() {
        return "Livre{" +
                "idLivre=" + idLivre +
                ", titre='" + titre + '\'' +
                ", auteur='" + auteur + '\'' +
                ", isbn='" + isbn + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Livre livre)) return false;
        return Objects.equals(idLivre, livre.idLivre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLivre);
    }
}
