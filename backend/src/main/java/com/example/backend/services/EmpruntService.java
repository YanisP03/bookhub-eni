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

/**
 * ═══════════════════════════════════════════════════════════════
 *  SERVICE EMPRUNT — Cycle de vie complet d'un emprunt
 * ═══════════════════════════════════════════════════════════════
 *
 *  FLUX PRINCIPAL (statuts de l'entité Emprunt) :
 *
 *  [Lecteur]        POST /api/emprunts/{livreId}
 *       │               ↓
 *       │           DEMANDE          ← exemplaire réservé immédiatement
 *       │               │
 *  [Bibliothécaire] PUT /api/emprunts/{id}/valider   → EN_COURS  (dateRetourPrevue = now + 14j)
 *                   PUT /api/emprunts/{id}/refuser   → ANNULE    (exemplaire re-libéré)
 *       │               │
 *  [Lecteur]        PUT /api/emprunts/{id}/retour
 *       │               ↓
 *       │           RETOUR_DEMANDE   ← livre physiquement rapporté, biblio doit confirmer
 *       │               │
 *  [Bibliothécaire] PUT /api/emprunts/{id}/valider-retour
 *                       ↓
 *                   RENDU            ← exemplaire libéré, joursRetard calculé, réservations notifiées
 *
 *  STATUTS LIVRE associés :
 *    - DISPONIBLE  : nbDisponibles > 0
 *    - EMPRUNTE    : nbDisponibles = 0
 *
 *  RÈGLES MÉTIER :
 *    - Maximum MAX_EMPRUNTS_ACTIFS (3) emprunts simultanés par lecteur
 *    - Durée standard DUREE_EMPRUNT_JOURS (14) jours
 *    - Un lecteur ne peut pas emprunter deux fois le même livre activement
 *    - Si une réservation NOTIFIEE existe pour ce lecteur+livre, elle passe en CONVERTIE
 *    - À la validation du retour : la 1re réservation EN_ATTENTE du livre passe en NOTIFIEE
 *
 * ═══════════════════════════════════════════════════════════════
 */
@Service
@Transactional
public class EmpruntService {

    /** Nombre maximum d'emprunts actifs (DEMANDE + EN_COURS) par lecteur. */
    private static final int MAX_EMPRUNTS_ACTIFS = 3;

    /** Durée standard d'un emprunt en jours (fixée à la validation bibliothécaire). */
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

    // ─────────────────────────────────────────────────────────────
    // ÉTAPE 1 — Lecteur : demande d'emprunt → statut DEMANDE
    // ─────────────────────────────────────────────────────────────

