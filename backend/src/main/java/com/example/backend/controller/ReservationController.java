package com.example.backend.controller;

import com.example.backend.model.entity.Reservation;
import com.example.backend.services.ReservationService;
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

@Tag(name = "Réservations", description = "File d'attente pour les livres indisponibles")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(summary = "Réservations du lecteur connecté")
    @GetMapping("/mes-reservations")
    public List<Reservation> getMesReservations(@AuthenticationPrincipal UserDetails user) {
        return reservationService.getMesReservations(user.getUsername());
    }

    @Operation(summary = "File d'attente globale (EN_ATTENTE)", description = "Réservé bibliothécaire/admin")
    @GetMapping("/file-attente")
    public List<Reservation> getFileAttente() {
        return reservationService.getFileAttente();
    }

    @Operation(summary = "Réservations notifiées (livre disponible, lecteur à contacter)", description = "Réservé bibliothécaire/admin")
    @GetMapping("/notifiees")
    public List<Reservation> getReservationsNotifiees() {
        return reservationService.getReservationsNotifiees();
    }

    @Operation(summary = "Réserver un livre", description = "Uniquement pour les livres sans exemplaire disponible. Place le lecteur en file d'attente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Réservation créée"),
        @ApiResponse(responseCode = "400", description = "Livre disponible (emprunter directement) ou réservation déjà active")
    })
    @PostMapping("/{livreId}")
    public ResponseEntity<Reservation> reserver(
            @Parameter(description = "ID du livre à réserver") @PathVariable Integer livreId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reservationService.reserverLivre(livreId, user.getUsername()));
    }

    @Operation(summary = "Annuler une réservation", description = "Recalcule les positions des réservations suivantes")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Réservation annulée"),
        @ApiResponse(responseCode = "403", description = "Cette réservation ne vous appartient pas")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(
            @Parameter(description = "ID de la réservation") @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        reservationService.annulerReservation(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
