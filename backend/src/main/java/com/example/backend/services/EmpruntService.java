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

    /** Lecteur : fait une demande d'emprunt (statut DEMANDE) */
    public Emprunt demanderEmprunt(Integer livreId, String mail) {
        Utilisateur user = utilisateurRepository.findByMail(mail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable"));

        long nbActifs = empruntRepository.countActifsByUtilisateur(user);
        if (nbActifs >= MAX_EMPRUNTS_ACTIFS)
            throw new BusinessException("Vous avez atteint la limite de " + MAX_EMPRUNTS_ACTIFS + " emprunts simultanés.");

        if (empruntRepository.hasEmpruntActifPourLivre(user, livre))
            throw new BusinessException("Vous avez déjà un emprunt en cours pour ce livre.");

        if (livre.getNbDisponibles() <= 0)
            throw new BusinessException("Aucun exemplaire disponible pour ce livre.");

        // Réserver l'exemplaire immédiatement
        livre.setNbDisponibles(livre.getNbDisponibles() - 1);
        if (livre.getNbDisponibles() == 0)
            livre.setStatut(getStatut("EMPRUNTE"));
        livreRepository.save(livre);

        // Si l'utilisateur avait une réservation NOTIFIEE pour ce livre, on la convertit
        reservationRepository.findByUtilisateurAndLivreAndStatut(user, livre, getStatut("NOTIFIEE"))
                .ifPresent(r -> {
                    r.setStatut(getStatut("CONVERTIE"));
                    reservationRepository.save(r);
                });

        Emprunt emprunt = new Emprunt();
        emprunt.setUtilisateur(user);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDateTime.now());
        emprunt.setStatut(getStatut("DEMANDE"));
        return empruntRepository.save(emprunt);
    }

    /** Bibliothécaire : valide la demande → EN_COURS */
    public Emprunt validerEmprunt(Integer empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable"));

        if (!"DEMANDE".equals(emprunt.getStatut().getLibelle()))
            throw new BusinessException("Cet emprunt n'est pas en attente de validation.");

        emprunt.setDateEmprunt(LocalDateTime.now());
        emprunt.setDateRetourPrevue(LocalDateTime.now().plusDays(DUREE_EMPRUNT_JOURS));
        emprunt.setStatut(getStatut("EN_COURS"));
        return empruntRepository.save(emprunt);
    }

    /** Bibliothécaire : refuse la demande → ANNULE + rend l'exemplaire */
    public Emprunt refuserEmprunt(Integer empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable"));

        if (!"DEMANDE".equals(emprunt.getStatut().getLibelle()))
            throw new BusinessException("Cet emprunt n'est pas en attente de validation.");

        emprunt.setStatut(getStatut("ANNULE"));
        empruntRepository.save(emprunt);

        Livre livre = emprunt.getLivre();
        livre.setNbDisponibles(livre.getNbDisponibles() + 1);
        livre.setStatut(getStatut("DISPONIBLE"));
        livreRepository.save(livre);

        return emprunt;
    }

    /** Bibliothécaire : liste toutes les demandes en attente, triées par date */
    @Transactional(readOnly = true)
    public List<Emprunt> getDemandesEnAttente() {
        return empruntRepository.findByStatut(getStatut("DEMANDE"))
                .stream()
                .sorted(java.util.Comparator.comparing(Emprunt::getDateEmprunt))
                .collect(java.util.stream.Collectors.toList());
    }

    /** Retourne la position (1-based) de cet emprunt dans la file d'attente du livre */
    @Transactional(readOnly = true)
    public int getPositionFileAttente(Integer empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable"));
        List<Emprunt> file = empruntRepository
                .findDemandesByLivreOrderByDate(emprunt.getLivre().getId());
        for (int i = 0; i < file.size(); i++) {
            if (file.get(i).getId().equals(empruntId)) return i + 1;
        }
        return -1;
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

        if (emprunt.getDateRetourPrevue() != null && now.isAfter(emprunt.getDateRetourPrevue())) {
            long jours = ChronoUnit.DAYS.between(emprunt.getDateRetourPrevue(), now);
            emprunt.setJoursRetard((int) jours);
        }

        Livre livre = emprunt.getLivre();
        livre.setNbDisponibles(livre.getNbDisponibles() + 1);
        livre.setStatut(getStatut("DISPONIBLE"));
        livreRepository.save(livre);

        List<Reservation> attente = reservationRepository
                .findByLivreAndStatutOrderByDateReservationAsc(livre, getStatut("EN_ATTENTE"));
        if (!attente.isEmpty()) {
            attente.get(0).setStatut(getStatut("NOTIFIEE"));
            reservationRepository.save(attente.get(0));
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
        return statutRepository.findFirstByLibelle(libelle)
                .orElseThrow(() -> new ResourceNotFoundException("Statut introuvable : " + libelle));
    }
}
