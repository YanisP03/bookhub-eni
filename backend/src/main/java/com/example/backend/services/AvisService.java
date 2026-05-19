package com.example.backend.services;

import com.example.backend.dto.AvisRequestDTO;
import com.example.backend.dto.AvisResponseDTO;
import com.example.backend.model.entity.Avis;
import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Utilisateur;
import com.example.backend.repository.AvisRepository;
import com.example.backend.repository.LivreRepository;
import com.example.backend.repository.UtilisateurRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvisService {

    private final AvisRepository avisRepository;
    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AvisService(AvisRepository avisRepository,
                       LivreRepository livreRepository,
                       UtilisateurRepository utilisateurRepository) {
        this.avisRepository = avisRepository;
        this.livreRepository = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // ── Soumettre un avis (INSERT ou UPDATE via procédure stockée) ──────────
    @Transactional
    public AvisResponseDTO soumettreAvis(Integer idUtilisateur, AvisRequestDTO dto) {

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Livre livre = livreRepository.findById(dto.getIdLivre())
                .orElseThrow(() -> new RuntimeException("Livre introuvable"));

        // Appel de la procédure stockée (elle vérifie la réservation + fait l'upsert)
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("sp_UpsertAvis")
                .registerStoredProcedureParameter("p_id_utilisateur", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_id_livre",       Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_pseudo",         String.class,  ParameterMode.IN)
                .registerStoredProcedureParameter("p_note",           Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_commentaire",    String.class,  ParameterMode.IN)
                .setParameter("p_id_utilisateur", idUtilisateur)
                .setParameter("p_id_livre",       dto.getIdLivre())
                .setParameter("p_pseudo",         dto.getPseudo())
                .setParameter("p_note",           dto.getNote())
                .setParameter("p_commentaire",    dto.getCommentaire());

        query.execute(); // lève une exception SQL si la règle métier échoue

        // Recharger l'avis pour construire la réponse
        Avis avis = avisRepository.findByUtilisateurAndLivre(utilisateur, livre)
                .orElseThrow(() -> new RuntimeException("Avis introuvable après upsert"));

        return toResponseDTO(avis);
    }

    // ── Mes avis (espace utilisateur) ────────────────────────────────────────
    public List<AvisResponseDTO> getMesAvis(Integer idUtilisateur) {
        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return avisRepository.findByUtilisateurOrderByDatePublicationDesc(utilisateur)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── Avis approuvés d'un livre (affichage public) ─────────────────────────
    public List<AvisResponseDTO> getAvisApprouvesDuLivre(Integer idLivre) {
        Livre livre = livreRepository.findById(idLivre)
                .orElseThrow(() -> new RuntimeException("Livre introuvable"));

        return avisRepository.findByLivreAndStatutOrderByDatePublicationDesc(livre, "APPROUVE")
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── Modération : avis en attente (bibliothécaire) ────────────────────────
    public List<AvisResponseDTO> getAvisEnAttente() {
        return avisRepository.findByStatutOrderByDatePublicationAsc("EN_ATTENTE")
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvisResponseDTO modererAvis(Integer idAvis, String decision) {
        if (!decision.equals("APPROUVE") && !decision.equals("REJETE")) {
            throw new IllegalArgumentException("Décision invalide : APPROUVE ou REJETE attendu");
        }

        Avis avis = avisRepository.findById(idAvis)
                .orElseThrow(() -> new RuntimeException("Avis introuvable"));

        avis.setStatut(decision);
        return toResponseDTO(avisRepository.save(avis));
    }

    // ── Mapper entité → DTO ──────────────────────────────────────────────────
    private AvisResponseDTO toResponseDTO(Avis avis) {
        AvisResponseDTO dto = new AvisResponseDTO();
        dto.setId(avis.getId());
        dto.setPseudo(avis.getPseudo() != null ? avis.getPseudo()
                : avis.getUtilisateur().getNom()); // fallback sur le vrai nom
        dto.setNote(avis.getNote());
        dto.setCommentaire(avis.getCommentaire());
        dto.setDatePublication(avis.getDatePublication());
        dto.setStatut(avis.getStatut());
        dto.setIdLivre(avis.getLivre().getId());
        dto.setNomLivre(avis.getLivre().getTitre());
        dto.setNomUtilisateur(avis.getUtilisateur().getNom());
        return dto;
    }
}