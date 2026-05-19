package com.example.backend.repository;

import com.example.backend.model.entity.Avis;
import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvisRepository extends JpaRepository<Avis, Integer> {

    // Avis approuvés d'un livre (pour affichage public)
    List<Avis> findByLivreAndStatutOrderByDatePublicationDesc(Livre livre, String statut);

    // Tous les avis d'un utilisateur (pour son espace perso)
    List<Avis> findByUtilisateurOrderByDatePublicationDesc(Utilisateur utilisateur);

    // Avis en attente de modération (pour le bibliothécaire)
    List<Avis> findByStatutOrderByDatePublicationAsc(String statut);

    // Vérifier si l'utilisateur a déjà un avis sur ce livre
    Optional<Avis> findByUtilisateurAndLivre(Utilisateur utilisateur, Livre livre);

    // Vérifier si l'utilisateur a déjà réservé le livre (condition métier)
    @Query(value = """
    SELECT COUNT(*) FROM RESERVATION r
    INNER JOIN STATUT s ON s.id_statut = r.id_statut
    WHERE r.id_utilisateur = :idUtilisateur
      AND r.id_livre = :idLivre
      AND s.libelle_statut IN ('RÉCUPÉRÉ', 'RENDU')
    """, nativeQuery = true)
    int countReservationsTerminees(@Param("idUtilisateur") Integer idUtilisateur,
                                   @Param("idLivre") Integer idLivre);
}