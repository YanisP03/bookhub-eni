package com.example.backend.controller;

import com.example.backend.model.dto.LivreDto;
import com.example.backend.model.entity.Livre;
import com.example.backend.services.LivreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/livres")
@CrossOrigin(origins = "http://localhost:4200")
public class LivreController {

    private final LivreService livreService;

    public LivreController(LivreService livreService) {
        this.livreService = livreService;
    }

    @GetMapping
    public List<Livre> findAll() {
        return livreService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livre> findById(@PathVariable Integer id) {
        Optional<Livre> livre = livreService.findById(id);
        if (livre.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(livre.get());
    }

    @GetMapping("/search")
    public List<Livre> search(@RequestParam String query) {
        return livreService.search(query);
    }

    @GetMapping("/disponibles")
    public List<Livre> findDisponibles() {
        return livreService.findDisponibles();
    }

    @GetMapping("/rechercher")
    public List<Livre> rechercher(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) Integer idCategorie,
            @RequestParam(required = false) Boolean disponible) {
        return livreService.rechercher(titre, idCategorie, disponible);
    }

    @PostMapping
    public ResponseEntity<Livre> create(@RequestBody Livre livre) {
        Livre saved = livreService.saveLivre(livre);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livre> update(@PathVariable Integer id, @RequestBody Livre livre) {
        if (livreService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        livre.setId(id);
        Livre saved = livreService.saveLivre(livre);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (livreService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        livreService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<LivreDto> getLivreDetail(@PathVariable Integer id) {
        return livreService.getLivreDetail(id)
                .map(ResponseEntity::ok) // Si le livre existe (Optional présent), on renvoie 200 OK avec le DTO
                .orElse(ResponseEntity.notFound().build()); // Sinon on renvoie 404 Not Found
    }
}