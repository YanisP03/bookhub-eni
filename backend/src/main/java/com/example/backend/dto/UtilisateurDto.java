package com.example.backend.dto;

import java.time.LocalDateTime;

public class UtilisateurDto {

    private Integer id;
    private String nom;
    private String prenom;
    private String mail;
    private String telephone;
    private String roleLibelle;
    private LocalDateTime dateInscription;
    private int nbRetards;
    private boolean bloque;
    private LocalDateTime dateBlocageAuto;

    public UtilisateurDto() {}

    public UtilisateurDto(Integer id, String nom, String prenom, String mail, String telephone,
                          String roleLibelle, LocalDateTime dateInscription,
                          int nbRetards, boolean bloque, LocalDateTime dateBlocageAuto) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.telephone = telephone;
        this.roleLibelle = roleLibelle;
        this.dateInscription = dateInscription;
        this.nbRetards = nbRetards;
        this.bloque = bloque;
        this.dateBlocageAuto = dateBlocageAuto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public String getRoleLibelle() { return roleLibelle; }
    public void setRoleLibelle(String roleLibelle) { this.roleLibelle = roleLibelle; }

    public int getNbRetards() { return nbRetards; }
    public void setNbRetards(int nbRetards) { this.nbRetards = nbRetards; }

    public boolean isBloque() { return bloque; }
    public void setBloque(boolean bloque) { this.bloque = bloque; }

    public LocalDateTime getDateBlocageAuto() { return dateBlocageAuto; }
    public void setDateBlocageAuto(LocalDateTime dateBlocageAuto) { this.dateBlocageAuto = dateBlocageAuto; }
}
