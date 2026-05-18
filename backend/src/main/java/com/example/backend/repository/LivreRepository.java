package com.example.backend.repository;

import com.example.backend.model.entity.Categorie;
import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Integer> {

    List<Livre> findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            String titre, String auteur, String isbn);

    List<Livre> findByCategorie(Categorie categorie);

    List<Livre> findByStatut(Statut statut);

    List<Livre> findByNbDisponiblesGreaterThan(int count);

    @Query("SELECT l FROM Livre l WHERE " +
            "(:titre IS NULL OR LOWER(l.titre) LIKE LOWER(CONCAT('%', :titre, '%'))) AND " +
            "(:idCategorie IS NULL OR l.categorie.id = :idCategorie) AND " +
            "(:disponible IS NULL OR l.nbDisponibles > 0)")
    List<Livre> rechercher(
            @Param("titre") String titre,
            @Param("idCategorie") Integer idCategorie,
            @Param("disponible") Boolean disponible);



    /**
     * Vérifie si un ISBN existe déjà (hors l'id courant pour les updates).
     */
    @Query("SELECT COUNT(l) > 0 FROM Livre l WHERE l.isbn = :isbn AND (:id IS NULL OR l.id != :id)")
    boolean existsByIsbnAndIdNot(@Param("isbn") String isbn, @Param("id") Integer id);

    /**
     * Recherche full-text sur titre, auteur et ISBN.
     */
    @Query("SELECT l FROM Livre l WHERE " +
            "LOWER(l.titre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(l.auteur) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "l.isbn LIKE CONCAT('%', :q, '%')")
    List<Livre> search(@Param("q") String query);

    /**
     * Filtre par catégorie.
     */
    List<Livre> findByCategorie_Id(Integer idCategorie);

    /**
     * Filtre par statut.
     */
    List<Livre> findByStatut_Id(Integer idStatut);

    /**
     * Vérifie si le livre a des emprunts actifs (pour bloquer la suppression).
     */
    @Query("SELECT COUNT(e) > 0 FROM Emprunt e " +
            "JOIN e.statut s " +
            "WHERE e.livre.id = :idLivre AND s.libelle = 'EN COURS'")
    boolean hasEmpruntsActifs(@Param("idLivre") Integer idLivre);

}