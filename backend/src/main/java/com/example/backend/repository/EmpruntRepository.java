package com.example.backend.repository;

import com.example.backend.model.entity.Emprunt;
import com.example.backend.model.entity.Statut;
import com.example.backend.model.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmpruntRepository extends JpaRepository<Emprunt, Integer> {
    List<Emprunt> findByUtilisateur(Utilisateur utilisateur);
    List<Emprunt> findByUtilisateurAndStatut(Utilisateur utilisateur, Statut statut);
    List<Emprunt> findByStatut(Statut statut);
    long countByUtilisateurAndStatut(Utilisateur utilisateur, Statut statut);

    @Query("SELECT COUNT(e) FROM Emprunt e WHERE e.utilisateur = :user AND e.statut.libelle IN ('EN_COURS', 'DEMANDE')")
    long countActifsByUtilisateur(@Param("user") Utilisateur user);
}
