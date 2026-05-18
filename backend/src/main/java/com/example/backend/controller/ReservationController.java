package com.example.backend.controller;

import com.example.backend.model.entity.Reservation;
import com.example.backend.services.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/mes-reservations")
    public List<Reservation> getMesReservations(@AuthenticationPrincipal UserDetails user) {
        return reservationService.getMesReservations(user.getUsername());
    }

    @PostMapping("/{livreId}")
    public ResponseEntity<Reservation> reserver(@PathVariable Integer livreId,
                                                @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reservationService.reserverLivre(livreId, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(@PathVariable Integer id,
                                        @AuthenticationPrincipal UserDetails user) {
        reservationService.annulerReservation(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
