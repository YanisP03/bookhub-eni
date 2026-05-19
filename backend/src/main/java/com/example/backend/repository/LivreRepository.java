package com.example.backend.repository;

import com.example.backend.model.entity.Categorie;
import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository de gestion des livres.
 *
 * Hérite de JpaRepository pour fournir automatiquement
 * les opérations CRUD standards :
 * - save()
 * - findById()
 * - findAll()
 * - deleteById()
 */
@Repository
public interface LivreRepository extends JpaRepository<Livre, Integer> {

    /**
     * Recherche des livres par titre, auteur ou ISBN
     * sans tenir compte de la casse.
     *
     * Exemple :
     * recherche "java" → trouvera :
     * - titre = "Java Spring"
     * - auteur = "Java Expert"
     * - ISBN contenant "java"
     *
     * @param titre mot-clé titre
     * @param auteur mot-clé auteur
     * @param isbn mot-clé ISBN
     * @return liste des livres correspondants
     */
    List<Livre> findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            String titre, String auteur, String isbn);

    /**
     * Récupère tous les livres d'une catégorie donnée.
     *
     * @param categorie catégorie recherchée
     * @return liste des livres
     */
    List<Livre> findByCategorie(Categorie categorie);

    /**
     * Récupère les livres selon leur statut.
     *
     * Exemple :
     * DISPONIBLE
     * INDISPONIBLE
     *
     * @param statut statut du livre
     * @return liste des livres
     */
    List<Livre> findByStatut(Statut statut);

    /**
     * Retourne les livres ayant un nombre d'exemplaires
     * disponibles supérieur à la valeur donnée.
     *
     * Exemple :
     * count = 0 → livres disponibles uniquement
     *
     * @param count seuil minimal
     * @return liste des livres disponibles
     */
    List<Livre> findByNbDisponiblesGreaterThan(int count);

    /**
     * Recherche avancée multi-critères.
     *
     * Les paramètres sont optionnels :
     * - titre
     * - catégorie
     * - disponibilité
     *
     * Si un paramètre est NULL, il est ignoré.
     *
     * Exemple :
     * titre="Java"
     * idCategorie=2
     * disponible=true
     *
     * @param titre filtre titre
     * @param idCategorie filtre catégorie
     * @param disponible filtre disponibilité
     * @return liste filtrée des livres
     */
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
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Livre l WHERE l.isbn = :isbn AND (:id IS NULL OR l.id != :id)")
    boolean existsByIsbnAndIdNot(@Param("isbn") String isbn, @Param("id") Integer id);

    @Query("SELECT l FROM Livre l ORDER BY (SELECT COUNT(e) FROM Emprunt e WHERE e.livre = l) DESC")
    List<Livre> findOrderedByBorrowCount();

    List<Livre> findByNoteMoyenneIsNotNullOrderByNoteMoyenneDesc();

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
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Emprunt e JOIN e.statut s WHERE e.livre.id = :idLivre AND LOWER(s.libelle) = 'EN_COURS'")
    boolean hasEmpruntsActifs(@Param("idLivre") Integer idLivre);

    /**
     * Recalcule la note moyenne d'un livre.
     *
     * Moyenne calculée uniquement avec
     * les avis approuvés.
     *
     * Formule :
     * AVG(note)
     *
     * Mise à jour du champ noteMoyenne
     * dans la table LIVRE.
     *
     * Requête SQL native utilisée.
     *
     * @param idLivre identifiant du livre
     */
    @Modifying
    @Query("UPDATE Livre l SET l.noteMoyenne = " +
            "(SELECT AVG(CAST(a.note AS java.math.BigDecimal)) FROM Avis a WHERE a.livre.id = :idLivre AND a.statut = 'APPROUVE') " +
            "WHERE l.id = :idLivre")
    void recalculerNoteMoyenne(@Param("idLivre") Integer idLivre);

}