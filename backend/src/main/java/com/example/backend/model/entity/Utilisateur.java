package com.example.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "UTILISATEUR")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String mail;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String motDePasse;

    @Column(length = 20)
    private String telephone;

    @Column(name = "dateInscription", insertable = false, updatable = false, columnDefinition = "datetime2")
    private LocalDateTime dateInscription;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer nbRetards = 0;

    @Column(columnDefinition = "BIT DEFAULT 0")
    private Boolean bloque = false;

    @Column
    private LocalDateTime dateBlocageAuto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;

    @JsonIgnore
    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY)
    private List<Emprunt> emprunts;

    @JsonIgnore
    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    @JsonIgnore
    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY)
    private List<Avis> avis;

    public Utilisateur() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<Emprunt> getEmprunts() { return emprunts; }
    public List<Reservation> getReservations() { return reservations; }
    public List<Avis> getAvis() { return avis; }

    public int getNbRetards() { return nbRetards != null ? nbRetards : 0; }
    public void setNbRetards(int nbRetards) { this.nbRetards = nbRetards; }

    public boolean isBloque() { return Boolean.TRUE.equals(bloque); }
    public void setBloque(boolean bloque) { this.bloque = bloque; }

    public LocalDateTime getDateBlocageAuto() { return dateBlocageAuto; }
    public void setDateBlocageAuto(LocalDateTime dateBlocageAuto) { this.dateBlocageAuto = dateBlocageAuto; }
}
