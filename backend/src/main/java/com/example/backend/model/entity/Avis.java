package com.example.backend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "AVIS")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAvis;

    @Size(max = 100)
    private String pseudo;

    private Integer note;

    @Size(max = 1000)
    private String commentaire;

    private LocalDateTime datePublication;

    @PrePersist
    public void prePersist() {
        this.datePublication = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "id_livre", nullable = false)
    private Livre livre;

    @Override
    public String toString() {
        return "Avis{" +
                "idAvis=" + idAvis +
                ", pseudo='" + pseudo + '\'' +
                ", note=" + note +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Avis avis)) return false;
        return Objects.equals(idAvis, avis.idAvis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAvis);
    }
}
