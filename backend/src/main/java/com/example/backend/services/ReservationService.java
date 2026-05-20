package com.example.backend.services;

import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Reservation;
import com.example.backend.model.entity.Statut;
import com.example.backend.model.entity.Utilisateur;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StatutRepository statutRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              LivreRepository livreRepository,
                              UtilisateurRepository utilisateurRepository,
                              StatutRepository statutRepository) {
        this.reservationRepository = reservationRepository;
        this.livreRepository = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.statutRepository = statutRepository;
    }

    public Reservation reserverLivre(Integer livreId, String mail) {
        Utilisateur user = utilisateurRepository.findByMail(mail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable"));

        // Vérifier si l'utilisateur est bloqué
        if (user.isBloque()) {
            if (user.getDateBlocageAuto() != null && java.time.LocalDateTime.now().isAfter(user.getDateBlocageAuto())) {
                user.setBloque(false);
                user.setDateBlocageAuto(null);
                user.setNbRetards(0);
                utilisateurRepository.save(user);
            } else {
                String date = user.getDateBlocageAuto() != null
                        ? user.getDateBlocageAuto().toLocalDate().toString() : "indéterminée";
                throw new BusinessException(
                        "Votre compte est suspendu jusqu'au " + date + " suite à trop de retards.");
            }
        }

        Statut enAttente = getStatut("EN_ATTENTE");

        if (livre.getNbDisponibles() > 0)
            throw new BusinessException("Ce livre est disponible, empruntez-le directement.");

        reservationRepository.findByUtilisateurAndLivreAndStatut(user, livre, enAttente)
                .ifPresent(r -> { throw new BusinessException("Vous avez déjà une réservation active pour ce livre."); });

        List<Reservation> file = reservationRepository
                .findByLivreAndStatutOrderByDateReservationAsc(livre, enAttente);

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(user);
        reservation.setLivre(livre);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut(enAttente);
        reservation.setPositionFileAttente(file.size() + 1);
        return reservationRepository.save(reservation);
    }

    public void annulerReservation(Integer reservationId, String mail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));

        if (!reservation.getUtilisateur().getMail().equals(mail))
            throw new BusinessException("Cette réservation ne vous appartient pas.");

        reservation.setStatut(getStatut("ANNULEE"));
        reservationRepository.save(reservation);

        // Recalculer les positions des réservations suivantes
        List<Reservation> restantes = reservationRepository
                .findByLivreAndStatutOrderByDateReservationAsc(reservation.getLivre(), getStatut("EN_ATTENTE"));
        for (int i = 0; i < restantes.size(); i++) {
            restantes.get(i).setPositionFileAttente(i + 1);
            reservationRepository.save(restantes.get(i));
        }
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsNotifiees() {
        return reservationRepository.findByStatutOrderByLivreTitreAscPositionFileAttenteAsc(getStatut("NOTIFIEE"));
    }

    @Transactional(readOnly = true)
    public List<Reservation> getFileAttente() {
        Statut enAttente = getStatut("EN_ATTENTE");
        return reservationRepository.findByStatutOrderByLivreTitreAscPositionFileAttenteAsc(enAttente);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getMesReservations(String mail) {
        Utilisateur user = utilisateurRepository.findByMail(mail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return reservationRepository.findByUtilisateur(user);
    }

    private Statut getStatut(String libelle) {
        return statutRepository.findFirstByLibelle(libelle)
                .orElseThrow(() -> new ResourceNotFoundException("Statut introuvable : " + libelle));
    }
}
