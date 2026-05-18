package com.example.backend.controller;

import com.example.backend.model.entity.Emprunt;
import com.example.backend.services.EmpruntService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emprunts")
public class EmpruntController {

    private final EmpruntService empruntService;

    public EmpruntController(EmpruntService empruntService) {
        this.empruntService = empruntService;
    }

    @GetMapping("/mes-emprunts")
    public List<Emprunt> getMesEmprunts(@AuthenticationPrincipal UserDetails user) {
        return empruntService.getMesEmprunts(user.getUsername());
    }

    @PostMapping("/{livreId}")
    public ResponseEntity<Emprunt> demander(@PathVariable Integer livreId,
                                            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empruntService.demanderEmprunt(livreId, user.getUsername()));
    }

    @PutMapping("/{id}/retour")
    public ResponseEntity<Emprunt> rendre(@PathVariable Integer id,
                                          @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empruntService.rendreLivre(id, user.getUsername()));
    }

    // ── Endpoints bibliothécaire ────────────────────────────────────────────

    @GetMapping("/demandes")
    public List<Emprunt> getDemandesEnAttente() {
        return empruntService.getDemandesEnAttente();
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<Emprunt> valider(@PathVariable Integer id) {
        return ResponseEntity.ok(empruntService.validerEmprunt(id));
    }

    @PutMapping("/{id}/refuser")
    public ResponseEntity<Emprunt> refuser(@PathVariable Integer id) {
        return ResponseEntity.ok(empruntService.refuserEmprunt(id));
    }
}
