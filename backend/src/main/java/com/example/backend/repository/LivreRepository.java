package com.example.backend.repository;

import com.example.backend.model.entity.Categorie;
import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

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
}