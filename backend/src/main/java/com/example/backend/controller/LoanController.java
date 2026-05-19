package com.example.backend.controller;

import com.example.backend.model.entity.Loan;
import com.example.backend.services.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*") // Permet à ton FrontEnd de communiquer avec sans blocage CORS
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // L'endpoint pour rendre un livre : POST http://localhost:8080/api/loans/{id}/return
    @PostMapping("/{id}/return")
    public ResponseEntity<Loan> returnBook(@PathVariable Integer id) {
        Loan loanModifie = loanService.enregistrerRetour(id);
        return ResponseEntity.ok(loanModifie);
    }
}