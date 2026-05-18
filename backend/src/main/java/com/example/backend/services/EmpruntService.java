package com.example.backend.services;

import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.entity.*;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class EmpruntService {

    private static final int MAX_EMPRUNTS_ACTIFS = 3;
    private static final int DUREE_EMPRUNT_JOURS = 14;

    private final EmpruntRepository empruntRepository;
    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StatutRepository statutRepository;
    private final ReservationRepository reservationRepository;

    public EmpruntService(EmpruntRepository empruntRepository,
                          LivreRepository livreRepository,
                          UtilisateurRepository utilisateurRepository,
                          StatutRepository statutRepository,
                          ReservationRepository reservationRepository) {
        this.empruntRepository = empruntRepository;
        this.livreRepository = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.statutRepository = statutRepository;
        this.reservationRepository = reservationRepository;
    }

    public Emprunt emprunterLivre(Integer livreId, String mail) {
        Utilisateur user = utilisateurRepository.findByMail(mail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable"));

        Statut enCours = getStatut("EN_COURS");

        long nbActifs = empruntRepository.countByUtilisateurAndStatut(user, enCours);
        if (nbActifs >= MAX_EMPRUNTS_ACTIFS)
            throw new BusinessException("Vous avez atteint la limite de " + MAX_EMPRUNTS_ACTIFS + " emprunts simultanés.");

        if (livre.getNbDisponibles() <= 0)
            throw new BusinessException("Aucun exemplaire disponible pour ce livre.");

        livre.setNbDisponibles(livre.getNbDisponibles() - 1);
        if (livre.getNbDisponibles() == 0)
            livre.setStatut(getStatut("EMPRUNTE"));
        livreRepository.save(livre);

        Emprunt emprunt = new Emprunt();
        emprunt.setUtilisateur(user);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDateTime.now());
        emprunt.setDateRetourPrevue(LocalDateTime.now().plusDays(DUREE_EMPRUNT_JOURS));
        emprunt.setStatut(enCours);
        return empruntRepository.save(emprunt);
    }

    public Emprunt rendreLivre(Integer empruntId, String mail) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable"));

        if (!emprunt.getUtilisateur().getMail().equals(mail))
            throw new BusinessException("Cet emprunt ne vous appartient pas.");
        if (emprunt.getDateRetour() != null)
            throw new BusinessException("Ce livre a déjà été rendu.");

        LocalDateTime now = LocalDateTime.now();
        emprunt.setDateRetour(now);
        emprunt.setStatut(getStatut("RENDU"));

        if (now.isAfter(emprunt.getDateRetourPrevue())) {
            long jours = ChronoUnit.DAYS.between(emprunt.getDateRetourPrevue(), now);
            emprunt.setJoursRetard((int) jours);
        }

        Livre livre = emprunt.getLivre();
        livre.setNbDisponibles(livre.getNbDisponibles() + 1);
        livre.setStatut(getStatut("DISPONIBLE"));
        livreRepository.save(livre);

        // Notifier la première réservation en attente
        List<Reservation> attente = reservationRepository
                .findByLivreAndStatutOrderByDateReservationAsc(livre, getStatut("EN_ATTENTE"));
        if (!attente.isEmpty()) {
            Reservation prochaine = attente.get(0);
            prochaine.setStatut(getStatut("NOTIFIEE"));
            reservationRepository.save(prochaine);
        }

        return empruntRepository.save(emprunt);
    }

    @Transactional(readOnly = true)
    public List<Emprunt> getMesEmprunts(String mail) {
        Utilisateur user = utilisateurRepository.findByMail(mail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return empruntRepository.findByUtilisateur(user);
    }

    private Statut getStatut(String libelle) {
        return statutRepository.findByLibelle(libelle)
                .orElseThrow(() -> new ResourceNotFoundException("Statut introuvable : " + libelle));
    }
}