    /**
     * Crée une demande d'emprunt pour un livre.
     *
     * Contrôles :
     *   1. Limite de 3 emprunts actifs (DEMANDE + EN_COURS)
     *   2. Pas de doublon actif sur le même livre
     *   3. Au moins 1 exemplaire disponible
     *
     * Effets de bord :
     *   - nbDisponibles du livre décrémenté immédiatement
     *   - Si nbDisponibles tombe à 0 → statut livre passe à EMPRUNTE
     *   - Si le lecteur avait une réservation NOTIFIEE pour ce livre → CONVERTIE
     *
     * Endpoint : POST /api/emprunts/{livreId}
     */
    public Emprunt demanderEmprunt(Integer livreId, String mail) {
        Utilisateur user = utilisateurRepository.findByMail(mail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable"));

        // Règle : compte bloqué
        verifierBlocage(user);

        // Règle : max MAX_EMPRUNTS_ACTIFS emprunts simultanés
        long nbActifs = empruntRepository.countActifsByUtilisateur(user);
        if (nbActifs >= MAX_EMPRUNTS_ACTIFS)
            throw new BusinessException("Vous avez atteint la limite de " + MAX_EMPRUNTS_ACTIFS + " emprunts simultanés.");

        // Règle : pas de doublon actif sur le même livre (évite aussi l'erreur de contrainte DB)
        if (empruntRepository.hasEmpruntActifPourLivre(user, livre))
            throw new BusinessException("Vous avez déjà un emprunt en cours pour ce livre.");

        // Règle : au moins 1 exemplaire disponible
        if (livre.getNbDisponibles() <= 0)
            throw new BusinessException("Aucun exemplaire disponible pour ce livre.");

        // Réserver l'exemplaire immédiatement (avant validation bibliothécaire)
        livre.setNbDisponibles(livre.getNbDisponibles() - 1);
        if (livre.getNbDisponibles() == 0)
            livre.setStatut(getStatut("EMPRUNTE"));
        livreRepository.save(livre);

        // Si le lecteur avait une réservation NOTIFIEE sur ce livre, on la clôture
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

    // ─────────────────────────────────────────────────────────────
    // ÉTAPE 2a — Bibliothécaire : validation → statut EN_COURS
    // ─────────────────────────────────────────────────────────────

    /**
     * Valide une demande d'emprunt.
     * Fixe dateRetourPrevue = now + DUREE_EMPRUNT_JOURS.
     *
     * Endpoint : PUT /api/emprunts/{id}/valider
     */
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

    // ─────────────────────────────────────────────────────────────
    // ÉTAPE 2b — Bibliothécaire : refus → statut ANNULE
    // ─────────────────────────────────────────────────────────────

    /**
     * Refuse une demande d'emprunt.
     * Re-libère l'exemplaire réservé à l'étape 1.
     *
     * Endpoint : PUT /api/emprunts/{id}/refuser
     */
    public Emprunt refuserEmprunt(Integer empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable"));

        if (!"DEMANDE".equals(emprunt.getStatut().getLibelle()))
            throw new BusinessException("Cet emprunt n'est pas en attente de validation.");

        emprunt.setStatut(getStatut("ANNULE"));
        empruntRepository.save(emprunt);

        // Re-libérer l'exemplaire (avait été décrémenté à l'étape 1)
        Livre livre = emprunt.getLivre();
        livre.setNbDisponibles(livre.getNbDisponibles() + 1);
        livre.setStatut(getStatut("DISPONIBLE"));
        livreRepository.save(livre);

        return emprunt;
    }

    // ─────────────────────────────────────────────────────────────
    // ÉTAPE 3 — Lecteur : signale le retour → statut RETOUR_DEMANDE
    // ─────────────────────────────────────────────────────────────

    /**
     * Le lecteur rapporte physiquement le livre.
     * Le bibliothécaire doit ensuite confirmer (étape 4).
     * L'exemplaire n'est PAS encore libéré à cette étape.
     *
     * Endpoint : PUT /api/emprunts/{id}/retour
     */
    public Emprunt rendreLivre(Integer empruntId, String mail) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable"));

        if (!emprunt.getUtilisateur().getMail().equals(mail))
            throw new BusinessException("Cet emprunt ne vous appartient pas.");

        String libelle = emprunt.getStatut().getLibelle();
        if ("RENDU".equals(libelle) || "RETOUR_DEMANDE".equals(libelle))
            throw new BusinessException("Ce livre a déjà été rendu ou est en attente de validation.");
        if (!"EN_COURS".equals(libelle))
            throw new BusinessException("Cet emprunt n'est pas en cours.");

        emprunt.setStatut(getStatut("RETOUR_DEMANDE"));
        return empruntRepository.save(emprunt);
    }

    // ─────────────────────────────────────────────────────────────
    // ÉTAPE 4 — Bibliothécaire : confirme le retour → statut RENDU
    // ─────────────────────────────────────────────────────────────

    /**
     * Valide le retour physique du livre.
     *
     * Effets de bord :
     *   - dateRetour fixée à maintenant
     *   - joursRetard calculé si dateRetourPrevue est dépassée
     *   - nbDisponibles du livre incrémenté
     *   - Si réservations EN_ATTENTE existent : la première passe en NOTIFIEE
     *
     * Endpoint : PUT /api/emprunts/{id}/valider-retour
     */
    public Emprunt validerRetour(Integer empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable"));

        if (!"RETOUR_DEMANDE".equals(emprunt.getStatut().getLibelle()))
            throw new BusinessException("Ce retour n'est pas en attente de validation.");

        LocalDateTime now = LocalDateTime.now();
        emprunt.setDateRetour(now);
        emprunt.setStatut(getStatut("RENDU"));

        // Calcul du retard uniquement si la date prévue est dépassée
        if (emprunt.getDateRetourPrevue() != null && now.isAfter(emprunt.getDateRetourPrevue())) {
            long jours = ChronoUnit.DAYS.between(emprunt.getDateRetourPrevue(), now);
            emprunt.setJoursRetard((int) jours);

            // Comptabiliser le retard sur l'utilisateur
            Utilisateur user = emprunt.getUtilisateur();
            user.setNbRetards(user.getNbRetards() + 1);
            if (user.getNbRetards() >= 3) {
                user.setBloque(true);
                user.setDateBlocageAuto(now.plusDays(5));
                user.setNbRetards(0);
            }
            utilisateurRepository.save(user);
        }

        // Libérer l'exemplaire dans le stock
        Livre livre = emprunt.getLivre();
        livre.setNbDisponibles(livre.getNbDisponibles() + 1);
        livre.setStatut(getStatut("DISPONIBLE"));
        livreRepository.save(livre);

        // Notifier la première réservation en attente pour ce livre
        List<Reservation> attente = reservationRepository
                .findByLivreAndStatutOrderByDateReservationAsc(livre, getStatut("EN_ATTENTE"));
        if (!attente.isEmpty()) {
            attente.get(0).setStatut(getStatut("NOTIFIEE"));
            reservationRepository.save(attente.get(0));
        }

        return empruntRepository.save(emprunt);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — listes pour l'interface bibliothécaire / lecteur
    // ─────────────────────────────────────────────────────────────

    /**
     * Toutes les demandes DEMANDE en attente de validation bibliothécaire,
     * triées par date d'emprunt croissante (plus ancienne = prioritaire).
     * Endpoint : GET /api/emprunts/demandes
     */
    @Transactional(readOnly = true)
    public List<Emprunt> getDemandesEnAttente() {
        return empruntRepository.findByStatut(getStatut("DEMANDE"))
                .stream()
                .sorted(java.util.Comparator.comparing(Emprunt::getDateEmprunt))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Tous les retours RETOUR_DEMANDE en attente de confirmation bibliothécaire.
     * Endpoint : GET /api/emprunts/retours
     */
    @Transactional(readOnly = true)
    public List<Emprunt> getRetoursEnAttente() {
        return empruntRepository.findByStatut(getStatut("RETOUR_DEMANDE"))
                .stream()
                .sorted(java.util.Comparator.comparing(Emprunt::getDateEmprunt))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Convertit une réservation NOTIFIEE en emprunt EN_COURS directement.
     * Utilisé par le bibliothécaire quand le lecteur vient récupérer le livre.
     *
     * Endpoint : PUT /api/emprunts/reservations/{id}/valider
     */
    public Emprunt validerReservationNotifiee(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));

        if (!"NOTIFIEE".equals(reservation.getStatut().getLibelle()))
            throw new BusinessException("Cette réservation n'est pas en statut NOTIFIEE.");

        Utilisateur user = reservation.getUtilisateur();
        Livre livre = reservation.getLivre();

        if (livre.getNbDisponibles() <= 0)
            throw new BusinessException("Aucun exemplaire disponible pour ce livre.");

        livre.setNbDisponibles(livre.getNbDisponibles() - 1);
        if (livre.getNbDisponibles() == 0)
            livre.setStatut(getStatut("EMPRUNTE"));
        livreRepository.save(livre);

        reservation.setStatut(getStatut("CONVERTIE"));
        reservationRepository.save(reservation);

        Emprunt emprunt = new Emprunt();
        emprunt.setUtilisateur(user);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDateTime.now());
        emprunt.setDateRetourPrevue(LocalDateTime.now().plusDays(DUREE_EMPRUNT_JOURS));
        emprunt.setStatut(getStatut("EN_COURS"));
        return empruntRepository.save(emprunt);
    }

    /**
     * Position (1-based) d'un emprunt dans la file d'attente DEMANDE de son livre.
     * Retourne -1 si l'emprunt n'est pas dans la file.
     * Endpoint : GET /api/emprunts/{id}/position
     */
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

    /**
     * Historique complet des emprunts d'un lecteur (tous statuts confondus).
     * Endpoint : GET /api/emprunts/mes-emprunts
     */
    @Transactional(readOnly = true)
    public List<Emprunt> getMesEmprunts(String mail) {
        Utilisateur user = utilisateurRepository.findByMail(mail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return empruntRepository.findByUtilisateur(user);
    }

    // ─────────────────────────────────────────────────────────────
    // UTILITAIRE INTERNE
    // ─────────────────────────────────────────────────────────────

    /**
     * Récupère un statut par son libellé exact.
     * Lance ResourceNotFoundException si le statut n'existe pas en base
     * (protection contre les statuts manquants après migration).
     */
    private void verifierBlocage(Utilisateur user) {
        if (!user.isBloque()) return;
        if (user.getDateBlocageAuto() != null && LocalDateTime.now().isAfter(user.getDateBlocageAuto())) {
            user.setBloque(false);
            user.setDateBlocageAuto(null);
            user.setNbRetards(0);
            utilisateurRepository.save(user);
            return;
        }
        String date = user.getDateBlocageAuto() != null
                ? user.getDateBlocageAuto().toLocalDate().toString() : "indéterminée";
        throw new BusinessException(
                "Votre compte est suspendu jusqu'au " + date + " suite à trop de retards.");
    }

    private Statut getStatut(String libelle) {
        return statutRepository.findFirstByLibelle(libelle)
                .orElseThrow(() -> new ResourceNotFoundException("Statut introuvable : " + libelle));
    }
}
