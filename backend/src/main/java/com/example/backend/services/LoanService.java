package com.example.backend.services;

import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Reservation;
import com.example.backend.model.entity.Retour;
import com.example.backend.model.entity.Statut;
import com.example.backend.repository.LivreRepository;
import com.example.backend.repository.ReservationRepository;
import com.example.backend.repository.RetourRepository;
import com.example.backend.repository.StatutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @deprecated Utilisez RetourService à la place.
 */
@Deprecated
@Service
public class LoanService {

    private final RetourRepository retourRepository;
    private final ReservationRepository reservationRepository;
    private final LivreRepository livreRepository;
    private final StatutRepository statutRepository;

    public LoanService(RetourRepository retourRepository,
                       ReservationRepository reservationRepository,
                       LivreRepository livreRepository,
                       StatutRepository statutRepository) {
        this.retourRepository = retourRepository;
        this.reservationRepository = reservationRepository;
        this.livreRepository = livreRepository;
        this.statutRepository = statutRepository;
    }

    @Transactional
    public Retour enregistrerRetour(Integer retourId) {
        Retour retour = retourRepository.findById(retourId)
                .orElseThrow(() -> new RuntimeException("Retour introuvable"));

        if (retour.getDateRetourReelle() != null)
            throw new IllegalStateException("Ce livre a déjà été rendu.");

        LocalDateTime now = LocalDateTime.now();
        retour.setDateRetourReelle(now);

        if (now.isAfter(retour.getDateRetourPrevue())) {
            long retard = ChronoUnit.DAYS.between(retour.getDateRetourPrevue(), now);
            retour.setJoursDeRetard((int) retard);
        } else {
            retour.setJoursDeRetard(0);
        }

        retour.setStatut(statutRepository.findFirstByLibelle("RENDU")
                .orElseThrow(() -> new RuntimeException("Statut RENDU introuvable")));

        Livre livre = retour.getLivre();
        List<Reservation> fileAttente = reservationRepository
                .findByLivreIdAndStatutLibelleOrderByPositionFileAttenteAsc(livre.getId(), "EN_ATTENTE");

        if (!fileAttente.isEmpty()) {
            fileAttente.get(0).setStatut(statutRepository.findFirstByLibelle("NOTIFIEE")
                    .orElseThrow(() -> new RuntimeException("Statut NOTIFIEE introuvable")));
            for (int i = 1; i < fileAttente.size(); i++)
                fileAttente.get(i).setPositionFileAttente(fileAttente.get(i).getPositionFileAttente() - 1);
            reservationRepository.saveAll(fileAttente);
            livre.setStatut(statutRepository.findFirstByLibelle("EMPRUNTE")
                    .orElseThrow(() -> new RuntimeException("Statut EMPRUNTE introuvable")));
        } else {
            livre.setStatut(statutRepository.findFirstByLibelle("DISPONIBLE")
                    .orElseThrow(() -> new RuntimeException("Statut DISPONIBLE introuvable")));
        }

        livreRepository.save(livre);
        return retourRepository.save(retour);
    }
}
