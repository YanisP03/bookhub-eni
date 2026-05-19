package com.example.backend.repository;

import com.example.backend.model.entity.Retour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RetourRepository extends JpaRepository<Retour, Integer> {
    Optional<Retour> findByLivreIdAndStatutLibelle(Integer livreId, String statutLibelle);
}
