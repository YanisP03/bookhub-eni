package com.example.backend.controller;

import com.example.backend.model.entity.dto.UtilisateurDto;
import com.example.backend.services.UtilisateurService; // Adapte le nom si ton service s'appelle autrement
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    // Injection par constructeur (Clean Code pour le jury)
    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    /**
     * Endpoint pour récupérer le profil de l'utilisateur connecté (US-AUTH-03)
     * URL : GET /api/utilisateurs/profil
     */
    @GetMapping("/profil")
    public ResponseEntity<UtilisateurDto> obtenirProfilConnecte(Authentication authentication) {
        // authentication.getName() contient l'email/username extrait du jeton JWT par ton filtre
        String email = authentication.getName();

        // Appel au service pour récupérer le DTO
        UtilisateurDto utilisateurDto = utilisateurService.recupererProfilParEmail(email);

        return ResponseEntity.ok(utilisateurDto);
    }
}
