package com.example.backend.services;

import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.entity.*;
import com.example.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires d'EmpruntService — flux complet du cycle de vie d'un emprunt.
 * Aucun contexte Spring, aucune base de données : tout est mocké via Mockito.
 */
@ExtendWith(MockitoExtension.class)
class EmpruntServiceTest {

    @Mock EmpruntRepository     empruntRepository;
    @Mock LivreRepository       livreRepository;
    @Mock UtilisateurRepository utilisateurRepository;
    @Mock StatutRepository      statutRepository;
    @Mock ReservationRepository reservationRepository;

    @InjectMocks EmpruntService empruntService;

    // ── Fixtures ─────────────────────────────────────────────────

    private Utilisateur user;
    private Livre       livre;

    @BeforeEach
    void setUp() {
        user = new Utilisateur();
        user.setId(1);
        user.setNom("Dupont");
        user.setPrenom("Jean");
        user.setMail("jean@test.fr");

        livre = new Livre();
        livre.setId(42);
        livre.setTitre("Dune");
        livre.setNbExemplaires(3);
        livre.setNbDisponibles(2);
        livre.setStatut(statut("DISPONIBLE"));

        // Statuts disponibles par défaut pour tous les tests
        lenient().when(statutRepository.findFirstByLibelle(anyString()))
                 .thenAnswer(inv -> Optional.of(statut(inv.getArgument(0))));
    }

    /** Crée un Statut avec un libellé donné. */
    private Statut statut(String libelle) {
        Statut s = new Statut();
        s.setLibelle(libelle);
        return s;
    }

    /** Crée un Emprunt minimal avec un statut donné. */
    private Emprunt emprunt(String statutLibelle) {
        Emprunt e = new Emprunt();
        e.setId(99);
        e.setUtilisateur(user);
        e.setLivre(livre);
        e.setDateEmprunt(LocalDateTime.now());
        e.setStatut(statut(statutLibelle));
        return e;
    }

    // ═══════════════════════════════════════════════════════════
    // ÉTAPE 1 — demanderEmprunt
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("demanderEmprunt()")
    class DemanderEmprunt {

