package com.example.backend.services;

import com.example.backend.model.dto.LivreDto;
import com.example.backend.model.entity.Categorie;
import com.example.backend.model.entity.Livre;
import com.example.backend.repository.LivreRepository;
import com.example.backend.repository.StatutRepository;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class LivreService {

    private final LivreRepository livreRepository;
    private final StatutRepository statutRepository;
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public LivreService(LivreRepository livreRepository,
                        StatutRepository statutRepository,
                        JdbcTemplate jdbcTemplate) {
        this.livreRepository = livreRepository;
        this.statutRepository = statutRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── Sauvegarde (INSERT ou UPDATE via procédure stockée) ────────────────

    /**
     * Crée ou modifie un livre via la procédure stockée sp_SaveLivre.
     * Si livre.getId() == null → INSERT
     * Si livre.getId() != null → UPDATE
     */
    public Livre saveLivre(Livre livre) {

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_SaveLivre")
                .withoutProcedureColumnMetaDataAccess() // <- CRUCIAL avec SQL Server
                .declareParameters(
                        new SqlParameter("id_livre",        Types.INTEGER),
                        new SqlParameter("titre",           Types.VARCHAR),
                        new SqlParameter("auteur",          Types.VARCHAR),
                        new SqlParameter("isbn",            Types.VARCHAR),
                        new SqlParameter("description",     Types.VARCHAR),
                        new SqlParameter("couverture",      Types.VARCHAR),
                        new SqlParameter("datePublication", Types.DATE),
                        new SqlParameter("nbExemplaires",   Types.INTEGER),
                        new SqlParameter("nbDisponibles",   Types.INTEGER),
                        new SqlParameter("id_categorie",    Types.INTEGER),
                        new SqlParameter("id_statut",       Types.INTEGER),
                        new SqlOutParameter("result_id",    Types.INTEGER)
                );

        Map<String, Object> params = new HashMap<>();
        params.put("id_livre",        livre.getId());
        params.put("titre",           livre.getTitre());
        params.put("auteur",          livre.getAuteur());
        params.put("isbn",            livre.getIsbn());
        params.put("description",     livre.getDescription());
        params.put("couverture",      livre.getCouverture());
        params.put("datePublication", livre.getDatePublication() != null
                ? Date.valueOf(livre.getDatePublication()) : null);
        params.put("nbExemplaires",   livre.getNbExemplaires());
        params.put("nbDisponibles",   livre.getNbDisponibles());
        params.put("id_categorie",    livre.getCategorie() != null
                ? livre.getCategorie().getId() : null);
        params.put("id_statut",       livre.getStatut() != null
                ? livre.getStatut().getId() : null);

        Map<String, Object> result = jdbcCall.execute(params);

        // SQL Server peut retourner la clé avec une casse différente
        Integer resultId = (Integer) result.get("result_id");
        if (resultId == null) {
            // Tentative avec casse différente (SQL Server)
            resultId = (Integer) result.get("RESULT_ID");
        }

        // Log pour déboguer si toujours null
        if (resultId == null) {
            System.err.println("Clés disponibles dans result : " + result.keySet());
            throw new BusinessException("Erreur lors de la sauvegarde du livre.");
        }

        return livreRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Livre introuvable après sauvegarde."));
    }

    // ── Lecture ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Livre> findAll() {
        return livreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Livre> findById(Integer id) {
        return livreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Livre> search(String query) {
        return livreRepository
                .findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                        query, query, query);
    }

    @Transactional(readOnly = true)
    public List<Livre> findDisponibles() {
        return livreRepository.findByNbDisponiblesGreaterThan(0);
    }

    @Transactional(readOnly = true)
    public List<Livre> rechercher(String titre, Integer idCategorie, Boolean disponible) {
        return livreRepository.rechercher(titre, idCategorie, disponible);
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

    // ── Suppression ────────────────────────────────────────────────────────

    /**
     * Supprime un livre (impossible s'il a des emprunts actifs).
     */
    public void deleteById(Integer id) {
        if (!livreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livre introuvable (id=" + id + ").");
        }
        if (livreRepository.hasEmpruntsActifs(id)) {
            throw new BusinessException(
                    "Impossible de supprimer ce livre : il a des emprunts en cours.");
        }
        livreRepository.deleteById(id);
    }
}