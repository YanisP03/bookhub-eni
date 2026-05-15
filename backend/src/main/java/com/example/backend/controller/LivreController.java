package com.example.backend.controller;

import com.example.backend.model.entity.Livre;
import com.example.backend.services.LivreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/livres")
public class LivreController {

    private final LivreService livreService;

    public LivreController(LivreService livreService) {
        this.livreService = livreService;
    }

    @GetMapping
    public List<Livre> findAll() { return livreService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Livre> findById(@PathVariable Integer id) {
        return livreService.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Livre> search(@RequestParam String query) {
        return livreService.search(query);
    }

    @GetMapping("/disponibles")
    public List<Livre> findDisponibles() { return livreService.findDisponibles(); }

    @GetMapping("/rechercher")
    public List<Livre> rechercher(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) Integer idCategorie,
            @RequestParam(required = false) Boolean disponible) {
        return livreService.rechercher(titre, idCategorie, disponible);
    }

    @PostMapping
    public Livre create(@RequestBody Livre livre) { return livreService.saveLivre(livre); }

    @PutMapping("/{id}")
    public ResponseEntity<Livre> update(@PathVariable Integer id, @RequestBody Livre livre) {
        return livreService.findById(id).map(e -> {
            livre.setId(id);
            return ResponseEntity.ok(livreService.saveLivre(livre));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return livreService.findById(id).map(l -> {
            livreService.deleteById(id);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}