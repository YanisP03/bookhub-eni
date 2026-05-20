package com.example.backend.repository;

import com.example.backend.model.entity.Emprunt;
import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Statut;
import com.example.backend.model.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmpruntRepository extends JpaRepository<Emprunt, Integer> {

    /** Tous les emprunts d'un utilisateur (historique complet). */
    List<Emprunt> findByUtilisateur(Utilisateur utilisateur);

    /** Emprunts filtrés par statut (utilisé pour lister les DEMANDE, RETOUR_DEMANDE, etc.). */
    List<Emprunt> findByStatut(Statut statut);

    /** Nombre d'emprunts actifs (DEMANDE ou EN_COURS) pour un utilisateur.
     *  Utilisé pour vérifier la limite de 3 emprunts simultanés. */
    @Query("SELECT COUNT(e) FROM Emprunt e WHERE e.utilisateur = :user AND e.statut.libelle IN ('EN_COURS', 'DEMANDE')")
    long countActifsByUtilisateur(@Param("user") Utilisateur user);

    /** File d'attente DEMANDE pour un livre donné, triée par date (plus ancienne en premier). */
    @Query("SELECT e FROM Emprunt e WHERE e.livre.id = :livreId AND e.statut.libelle = 'DEMANDE' ORDER BY e.dateEmprunt ASC")
    List<Emprunt> findDemandesByLivreOrderByDate(@Param("livreId") Integer livreId);

    /** Vérifie si l'utilisateur a déjà rendu ce livre (permet de poster un avis). */
    @Query("SELECT COUNT(e) > 0 FROM Emprunt e WHERE e.utilisateur = :user AND e.livre = :livre AND e.statut.libelle = 'RENDU'")
    boolean aRenduLivre(@Param("user") Utilisateur user, @Param("livre") Livre livre);

    /** Vérifie si l'utilisateur a déjà un emprunt actif (DEMANDE ou EN_COURS) pour ce livre.
     *  Évite les doublons avant appel à la procédure stockée. */
    @Query("SELECT COUNT(e) > 0 FROM Emprunt e WHERE e.utilisateur = :user AND e.livre = :livre AND e.statut.libelle IN ('DEMANDE', 'EN_COURS')")
    boolean hasEmpruntActifPourLivre(@Param("user") Utilisateur user, @Param("livre") Livre livre);

    void deleteByUtilisateur(Utilisateur utilisateur);
}
