package com.example.backend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CATEGORIE")
public class Categorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCategorie;

    @NotBlank
    @Size(max = 100)
    private String nom;

    @Size(max = 300)
    private String description;

    @Override
    public String toString() { return "Categorie{id=" + idCategorie + ", nom='" + nom + "'}"; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof Categorie that)) return false;
        return Objects.equals(idCategorie, that.idCategorie); }

    @Override
    public int hashCode() { return Objects.hash(idCategorie); }
}