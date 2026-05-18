package com.example.backend.model.entity.dto;

public class DashboardStatsDto {
    private long totalLivres;
    private long livresDisponibles;
    private long livresEmpruntes;
    private long totalUtilisateurs;
    private long empruntsEnCours;
    private long empruntsEnRetard;
    private long reservationsEnAttente;

    public DashboardStatsDto(long totalLivres, long livresDisponibles, long livresEmpruntes,
                             long totalUtilisateurs, long empruntsEnCours,
                             long empruntsEnRetard, long reservationsEnAttente) {
        this.totalLivres = totalLivres;
        this.livresDisponibles = livresDisponibles;
        this.livresEmpruntes = livresEmpruntes;
        this.totalUtilisateurs = totalUtilisateurs;
        this.empruntsEnCours = empruntsEnCours;
        this.empruntsEnRetard = empruntsEnRetard;
        this.reservationsEnAttente = reservationsEnAttente;
    }

    public long getTotalLivres() { return totalLivres; }
    public long getLivresDisponibles() { return livresDisponibles; }
    public long getLivresEmpruntes() { return livresEmpruntes; }
    public long getTotalUtilisateurs() { return totalUtilisateurs; }
    public long getEmpruntsEnCours() { return empruntsEnCours; }
    public long getEmpruntsEnRetard() { return empruntsEnRetard; }
    public long getReservationsEnAttente() { return reservationsEnAttente; }
}
