package com.example.backend.controller;

import com.example.backend.dto.ChangerMotDePasseDto;
import com.example.backend.dto.ProfilUpdateDto;
import com.example.backend.dto.UtilisateurDto;
import com.example.backend.services.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/profil")
    public ResponseEntity<UtilisateurDto> getProfil(Authentication auth) {
        return ResponseEntity.ok(utilisateurService.recupererProfilParEmail(auth.getName()));
    }

    @PutMapping("/profil")
    public ResponseEntity<UtilisateurDto> updateProfil(@RequestBody ProfilUpdateDto dto,
                                                       Authentication auth) {
        return ResponseEntity.ok(utilisateurService.mettreAJourProfil(auth.getName(), dto));
    }

    @PutMapping("/mot-de-passe")
    public ResponseEntity<?> changerMotDePasse(@RequestBody ChangerMotDePasseDto dto,
                                               Authentication auth) {
        utilisateurService.changerMotDePasse(auth.getName(), dto);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès."));
    }

    @DeleteMapping("/compte")
    public ResponseEntity<?> supprimerCompte(@RequestBody Map<String, String> body,
                                             Authentication auth) {
        utilisateurService.supprimerCompte(auth.getName(), body.get("motDePasse"));
        return ResponseEntity.ok(Map.of("message", "Compte supprimé."));
    }
}
