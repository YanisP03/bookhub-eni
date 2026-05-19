package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LivreDto {
    private Integer id;
    private String titre;
    private String auteur;
    private String isbn;
    private String description;
    private String nomCategorie;
    private BigDecimal noteMoyenne;
    private int nbDisponibles;
    private List<AvisDto> commentaires;

    @Data
    public static class AvisDto {
        private String pseudo;
        private Integer note;
        private String commentaire;
    }
}
