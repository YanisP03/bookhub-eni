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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock LivreRepository       livreRepository;
    @Mock UtilisateurRepository utilisateurRepository;
    @Mock StatutRepository      statutRepository;

    @InjectMocks ReservationService reservationService;

    private Utilisateur user;
    private Livre       livre;

    @BeforeEach
    void setUp() {
        user = new Utilisateur();
        user.setId(1);
        user.setMail("jean@test.fr");

        livre = new Livre();
        livre.setId(42);
        livre.setTitre("Dune");
        livre.setNbDisponibles(0); // épuisé par défaut pour les réservations

        lenient().when(statutRepository.findFirstByLibelle(anyString()))
                 .thenAnswer(inv -> Optional.of(statut(inv.getArgument(0))));
    }

    private Statut statut(String libelle) {
        Statut s = new Statut();
        s.setLibelle(libelle);
        return s;
    }

    // ═══════════════════════════════════════════════════════════
    // reserverLivre
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("reserverLivre()")
    class ReserverLivre {

        @BeforeEach
        void mockBase() {
            when(utilisateurRepository.findByMail("jean@test.fr")).thenReturn(Optional.of(user));
            when(livreRepository.findById(42)).thenReturn(Optional.of(livre));
            when(reservationRepository.findByUtilisateurAndLivreAndStatut(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(reservationRepository.findByLivreAndStatutOrderByDateReservationAsc(any(), any()))
                    .thenReturn(List.of());
            when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("Cas nominal → réservation créée en position 1")
        void happyPath_premierDansLaFile() {
            Reservation result = reservationService.reserverLivre(42, "jean@test.fr");

            assertThat(result.getStatut().getLibelle()).isEqualTo("EN_ATTENTE");
            assertThat(result.getPositionFileAttente()).isEqualTo(1);
            assertThat(result.getUtilisateur()).isEqualTo(user);
            assertThat(result.getLivre()).isEqualTo(livre);
        }

        @Test
        @DisplayName("Déjà 2 réservations → position 3")
        void deuxReservationsExistantes_position3() {
            Reservation r1 = new Reservation(); r1.setPositionFileAttente(1);
            Reservation r2 = new Reservation(); r2.setPositionFileAttente(2);
            when(reservationRepository.findByLivreAndStatutOrderByDateReservationAsc(any(), any()))
                    .thenReturn(List.of(r1, r2));

            Reservation result = reservationService.reserverLivre(42, "jean@test.fr");

            assertThat(result.getPositionFileAttente()).isEqualTo(3);
        }

        @Test
        @DisplayName("Livre disponible → BusinessException (emprunter directement)")
        void livreDisponible_throwsBusinessException() {
            livre.setNbDisponibles(2);

            assertThatThrownBy(() -> reservationService.reserverLivre(42, "jean@test.fr"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("disponible");
        }

        @Test
        @DisplayName("Réservation active déjà existante → BusinessException")
        void reservationDejaActive_throwsBusinessException() {
            Reservation existing = new Reservation();
            existing.setStatut(statut("EN_ATTENTE"));
            when(reservationRepository.findByUtilisateurAndLivreAndStatut(any(), any(), any()))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> reservationService.reserverLivre(42, "jean@test.fr"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("réservation");
        }

        @Test
        @DisplayName("Utilisateur inconnu → ResourceNotFoundException")
        void utilisateurInconnu_throwsResourceNotFoundException() {
            when(utilisateurRepository.findByMail(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.reserverLivre(42, "inconnu@test.fr"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // annulerReservation
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("annulerReservation()")
    class AnnulerReservation {

        @Test
        @DisplayName("Cas nominal → statut ANNULEE + positions recalculées")
        void happyPath_annuleEtRecalcule() {
            Reservation resa = new Reservation();
            resa.setId(10);
            resa.setUtilisateur(user);
            resa.setLivre(livre);
            resa.setStatut(statut("EN_ATTENTE"));
            resa.setDateReservation(LocalDateTime.now());

            // Une autre réservation en position 2 doit passer à 1 après annulation
            Reservation suivante = new Reservation();
            suivante.setPositionFileAttente(2);

            when(reservationRepository.findById(10)).thenReturn(Optional.of(resa));
            when(reservationRepository.findByLivreAndStatutOrderByDateReservationAsc(any(), any()))
                    .thenReturn(List.of(suivante));

            reservationService.annulerReservation(10, "jean@test.fr");

            assertThat(resa.getStatut().getLibelle()).isEqualTo("ANNULEE");
            assertThat(suivante.getPositionFileAttente()).isEqualTo(1);
            verify(reservationRepository).save(resa);
        }

        @Test
        @DisplayName("Réservation d'un autre utilisateur → BusinessException")
        void autreUtilisateur_throwsBusinessException() {
            Utilisateur autre = new Utilisateur();
            autre.setMail("autre@test.fr");
            Reservation resa = new Reservation();
            resa.setUtilisateur(autre);
            when(reservationRepository.findById(10)).thenReturn(Optional.of(resa));

            assertThatThrownBy(() -> reservationService.annulerReservation(10, "jean@test.fr"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("appartient");
        }

        @Test
        @DisplayName("Réservation introuvable → ResourceNotFoundException")
        void reservationInconnue_throwsResourceNotFoundException() {
            when(reservationRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.annulerReservation(999, "jean@test.fr"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
