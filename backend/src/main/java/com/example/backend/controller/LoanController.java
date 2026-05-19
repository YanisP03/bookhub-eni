package com.example.backend.controller;

import com.example.backend.model.entity.Retour;
import com.example.backend.services.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Retour> returnBook(@PathVariable Integer id) {
        return ResponseEntity.ok(loanService.enregistrerRetour(id));
    }
}
