package com.example.backend.repository;

import com.example.backend.model.entity.Retour;
import com.example.backend.model.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository pour l'entité Retour (table RETOUR).
 * Entité distincte de Emprunt — représente un suivi de retour
 * géré via LoanService / RetourService (workflow alternatif).
 */
@Repository
public interface RetourRepository extends JpaRepository<Retour, Integer> {
    void deleteByUtilisateur(Utilisateur utilisateur);
}
