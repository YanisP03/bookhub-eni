package com.example.backend.repository;

import com.example.backend.model.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> { // Changement de Long à Integer ici

    // Utilisé pour la connexion
    Optional<Utilisateur> findByMail(String mail);

    // Utilisé pour l'inscription (vérification d'unicité)
    boolean existsByMail(String mail);
}