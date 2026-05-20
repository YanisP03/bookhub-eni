package com.example.backend.controller;

import com.example.backend.dto.AvisRequestDTO;
import com.example.backend.dto.AvisResponseDTO;
import com.example.backend.services.AvisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Avis", description = "Soumission et modération des avis sur les livres")
@RestController
@RequestMapping("/api/avis")
public class AvisController {

    private final AvisService avisService;

    public AvisController(AvisService avisService) {
        this.avisService = avisService;
    }

    @Operation(summary = "Avis approuvés d'un livre", description = "Accessible sans authentification")
    @GetMapping("/livre/{idLivre}")
    public ResponseEntity<List<AvisResponseDTO>> getAvisDuLivre(
            @Parameter(description = "ID du livre") @PathVariable Integer idLivre) {
        return ResponseEntity.ok(avisService.getAvisApprouvesDuLivre(idLivre));
    }

    @Operation(summary = "Soumettre un avis", description = "Possible uniquement après avoir rendu le livre. Soumis en modération.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Avis soumis (EN_ATTENTE de modération)"),
        @ApiResponse(responseCode = "400", description = "Livre non emprunté/rendu")
    })
    @PostMapping
    public ResponseEntity<AvisResponseDTO> soumettreAvis(@Valid @RequestBody AvisRequestDTO dto,
                                                          Authentication auth) {
        return ResponseEntity.ok(avisService.soumettreAvis(auth.getName(), dto));
    }

    @Operation(summary = "Mes avis", description = "Tous les avis du lecteur connecté")
    @GetMapping("/mes-avis")
    public ResponseEntity<List<AvisResponseDTO>> getMesAvis(Authentication auth) {
        return ResponseEntity.ok(avisService.getMesAvis(auth.getName()));
    }

    @Operation(summary = "Avis en attente de modération", description = "Réservé bibliothécaire/admin")
    @GetMapping("/moderation")
    public ResponseEntity<List<AvisResponseDTO>> getAvisEnAttente() {
        return ResponseEntity.ok(avisService.getAvisEnAttente());
    }

    @Operation(summary = "Modérer un avis", description = "Décision : APPROUVE ou REJETE. Recalcule la note moyenne du livre si approuvé.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Avis modéré"),
        @ApiResponse(responseCode = "400", description = "Décision invalide")
    })
    @PatchMapping("/{id}/moderer")
    public ResponseEntity<AvisResponseDTO> modererAvis(
            @Parameter(description = "ID de l'avis") @PathVariable Integer id,
            @Parameter(description = "APPROUVE ou REJETE") @RequestParam String decision) {
        return ResponseEntity.ok(avisService.modererAvis(id, decision));
    }
}
