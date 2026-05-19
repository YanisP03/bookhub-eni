package com.example.backend.services; // Vérifie que le package correspond bien à ton dossier

import com.example.backend.model.entity.Loan;
import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Reservation;
import com.example.backend.model.entity.Statut;
import com.example.backend.repository.LoanRepository;
import com.example.backend.repository.ReservationRepository;
import com.example.backend.repository.LivreRepository;
import com.example.backend.repository.StatutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final LivreRepository livreRepository;
    private final StatutRepository statutRepository;

    // Constructeur avec les 4 injections indispensables
    public LoanService(LoanRepository loanRepository,
                       ReservationRepository reservationRepository,
                       LivreRepository livreRepository,
                       StatutRepository statutRepository) {
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
        this.livreRepository = livreRepository;
        this.statutRepository = statutRepository;
    }

    @Transactional
    public Loan enregistrerRetour(Integer loanId) {
        // 1. Récupérer l'emprunt
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Emprunt introuvable"));

        if (loan.getDateRetourReelle() != null) {
            throw new IllegalStateException("Ce livre a déjà été rendu.");
        }

        // 2. Mettre à jour les dates du retour
        LocalDateTime now = LocalDateTime.now();
        loan.setDateRetourReelle(now);

        // 3. Calculer le retard si la date réelle est après la date prévue
        if (now.isAfter(loan.getDateRetourPrevue())) {
            long retard = ChronoUnit.DAYS.between(loan.getDateRetourPrevue(), now);
            loan.setJoursDeRetard((int) retard);
        } else {
            loan.setJoursDeRetard(0);
        }

        // Passer le statut de l'emprunt lui-même à "RENDU"
        Statut statutRendu = statutRepository.findFirstByLibelle("RENDU")
                        .orElseThrow(()-> new RuntimeException("Statut RENDU introuvable"));
        loan.setStatut(statutRendu);

        // 4. Libérer le livre et checker la file d'attente des réservations
        Integer livreId = loan.getLivre().getId();
        Livre livre = loan.getLivre();

        // Récupérer la file d'attente triée (1, 2, 3...)
        List<Reservation> fileAttente = reservationRepository.findByLivreIdAndStatutLibelleOrderByPositionFileAttenteAsc(livreId, "EN_ATTENTE");

        if (!fileAttente.isEmpty()) {
            // Quelqu'un attend le livre !
            for (Reservation res : fileAttente) {
                if (res.getPositionFileAttente() == 1) {
                    // Le premier de la file sort de l'attente et passe en priorité retrait
                    res.setPositionFileAttente(0);
                    Statut statutDispoRetrait = statutRepository.findFirstByLibelle("NOTIFIEE")
                            .orElseThrow(()-> new RuntimeException("Statut NOTIFIEE introuvable"));
                    res.setStatut(statutDispoRetrait);
                } else {
                    // Les autres avancent d'un rang dans la file
                    res.setPositionFileAttente(res.getPositionFileAttente() - 1);
                }
            }
            // Sauvegarder les changements de positions
            reservationRepository.saveAll(fileAttente);

            // Le livre change de statut mais reste bloqué pour la réservation prioritaire
            Statut statutReserve = statutRepository.findFirstByLibelle("EMPRUNTE")
                    .orElseThrow(()-> new RuntimeException("Statut EMPRUNTE introuvable"));
            livre.setStatut(statutReserve);
        } else {
            // Personne n'attend, le livre redevient disponible pour tout le monde
            Statut statutDisponible = statutRepository.findFirstByLibelle("DISPONIBLE")
                    .orElseThrow(()-> new RuntimeException("Statut DISPONIBLE introuvable"));
            livre.setStatut(statutDisponible);
        }

        // Sauvegarder l'état du livre modifié
        livreRepository.save(livre);

        // Sauvegarder et retourner l'emprunt finalisé
        return loanRepository.save(loan);
    }
}