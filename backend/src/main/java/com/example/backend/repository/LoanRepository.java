package com.example.backend.repository;

import com.example.backend.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {

    // Une méthode utile pour plus tard : trouver l'emprunt actif d'un livre
    // (en cherchant par l'id du livre et un statut "EN_COURS")
    Optional<Loan> findByLivreIdAndStatutLibelle(Integer livreId, String statutLibelle);
}
