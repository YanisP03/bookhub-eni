package com.example.backend.controller;

import com.example.backend.dto.DashboardStatsDto;
import com.example.backend.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Dashboard", description = "Statistiques et graphiques pour le tableau de bord admin/bibliothécaire")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Statistiques globales", description = "Totaux livres, emprunts en cours, retards, réservations en attente")
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @Operation(summary = "Évolution mensuelle des emprunts", description = "Nombre d'emprunts par mois sur les 6 derniers mois")
    @GetMapping("/evolution")
    public ResponseEntity<List<Map<String, Object>>> getEvolution() {
        return ResponseEntity.ok(dashboardService.getEvolutionMensuelle());
    }
}
