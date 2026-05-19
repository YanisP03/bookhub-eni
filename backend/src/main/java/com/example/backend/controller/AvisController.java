package com.example.backend.controller;

import com.example.backend.dto.AvisRequestDTO;
import com.example.backend.dto.AvisResponseDTO;
import com.example.backend.services.AvisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/livre/{idLivre}")
    public ResponseEntity<List<AvisResponseDTO>> getAvisDuLivre(@PathVariable Integer idLivre) {
        return ResponseEntity.ok(avisService.getAvisApprouvesDuLivre(idLivre));
    }

    @PostMapping
    public ResponseEntity<AvisResponseDTO> soumettreAvis(@Valid @RequestBody AvisRequestDTO dto,
                                                          Authentication auth) {
        return ResponseEntity.ok(avisService.soumettreAvis(auth.getName(), dto));
    }

    @GetMapping("/mes-avis")
    public ResponseEntity<List<AvisResponseDTO>> getMesAvis(Authentication auth) {
        return ResponseEntity.ok(avisService.getMesAvis(auth.getName()));
    }

    @GetMapping("/moderation")
    public ResponseEntity<List<AvisResponseDTO>> getAvisEnAttente() {
        return ResponseEntity.ok(avisService.getAvisEnAttente());
    }

    @PatchMapping("/{id}/moderer")
    public ResponseEntity<AvisResponseDTO> modererAvis(@PathVariable Integer id,
                                                        @RequestParam String decision) {
        return ResponseEntity.ok(avisService.modererAvis(id, decision));
    }
}
