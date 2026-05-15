package com.example.backend.repository;

import com.example.backend.model.entity.Avis;
import com.example.backend.model.entity.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Integer> {
    List<Avis> findByLivreOrderByDatePublicationDesc(Livre livre);
}