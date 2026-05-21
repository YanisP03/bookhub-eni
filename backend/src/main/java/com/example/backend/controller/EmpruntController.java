package com.example.backend.controller;

import com.example.backend.model.entity.Emprunt;
import com.example.backend.services.EmpruntService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Emprunts", description = "Cycle de vie complet d'un emprunt : demande → validation → retour → confirmation")
@RestController
@RequestMapping("/api/emprunts")
public class EmpruntController {

    private final EmpruntService empruntService;

    public EmpruntController(EmpruntService empruntService) {
        this.empruntService = empruntService;
    }

    @Operation(summary = "Historique des emprunts du lecteur connecté")
    @GetMapping("/mes-emprunts")
    public List<Emprunt> getMesEmprunts(@AuthenticationPrincipal UserDetails user) {
        return empruntService.getMesEmprunts(user.getUsername());
    }

    @Operation(summary = "Demander un emprunt", description = "Crée une demande en statut DEMANDE. Max 3 emprunts actifs simultanés.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Demande créée"),
        @ApiResponse(responseCode = "400", description = "Limite atteinte ou livre indisponible")
    })
    @PostMapping("/{livreId}")
    public ResponseEntity<Emprunt> demander(
            @Parameter(description = "ID du livre à emprunter") @PathVariable Integer livreId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empruntService.demanderEmprunt(livreId, user.getUsername()));
    }

    @Operation(summary = "Signaler le retour d'un livre", description = "Passe l'emprunt en RETOUR_DEMANDE. Le bibliothécaire doit confirmer.")
    @PutMapping("/{id}/retour")
    public ResponseEntity<Emprunt> rendre(
            @Parameter(description = "ID de l'emprunt") @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empruntService.rendreLivre(id, user.getUsername()));
    }

    @Operation(summary = "Lister les demandes en attente de validation", description = "Réservé bibliothécaire/admin")
    @GetMapping("/demandes")
    public List<Emprunt> getDemandesEnAttente() {
        return empruntService.getDemandesEnAttente();
    }

    @Operation(summary = "Position dans la file d'attente")
    @GetMapping("/{id}/position")
    public ResponseEntity<Integer> getPosition(@PathVariable Integer id) {
        return ResponseEntity.ok(empruntService.getPositionFileAttente(id));
    }

    @Operation(summary = "Valider une demande d'emprunt", description = "Passe en EN_COURS et fixe la date de retour prévue à J+14")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Emprunt validé"),
        @ApiResponse(responseCode = "400", description = "Statut invalide")
    })
    @PutMapping("/{id}/valider")
    public ResponseEntity<Emprunt> valider(@PathVariable Integer id) {
        return ResponseEntity.ok(empruntService.validerEmprunt(id));
    }

    @Operation(summary = "Refuser une demande d'emprunt", description = "Passe en ANNULE et restitue l'exemplaire")
    @PutMapping("/{id}/refuser")
    public ResponseEntity<Emprunt> refuser(@PathVariable Integer id) {
        return ResponseEntity.ok(empruntService.refuserEmprunt(id));
    }

    @Operation(summary = "Lister les retours en attente de confirmation", description = "Réservé bibliothécaire/admin")
    @GetMapping("/retours")
    public List<Emprunt> getRetoursEnAttente() {
        return empruntService.getRetoursEnAttente();
    }

    @Operation(summary = "Confirmer le retour physique d'un livre", description = "Calcule le retard, libère l'exemplaire et notifie la 1ère réservation en attente")
    @PutMapping("/{id}/valider-retour")
    public ResponseEntity<Emprunt> validerRetour(@PathVariable Integer id) {
        return ResponseEntity.ok(empruntService.validerRetour(id)); 
    }

    @Operation(summary = "Convertir une réservation notifiée en emprunt", description = "Crée directement un emprunt EN_COURS pour le lecteur qui vient récupérer son livre")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Emprunt créé, livre remis"),
        @ApiResponse(responseCode = "400", description = "Réservation non en statut NOTIFIEE")
    })
    @PutMapping("/reservations/{id}/valider")
    public ResponseEntity<Emprunt> validerReservationNotifiee(@PathVariable Integer id) {
        return ResponseEntity.ok(empruntService.validerReservationNotifiee(id));
    }
}
