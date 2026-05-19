package com.example.backend.controller;

import com.example.backend.model.entity.Retour;
import com.example.backend.services.RetourService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/retours")
public class RetourController {

    private final RetourService retourService;

    public RetourController(RetourService retourService) {
        this.retourService = retourService;
    }

    @PostMapping("/{id}/valider")
    public ResponseEntity<Retour> valider(@PathVariable Integer id) {
        return ResponseEntity.ok(retourService.enregistrerRetour(id));
    }
}
