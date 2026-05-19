package com.example.backend.controller;

import com.example.backend.dto.LivreDto;
import com.example.backend.model.entity.Livre;
import com.example.backend.services.LivreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des livres.
 * Expose les endpoints API liés aux opérations CRUD et aux recherches.
 */
@RestController
@RequestMapping("/api/livres")
public class LivreController {

    // Service contenant la logique métier des livres
    private final LivreService livreService;

    /**
     * Injection du service LivreService
     */
    public LivreController(LivreService livreService) {
        this.livreService = livreService;
    }

    /**
     * Récupérer tous les livres
     * Endpoint : GET /api/livres
     *
     * @return liste complète des livres
     */
    @GetMapping
    public List<Livre> findAll() {
        return livreService.findAll();
    }

    /**
     * Récupérer un livre par son identifiant
     * Endpoint : GET /api/livres/{id}
     *
     * @param id identifiant du livre
     * @return le livre si trouvé, sinon erreur 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Livre> findById(@PathVariable Integer id) {
        Optional<Livre> livre = livreService.findById(id);
        // Retourne 404 si le livre n'existe pas
        if (livre.isEmpty()) return ResponseEntity.notFound().build();
        // Retourne le livre trouvé
        return ResponseEntity.ok(livre.get());
    }

    /**
     * Rechercher un livre par mot-clé
     * Endpoint : GET /api/livres/search?query=...
     *
     * @param query mot-clé de recherche
     * @return liste des livres correspondants
     */
    @GetMapping("/search")
    public List<Livre> search(@RequestParam String query) {
        return livreService.search(query);
    }

    /**
     * Récupérer uniquement les livres disponibles
     * Endpoint : GET /api/livres/disponibles
     *
     * @return liste des livres disponibles
     */
    @GetMapping("/disponibles")
    public List<Livre> findDisponibles() {
        return livreService.findDisponibles();
    }

    @GetMapping("/populaires")
    public List<Livre> findPopulaires(@RequestParam(defaultValue = "5") int limit) {
        return livreService.findPopulaires(limit);
    }

    @GetMapping("/mieux-notes")
    public List<Livre> findMieuxNotes(@RequestParam(defaultValue = "4") int limit) {
        return livreService.findMieuxNotes(limit);
    }

    /**
     * Recherche avancée avec filtres optionnels
     * Endpoint : GET /api/livres/rechercher
     *
     * Exemple :
     * /api/livres/rechercher?titre=Java&idCategorie=1&disponible=true
     *
     * @param titre filtre par titre
     * @param idCategorie filtre par catégorie
     * @param disponible filtre disponibilité
     * @return liste filtrée des livres
     */
    @GetMapping("/rechercher")
    public List<Livre> rechercher(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) Integer idCategorie,
            @RequestParam(required = false) Boolean disponible) {
        return livreService.rechercher(titre, idCategorie, disponible);
    }

    /**
     * Ajouter un nouveau livre
     * Endpoint : POST /api/livres
     *
     * @param livre objet livre reçu dans le body
     * @return livre enregistré
     */
    @PostMapping
    public ResponseEntity<Livre> create(@RequestBody Livre livre) {
        livre.setId(null); // forcer INSERT : la SP doit recevoir NULL, pas 0
        Livre saved = livreService.saveLivre(livre);
        return ResponseEntity.ok(saved);
    }

    /**
     * Modifier un livre existant
     * Endpoint : PUT /api/livres/{id}
     *
     * @param id identifiant du livre à modifier
     * @param livre nouvelles données
     * @return livre mis à jour ou 404 si absent
     */
    @PutMapping("/{id}")
    public ResponseEntity<Livre> update(@PathVariable Integer id, @RequestBody Livre livre) {
        // Vérifie si le livre existe
        if (livreService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        // Affecte l'id pour éviter une création accidentelle
        livre.setId(id);
        Livre saved = livreService.saveLivre(livre);
        return ResponseEntity.ok(saved);
    }

    /**
     * Supprimer un livre
     * Endpoint : DELETE /api/livres/{id}
     *
     * @param id identifiant du livre
     * @return 204 si suppression réussie, 404 sinon
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        // Vérifie si le livre existe avant suppression
        if (livreService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        livreService.deleteById(id);
        // Retour HTTP 204 = suppression réussie sans contenu
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer les détails complets d'un livre au format DTO
     * Endpoint : GET /api/livres/{id}/details
     *
     * Utilise un DTO pour retourner des informations enrichies
     * sans exposer directement l'entité.
     *
     * @param id identifiant du livre
     * @return DTO du livre ou erreur 404
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<LivreDto> getLivreDetail(@PathVariable Integer id) {
        return livreService.getLivreDetail(id)
                .map(ResponseEntity::ok) // Si le livre existe (Optional présent), on renvoie 200 OK avec le DTO
                .orElse(ResponseEntity.notFound().build()); // Sinon on renvoie 404 Not Found
    }
}