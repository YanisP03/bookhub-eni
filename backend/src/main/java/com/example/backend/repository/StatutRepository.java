package com.example.backend.repository;

import com.example.backend.model.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StatutRepository extends JpaRepository<Statut, Integer> {
    Optional<Statut> findByLibelle(String libelle);
}