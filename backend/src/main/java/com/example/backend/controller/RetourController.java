package com.example.backend.controller;

import com.example.backend.model.entity.Retour;
import com.example.backend.services.RetourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Retours", description = "Workflow alternatif de retour physique via l'entité Retour")
@RestController
@RequestMapping("/api/retours")
public class RetourController {

    private final RetourService retourService;

    public RetourController(RetourService retourService) {
        this.retourService = retourService;
    }

    @Operation(summary = "Enregistrer le retour physique d'un livre",
               description = "Calcule les jours de retard, met à jour le statut du livre et notifie la première réservation en attente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Retour enregistré"),
        @ApiResponse(responseCode = "400", description = "Livre déjà rendu"),
        @ApiResponse(responseCode = "404", description = "Retour introuvable")
    })
    @PostMapping("/{id}/valider")
    public ResponseEntity<Retour> valider(
            @Parameter(description = "ID du retour à enregistrer") @PathVariable Integer id) {
        return ResponseEntity.ok(retourService.enregistrerRetour(id));
    }
}
