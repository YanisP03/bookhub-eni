package com.example.backend.services;

import com.example.backend.model.entity.Emprunt;
import com.example.backend.model.entity.Statut;
import com.example.backend.dto.DashboardStatsDto;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmpruntRepository empruntRepository;
    private final ReservationRepository reservationRepository;
    private final StatutRepository statutRepository;

    public DashboardService(LivreRepository livreRepository,
                            UtilisateurRepository utilisateurRepository,
                            EmpruntRepository empruntRepository,
                            ReservationRepository reservationRepository,
                            StatutRepository statutRepository) {
        this.livreRepository = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.empruntRepository = empruntRepository;
        this.reservationRepository = reservationRepository;
        this.statutRepository = statutRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats() {
        long totalLivres = livreRepository.count();
        long livresDisponibles = livreRepository.findByNbDisponiblesGreaterThan(0).size();
        long livresEmpruntes = totalLivres - livresDisponibles;
        long totalUtilisateurs = utilisateurRepository.count();

        Statut enCours = statutRepository.findFirstByLibelle("EN_COURS").orElse(null);
        Statut enAttente = statutRepository.findFirstByLibelle("EN_ATTENTE").orElse(null);

        long empruntsEnCours = 0;
        long empruntsEnRetard = 0;

        if (enCours != null) {
            List<Emprunt> actifs = empruntRepository.findByStatut(enCours);
            empruntsEnCours = actifs.size();
            empruntsEnRetard = actifs.stream()
                    .filter(e -> e.getDateRetour() == null && LocalDateTime.now().isAfter(e.getDateRetourPrevue()))
                    .count();
        }

        long reservationsEnAttente = enAttente != null
                ? reservationRepository.findAll().stream()
                    .filter(r -> r.getStatut() != null && "EN_ATTENTE".equals(r.getStatut().getLibelle()))
                    .count()
                : 0;

        return new DashboardStatsDto(
                totalLivres, livresDisponibles, livresEmpruntes,
                totalUtilisateurs, empruntsEnCours, empruntsEnRetard, reservationsEnAttente
        );
    }
}
