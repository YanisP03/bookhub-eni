package com.example.backend.controller;

import com.example.backend.dto.LivreDto;
import com.example.backend.model.entity.Livre;
import com.example.backend.services.LivreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Livres", description = "Catalogue, recherche et gestion des livres")
@RestController
@RequestMapping("/api/livres")
public class LivreController {

    private final LivreService livreService;

    public LivreController(LivreService livreService) {
        this.livreService = livreService;
    }

    @Operation(summary = "Lister tous les livres")
    @GetMapping
    public List<Livre> findAll() {
        return livreService.findAll();
    }

    @Operation(summary = "Obtenir un livre par son ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Livre trouvé"),
        @ApiResponse(responseCode = "404", description = "Livre introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Livre> findById(@Parameter(description = "ID du livre") @PathVariable Integer id) {
        Optional<Livre> livre = livreService.findById(id);
        if (livre.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(livre.get());
    }

    @Operation(summary = "Rechercher des livres par mot-clé")
    @GetMapping("/search")
    public List<Livre> search(@Parameter(description = "Mot-clé de recherche") @RequestParam String query) {
        return livreService.search(query);
    }

    @Operation(summary = "Livres disponibles uniquement")
    @GetMapping("/disponibles")
    public List<Livre> findDisponibles() {
        return livreService.findDisponibles();
    }

    @Operation(summary = "Top livres les plus empruntés")
    @GetMapping("/populaires")
    public List<Livre> findPopulaires(@Parameter(description = "Nombre de résultats") @RequestParam(defaultValue = "5") int limit) {
        return livreService.findPopulaires(limit);
    }

    @Operation(summary = "Livres les mieux notés")
    @GetMapping("/mieux-notes")
    public List<Livre> findMieuxNotes(@Parameter(description = "Nombre de résultats") @RequestParam(defaultValue = "4") int limit) {
        return livreService.findMieuxNotes(limit);
    }

    @Operation(summary = "Recherche avancée avec filtres optionnels")
    @GetMapping("/rechercher")
    public List<Livre> rechercher(
            @Parameter(description = "Filtre par titre") @RequestParam(required = false) String titre,
            @Parameter(description = "Filtre par catégorie") @RequestParam(required = false) Integer idCategorie,
            @Parameter(description = "Filtre disponibilité") @RequestParam(required = false) Boolean disponible) {
        return livreService.rechercher(titre, idCategorie, disponible);
    }

    @Operation(summary = "Ajouter un nouveau livre", description = "Réservé aux bibliothécaires et administrateurs")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Livre créé"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PostMapping
    public ResponseEntity<Livre> create(@RequestBody Livre livre) {
        livre.setId(null);
        return ResponseEntity.ok(livreService.saveLivre(livre));
    }

    @Operation(summary = "Modifier un livre existant", description = "Réservé aux bibliothécaires et administrateurs")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Livre mis à jour"),
        @ApiResponse(responseCode = "404", description = "Livre introuvable")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Livre> update(@PathVariable Integer id, @RequestBody Livre livre) {
        if (livreService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        livre.setId(id);
        return ResponseEntity.ok(livreService.saveLivre(livre));
    }

    @Operation(summary = "Supprimer un livre", description = "Impossible si des emprunts actifs existent")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Livre supprimé"),
        @ApiResponse(responseCode = "404", description = "Livre introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (livreService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        livreService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Détails complets d'un livre (DTO enrichi)")
    @GetMapping("/{id}/details")
    public ResponseEntity<LivreDto> getLivreDetail(@PathVariable Integer id) {
        return livreService.getLivreDetail(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
