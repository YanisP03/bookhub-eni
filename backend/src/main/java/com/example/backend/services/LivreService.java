package com.example.backend.services;

import com.example.backend.model.dto.LivreDto;
import com.example.backend.model.entity.Livre;
import com.example.backend.repository.LivreRepository;
import com.example.backend.repository.StatutRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LivreService {

    private final LivreRepository livreRepository;
    private final StatutRepository statutRepository;

    public LivreService(LivreRepository livreRepository, StatutRepository statutRepository) {
        this.livreRepository = livreRepository;
        this.statutRepository = statutRepository;
    }

    public List<Livre> findAll() {
        return livreRepository.findAll();
    }

    public Optional<Livre> findById(Integer id) {
        return livreRepository.findById(id);
    }


    public List<Livre> search(String query) {
        return livreRepository
                .findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                        query, query, query);
    }

    public List<Livre> findDisponibles() {
        return livreRepository.findByNbDisponiblesGreaterThan(0);
    }

    public List<Livre> rechercher(String titre, Integer idCategorie, Boolean disponible) {
        return livreRepository.rechercher(titre, idCategorie, disponible);
    }

    public Livre save(Livre livre) {
        return livreRepository.save(livre);
    }

    public void deleteById(Integer id) {
        livreRepository.deleteById(id);
    }

    public Optional<LivreDto> getLivreDetail(Integer id) {
        return livreRepository.findById(id).map(livre -> {
            LivreDto dto = new LivreDto();
            dto.setId(livre.getId());
            dto.setTitre(livre.getTitre());
            dto.setAuteur(livre.getAuteur());
            dto.setIsbn(livre.getIsbn());
            dto.setDescription(livre.getDescription());
            dto.setNbDisponibles(livre.getNbDisponibles());
            dto.setNoteMoyenne(livre.getNoteMoyenne());

            if (livre.getCategorie() != null) {
                dto.setNomCategorie(livre.getCategorie().getNom());
            }
            // Mapping des avis
            if (livre.getAvis() != null) {
                dto.setCommentaires(livre.getAvis().stream().map(a -> {
                    LivreDto.AvisDto avisDto = new LivreDto.AvisDto();
                    avisDto.setPseudo(a.getPseudo());
                    avisDto.setNote(a.getNote());
                    avisDto.setCommentaire(a.getCommentaire());
                    return avisDto;
                }).toList());
            }

            return dto;
        });
    }
}