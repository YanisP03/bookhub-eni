package com.example.backend.controller;

import com.example.backend.dto.AvisRequestDTO;
import com.example.backend.dto.AvisResponseDTO;
import com.example.backend.services.AvisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avis")
public class AvisController {

    private final AvisService avisService;

    public AvisController(AvisService avisService) {
        this.avisService = avisService;
    }

    // POST /api/avis — soumettre ou modifier son avis (upsert via procédure)
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AvisResponseDTO> soumettreAvis(
            @Valid @RequestBody AvisRequestDTO dto,
            Authentication auth) {

        Integer idUtilisateur = getIdFromAuth(auth);
        return ResponseEntity.ok(avisService.soumettreAvis(idUtilisateur, dto));
    }

    // GET /api/avis/mes-avis — mes avis (utilisateur connecté)
    @GetMapping("/mes-avis")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AvisResponseDTO>> getMesAvis(Authentication auth) {
        Integer idUtilisateur = getIdFromAuth(auth);
        return ResponseEntity.ok(avisService.getMesAvis(idUtilisateur));
    }

    // GET /api/avis/livre/{idLivre} — avis approuvés d'un livre (public)
    @GetMapping("/livre/{idLivre}")
    public ResponseEntity<List<AvisResponseDTO>> getAvisDuLivre(@PathVariable Integer idLivre) {
        return ResponseEntity.ok(avisService.getAvisApprouvesDuLivre(idLivre));
    }

    // GET /api/avis/moderation — avis en attente (bibliothécaire)
    @GetMapping("/moderation")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<List<AvisResponseDTO>> getAvisEnAttente() {
        return ResponseEntity.ok(avisService.getAvisEnAttente());
    }

    // PATCH /api/avis/{id}/moderer?decision=APPROUVE|REJETE
    @PatchMapping("/{id}/moderer")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<AvisResponseDTO> modererAvis(
            @PathVariable Integer id,
            @RequestParam String decision) {
        return ResponseEntity.ok(avisService.modererAvis(id, decision));
    }

    // ── utilitaire pour extraire l'id depuis le JWT ──────────────────────────
    private Integer getIdFromAuth(Authentication auth) {
        // À adapter selon votre implémentation de UserDetails
        // Exemple si votre UserDetails stocke l'id :
        // return ((CustomUserDetails) auth.getPrincipal()).getId();
        throw new UnsupportedOperationException("Adapter selon votre UserDetails");
    }
}