package com.example.backend.controller;

import com.example.backend.dto.ChangerMotDePasseDto;
import com.example.backend.dto.CreerUtilisateurDto;
import com.example.backend.dto.ProfilUpdateDto;
import com.example.backend.dto.RegisterRequestDto;
import com.example.backend.dto.UtilisateurDto;
import com.example.backend.services.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Utilisateurs", description = "Profil, gestion de compte et administration des utilisateurs")
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @Operation(summary = "Mon profil", description = "Retourne le profil de l'utilisateur connecté")
    @GetMapping("/profil")
    public ResponseEntity<UtilisateurDto> getProfil(Authentication auth) {
        return ResponseEntity.ok(utilisateurService.recupererProfilParEmail(auth.getName()));
    }

    @Operation(summary = "Mettre à jour mon profil")
    @PutMapping("/profil")
    public ResponseEntity<UtilisateurDto> updateProfil(@RequestBody ProfilUpdateDto dto, Authentication auth) {
        return ResponseEntity.ok(utilisateurService.mettreAJourProfil(auth.getName(), dto));
    }

    @Operation(summary = "Changer mon mot de passe", description = "Vérifie l'ancien mot de passe avant de changer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mot de passe modifié"),
        @ApiResponse(responseCode = "400", description = "Ancien mot de passe incorrect")
    })
    @PutMapping("/mot-de-passe")
    public ResponseEntity<?> changerMotDePasse(@RequestBody ChangerMotDePasseDto dto, Authentication auth) {
        utilisateurService.changerMotDePasse(auth.getName(), dto);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès."));
    }

    @Operation(summary = "Supprimer mon compte", description = "Supprime le compte et toutes les données associées après vérification du mot de passe")
    @DeleteMapping("/compte")
    public ResponseEntity<?> supprimerCompte(@RequestBody Map<String, String> body, Authentication auth) {
        utilisateurService.supprimerCompte(auth.getName(), body.get("motDePasse"));
        return ResponseEntity.ok(Map.of("message", "Compte supprimé."));
    }

    @Operation(summary = "Créer un compte bibliothécaire", description = "Réservé admin")
    @PostMapping("/bibliothecaire")
    public ResponseEntity<UtilisateurDto> creerBibliothecaire(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creerBibliothecaire(dto));
    }

    @Operation(summary = "Promouvoir un lecteur en bibliothécaire", description = "Réservé admin")
    @PutMapping("/bibliothecaire/promouvoir")
    public ResponseEntity<UtilisateurDto> promouvoirBibliothecaire(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(utilisateurService.promouvoirBibliothecaire(body.get("mail")));
    }

    @Operation(summary = "Lister les utilisateurs avec retards ou bloqués", description = "Réservé bibliothécaire/admin")
    @GetMapping("/retardataires")
    public ResponseEntity<List<UtilisateurDto>> listerRetardataires() {
        return ResponseEntity.ok(utilisateurService.listerRetardataires());
    }

    @Operation(summary = "Débloquer un compte suspendu", description = "Réinitialise le blocage et les retards. Réservé admin.")
    @ApiResponse(responseCode = "200", description = "Compte débloqué")
    @PutMapping("/{id}/debloquer")
    public ResponseEntity<UtilisateurDto> debloquer(
            @Parameter(description = "ID de l'utilisateur à débloquer") @PathVariable Integer id) {
        return ResponseEntity.ok(utilisateurService.debloquerUtilisateur(id));
    }

    @Operation(summary = "Lister tous les utilisateurs", description = "Réservé admin")
    @GetMapping
    public ResponseEntity<List<UtilisateurDto>> listerTous() {
        return ResponseEntity.ok(utilisateurService.listerTous());
    }

    @Operation(summary = "Supprimer un compte utilisateur", description = "Supprime l'utilisateur et toutes ses données. Réservé admin.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerParId(
            @Parameter(description = "ID de l'utilisateur à supprimer") @PathVariable Integer id) {
        utilisateurService.supprimerParId(id);
        return ResponseEntity.ok(Map.of("message", "Compte supprimé."));
    }

    @Operation(summary = "Créer un utilisateur (lecteur ou bibliothécaire)", description = "Réservé admin")
    @PostMapping("/admin/creer")
    public ResponseEntity<UtilisateurDto> creerUtilisateur(@Valid @RequestBody CreerUtilisateurDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creerUtilisateur(dto));
    }
}
