package com.example.backend.services;

import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.entity.Avis;
import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Utilisateur;
import com.example.backend.model.entity.dto.AvisRequestDTO;
import com.example.backend.model.entity.dto.AvisResponseDTO;
import com.example.backend.repository.AvisRepository;
import com.example.backend.repository.LivreRepository;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AvisService {

    private final AvisRepository avisRepository;
    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;

    public AvisService(AvisRepository avisRepository,
                       LivreRepository livreRepository,
                       UtilisateurRepository utilisateurRepository) {
        this.avisRepository = avisRepository;
        this.livreRepository = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public AvisResponseDTO soumettreAvis(String email, AvisRequestDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findByMail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Livre livre = livreRepository.findById(dto.getIdLivre())
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable"));

        Optional<Avis> existingOpt = avisRepository.findByUtilisateurAndLivre(utilisateur, livre);

        Avis avis;
        if (existingOpt.isPresent()) {
            // Mise à jour de l'avis existant — repasse en attente de modération
            avis = existingOpt.get();
            avis.setNote(dto.getNote());
            avis.setCommentaire(dto.getCommentaire());
            if (dto.getPseudo() != null && !dto.getPseudo().isBlank()) {
                avis.setPseudo(dto.getPseudo());
            }
            avis.setStatut("EN_ATTENTE");
        } else {
            avis = new Avis();
            avis.setLivre(livre);
            avis.setUtilisateur(utilisateur);
            avis.setNote(dto.getNote());
            avis.setCommentaire(dto.getCommentaire());
            avis.setPseudo(dto.getPseudo() != null && !dto.getPseudo().isBlank()
                    ? dto.getPseudo()
                    : utilisateur.getPrenom() + " " + utilisateur.getNom().charAt(0) + ".");
            avis.setStatut("EN_ATTENTE");
        }

        return toResponseDTO(avisRepository.save(avis));
    }

    @Transactional(readOnly = true)
    public List<AvisResponseDTO> getMesAvis(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByMail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return avisRepository.findByUtilisateurOrderByDatePublicationDesc(utilisateur)
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvisResponseDTO> getAvisApprouvesDuLivre(Integer idLivre) {
        Livre livre = livreRepository.findById(idLivre)
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable"));
        return avisRepository.findByLivreAndStatutOrderByDatePublicationDesc(livre, "APPROUVE")
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvisResponseDTO> getAvisEnAttente() {
        return avisRepository.findByStatutOrderByDatePublicationAsc("EN_ATTENTE")
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public AvisResponseDTO modererAvis(Integer idAvis, String decision) {
        if (!decision.equals("APPROUVE") && !decision.equals("REJETE"))
            throw new BusinessException("Décision invalide : APPROUVE ou REJETE attendu");

        Avis avis = avisRepository.findById(idAvis)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));
        avis.setStatut(decision);
        AvisResponseDTO result = toResponseDTO(avisRepository.save(avis));

        // Recalcule la note moyenne du livre après modération
        livreRepository.recalculerNoteMoyenne(avis.getLivre().getId());

        return result;
    }

    private AvisResponseDTO toResponseDTO(Avis avis) {
        AvisResponseDTO dto = new AvisResponseDTO();
        dto.setId(avis.getId());
        dto.setPseudo(avis.getPseudo() != null ? avis.getPseudo() : avis.getUtilisateur().getNom());
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
