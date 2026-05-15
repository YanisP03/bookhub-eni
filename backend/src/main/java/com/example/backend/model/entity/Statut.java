package com.example.backend.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "STATUT")
public class Statut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut")
    private Integer id;

    @Column(name = "libelle_statut", nullable = false, length = 50)
    private String libelle;

    public Statut() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}