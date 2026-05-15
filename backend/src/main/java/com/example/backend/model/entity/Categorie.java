package com.example.backend.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CATEGORIE")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 300)
    private String description;

    public Categorie() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}