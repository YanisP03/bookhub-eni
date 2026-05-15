package com.example.backend.controller;

import com.example.backend.model.entity.Categorie;
import com.example.backend.repository.CategorieRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    private final CategorieRepository categorieRepository;

    public CategorieController(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    @GetMapping
    public List<Categorie> findAll() { return categorieRepository.findAll(); }
}