        @BeforeEach
        void mockBase() {
            when(utilisateurRepository.findByMail("jean@test.fr")).thenReturn(Optional.of(user));
            when(livreRepository.findById(42)).thenReturn(Optional.of(livre));
            when(empruntRepository.countActifsByUtilisateur(user)).thenReturn(0L);
            when(empruntRepository.hasEmpruntActifPourLivre(user, livre)).thenReturn(false);
            when(reservationRepository.findByUtilisateurAndLivreAndStatut(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(empruntRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("Cas nominal → emprunt créé avec statut DEMANDE")
        void happyPath_creesEmpruntDemande() {
            Emprunt result = empruntService.demanderEmprunt(42, "jean@test.fr");

            assertThat(result.getStatut().getLibelle()).isEqualTo("DEMANDE");
            assertThat(result.getUtilisateur()).isEqualTo(user);
            assertThat(result.getLivre()).isEqualTo(livre);
            verify(empruntRepository).save(any(Emprunt.class));
        }

        @Test
        @DisplayName("Cas nominal → nbDisponibles décrémenté immédiatement")
        void happyPath_decrementeNbDisponibles() {
            empruntService.demanderEmprunt(42, "jean@test.fr");

            assertThat(livre.getNbDisponibles()).isEqualTo(1);
            verify(livreRepository).save(livre);
        }

        @Test
        @DisplayName("Dernier exemplaire → livre passe à EMPRUNTE")
        void dernierExemplaire_passeLivreEmprunte() {
            livre.setNbDisponibles(1);

            empruntService.demanderEmprunt(42, "jean@test.fr");

            assertThat(livre.getNbDisponibles()).isEqualTo(0);
            assertThat(livre.getStatut().getLibelle()).isEqualTo("EMPRUNTE");
        }

        @Test
        @DisplayName("Réservation NOTIFIEE existante → passe en CONVERTIE")
        void reservationNotifiee_convertieEnConvertie() {
            Reservation resa = new Reservation();
            resa.setStatut(statut("NOTIFIEE"));
            when(reservationRepository.findByUtilisateurAndLivreAndStatut(any(), any(), any()))
                    .thenReturn(Optional.of(resa));

            empruntService.demanderEmprunt(42, "jean@test.fr");

            assertThat(resa.getStatut().getLibelle()).isEqualTo("CONVERTIE");
            verify(reservationRepository).save(resa);
        }

        @Test
        @DisplayName("Utilisateur inconnu → ResourceNotFoundException")
        void utilisateurInconnu_throwsResourceNotFoundException() {
            when(utilisateurRepository.findByMail(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empruntService.demanderEmprunt(42, "inconnu@test.fr"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Livre inconnu → ResourceNotFoundException")
        void livreInconnu_throwsResourceNotFoundException() {
            when(livreRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empruntService.demanderEmprunt(999, "jean@test.fr"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("3 emprunts actifs → BusinessException limite atteinte")
        void limiteAtteinte_throwsBusinessException() {
            when(empruntRepository.countActifsByUtilisateur(user)).thenReturn(3L);

            assertThatThrownBy(() -> empruntService.demanderEmprunt(42, "jean@test.fr"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("limite");
        }

        @Test
        @DisplayName("Doublon actif sur même livre → BusinessException")
        void doublonActif_throwsBusinessException() {
            when(empruntRepository.hasEmpruntActifPourLivre(user, livre)).thenReturn(true);

            assertThatThrownBy(() -> empruntService.demanderEmprunt(42, "jean@test.fr"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("en cours");
        }

        @Test
        @DisplayName("Aucun exemplaire disponible → BusinessException")
        void aucunExemplaire_throwsBusinessException() {
            livre.setNbDisponibles(0);

            assertThatThrownBy(() -> empruntService.demanderEmprunt(42, "jean@test.fr"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("disponible");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ÉTAPE 2a — validerEmprunt
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validerEmprunt()")
    class ValiderEmprunt {

        @Test
        @DisplayName("Cas nominal → statut EN_COURS + dateRetourPrevue dans 14 jours")
        void happyPath_setEnCours() {
            Emprunt e = emprunt("DEMANDE");
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));
            when(empruntRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Emprunt result = empruntService.validerEmprunt(99);

            assertThat(result.getStatut().getLibelle()).isEqualTo("EN_COURS");
            assertThat(result.getDateRetourPrevue()).isNotNull();
            assertThat(result.getDateRetourPrevue())
                    .isAfter(LocalDateTime.now().plusDays(13))
                    .isBefore(LocalDateTime.now().plusDays(15));
        }

        @Test
        @DisplayName("Emprunt pas en statut DEMANDE → BusinessException")
        void statutInvalide_throwsBusinessException() {
            Emprunt e = emprunt("EN_COURS");
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));

            assertThatThrownBy(() -> empruntService.validerEmprunt(99))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Emprunt introuvable → ResourceNotFoundException")
        void empruntInconnu_throwsResourceNotFoundException() {
            when(empruntRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empruntService.validerEmprunt(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ÉTAPE 2b — refuserEmprunt
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("refuserEmprunt()")
    class RefuserEmprunt {

        @Test
        @DisplayName("Cas nominal → statut ANNULE + exemplaire re-libéré")
        void happyPath_setAnnule_libereLivre() {
            int dispo = livre.getNbDisponibles();
            Emprunt e = emprunt("DEMANDE");
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));
            when(empruntRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            empruntService.refuserEmprunt(99);

            assertThat(e.getStatut().getLibelle()).isEqualTo("ANNULE");
            assertThat(livre.getNbDisponibles()).isEqualTo(dispo + 1);
            assertThat(livre.getStatut().getLibelle()).isEqualTo("DISPONIBLE");
            verify(livreRepository).save(livre);
        }

        @Test
        @DisplayName("Emprunt pas en statut DEMANDE → BusinessException")
        void statutInvalide_throwsBusinessException() {
            Emprunt e = emprunt("EN_COURS");
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));

            assertThatThrownBy(() -> empruntService.refuserEmprunt(99))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ÉTAPE 3 — rendreLivre (lecteur signale le retour)
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("rendreLivre()")
    class RendreLivre {

        @Test
        @DisplayName("Cas nominal EN_COURS → statut RETOUR_DEMANDE")
        void happyPath_setRetourDemande() {
            Emprunt e = emprunt("EN_COURS");
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));
            when(empruntRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Emprunt result = empruntService.rendreLivre(99, "jean@test.fr");

            assertThat(result.getStatut().getLibelle()).isEqualTo("RETOUR_DEMANDE");
            verify(empruntRepository).save(e);
        }

        @Test
        @DisplayName("Mauvais utilisateur → BusinessException")
        void mauvaisUtilisateur_throwsBusinessException() {
            Emprunt e = emprunt("EN_COURS");
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));

            assertThatThrownBy(() -> empruntService.rendreLivre(99, "autre@test.fr"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("appartient");
        }

        @Test
        @DisplayName("Emprunt déjà en RENDU → BusinessException")
        void dejaRendu_throwsBusinessException() {
            Emprunt e = emprunt("RENDU");
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));

            assertThatThrownBy(() -> empruntService.rendreLivre(99, "jean@test.fr"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Emprunt déjà en RETOUR_DEMANDE → BusinessException")
        void dejaEnAttente_throwsBusinessException() {
            Emprunt e = emprunt("RETOUR_DEMANDE");
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));

            assertThatThrownBy(() -> empruntService.rendreLivre(99, "jean@test.fr"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Emprunt en DEMANDE (pas encore validé) → BusinessException")
        void pasEnCours_throwsBusinessException() {
            Emprunt e = emprunt("DEMANDE");
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));

            assertThatThrownBy(() -> empruntService.rendreLivre(99, "jean@test.fr"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("en cours");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ÉTAPE 4 — validerRetour (bibliothécaire confirme)
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validerRetour()")
    class ValiderRetour {

        @Test
        @DisplayName("Cas nominal → statut RENDU + exemplaire libéré")
        void happyPath_setRendu_libereLivre() {
            int dispo = livre.getNbDisponibles();
            Emprunt e = emprunt("RETOUR_DEMANDE");
            e.setDateRetourPrevue(LocalDateTime.now().plusDays(5)); // pas en retard
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));
            when(empruntRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(reservationRepository.findByLivreAndStatutOrderByDateReservationAsc(any(), any()))
                    .thenReturn(Collections.emptyList());

            Emprunt result = empruntService.validerRetour(99);

            assertThat(result.getStatut().getLibelle()).isEqualTo("RENDU");
            assertThat(result.getDateRetour()).isNotNull();
            assertThat(result.getJoursRetard()).isZero();
            assertThat(livre.getNbDisponibles()).isEqualTo(dispo + 1);
            assertThat(livre.getStatut().getLibelle()).isEqualTo("DISPONIBLE");
            verify(livreRepository).save(livre);
        }

        @Test
        @DisplayName("Retour en retard → joursRetard calculé")
        void avecRetard_calculJoursRetard() {
            Emprunt e = emprunt("RETOUR_DEMANDE");
            e.setDateRetourPrevue(LocalDateTime.now().minusDays(5)); // 5 jours de retard
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));
            when(empruntRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(reservationRepository.findByLivreAndStatutOrderByDateReservationAsc(any(), any()))
                    .thenReturn(Collections.emptyList());

            Emprunt result = empruntService.validerRetour(99);

            assertThat(result.getJoursRetard()).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Réservation EN_ATTENTE → première passe en NOTIFIEE")
        void avecReservationAttente_notifiePremiere() {
            Reservation resa = new Reservation();
            resa.setStatut(statut("EN_ATTENTE"));

            Emprunt e = emprunt("RETOUR_DEMANDE");
            e.setDateRetourPrevue(LocalDateTime.now().plusDays(3));
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));
            when(empruntRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(reservationRepository.findByLivreAndStatutOrderByDateReservationAsc(any(), any()))
                    .thenReturn(List.of(resa));

            empruntService.validerRetour(99);

            assertThat(resa.getStatut().getLibelle()).isEqualTo("NOTIFIEE");
            verify(reservationRepository).save(resa);
        }

        @Test
        @DisplayName("Emprunt pas en RETOUR_DEMANDE → BusinessException")
        void statutInvalide_throwsBusinessException() {
            Emprunt e = emprunt("EN_COURS");
            when(empruntRepository.findById(99)).thenReturn(Optional.of(e));

            assertThatThrownBy(() -> empruntService.validerRetour(99))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Listes — getDemandesEnAttente / getRetoursEnAttente
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Listes bibliothécaire")
    class Listes {

        @Test
        @DisplayName("getDemandesEnAttente → retourne les DEMANDE triés par date")
        void getDemandesEnAttente_retourneListe() {
            Emprunt e1 = emprunt("DEMANDE");
            e1.setDateEmprunt(LocalDateTime.now().minusDays(2));
            Emprunt e2 = emprunt("DEMANDE");
            e2.setDateEmprunt(LocalDateTime.now().minusDays(1));
            when(empruntRepository.findByStatut(any())).thenReturn(List.of(e2, e1));

            List<Emprunt> result = empruntService.getDemandesEnAttente();

            // Plus ancienne en premier
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDateEmprunt()).isBefore(result.get(1).getDateEmprunt());
        }

        @Test
        @DisplayName("getRetoursEnAttente → retourne les RETOUR_DEMANDE")
        void getRetoursEnAttente_retourneListe() {
            Emprunt e = emprunt("RETOUR_DEMANDE");
            when(empruntRepository.findByStatut(any())).thenReturn(List.of(e));

            List<Emprunt> result = empruntService.getRetoursEnAttente();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("getMesEmprunts → retourne tous les emprunts de l'utilisateur")
        void getMesEmprunts_retourneHistorique() {
            when(utilisateurRepository.findByMail("jean@test.fr")).thenReturn(Optional.of(user));
            when(empruntRepository.findByUtilisateur(user))
                    .thenReturn(List.of(emprunt("EN_COURS"), emprunt("RENDU")));

            List<Emprunt> result = empruntService.getMesEmprunts("jean@test.fr");

            assertThat(result).hasSize(2);
        }
    }
}
