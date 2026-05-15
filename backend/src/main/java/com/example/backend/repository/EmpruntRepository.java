package com.example.backend.repository;

import com.example.backend.model.entity.Emprunt;
import com.example.backend.model.entity.Statut;
import com.example.backend.model.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmpruntRepository extends JpaRepository<Emprunt, Integer> {
    List<Emprunt> findByUtilisateur(Utilisateur utilisateur);
    List<Emprunt> findByUtilisateurAndStatut(Utilisateur utilisateur, Statut statut);
    List<Emprunt> findByStatut(Statut statut);
    long countByUtilisateurAndStatut(Utilisateur utilisateur, Statut statut);
}