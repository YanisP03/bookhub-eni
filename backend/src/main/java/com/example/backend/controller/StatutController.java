package com.example.backend.controller;

import com.example.backend.model.entity.Statut;
import com.example.backend.repository.StatutRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/statuts")
public class StatutController {

    private final StatutRepository statutRepository;

    public StatutController(StatutRepository statutRepository) {
        this.statutRepository = statutRepository;
    }

    @GetMapping
    public List<Statut> findAll() { return statutRepository.findAll(); }
}
