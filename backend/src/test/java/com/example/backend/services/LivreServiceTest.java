package com.example.backend.services;

import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.entity.Livre;
import com.example.backend.repository.LivreRepository;
import com.example.backend.repository.StatutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivreServiceTest {

    @Mock LivreRepository  livreRepository;
    @Mock StatutRepository statutRepository;
    @Mock JdbcTemplate     jdbcTemplate;

    @InjectMocks LivreService livreService;

    private Livre livre(Integer id, String titre, int nbDispo, BigDecimal note) {
        Livre l = new Livre();
        l.setId(id);
        l.setTitre(titre);
        l.setNbDisponibles(nbDispo);
        if (note != null) {
            // noteMoyenne est insertable=false/updatable=false, on ne peut pas le setter directement
            // On le set via réflexion pour le test
            try {
                var field = Livre.class.getDeclaredField("noteMoyenne");
                field.setAccessible(true);
                field.set(l, note);
            } catch (Exception ignored) {}
        }
        return l;
    }

    // ═══════════════════════════════════════════════════════════
    // findPopulaires
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findPopulaires()")
    class FindPopulaires {

        @Test
        @DisplayName("Retourne au plus `limit` livres")
        void respecteLimite() {
            List<Livre> tous = List.of(
                    livre(1, "A", 1, null), livre(2, "B", 1, null),
                    livre(3, "C", 1, null), livre(4, "D", 1, null),
                    livre(5, "E", 1, null), livre(6, "F", 1, null)
            );
            when(livreRepository.findOrderedByBorrowCount()).thenReturn(tous);

            List<Livre> result = livreService.findPopulaires(3);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("limit > taille de la liste → retourne tout")
        void limitSuperieur_retourneTout() {
            when(livreRepository.findOrderedByBorrowCount())
                    .thenReturn(List.of(livre(1, "A", 1, null), livre(2, "B", 1, null)));

            List<Livre> result = livreService.findPopulaires(10);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Liste vide → résultat vide")
        void listeVide_resultatVide() {
            when(livreRepository.findOrderedByBorrowCount()).thenReturn(List.of());

            assertThat(livreService.findPopulaires(5)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // findMieuxNotes
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findMieuxNotes()")
    class FindMieuxNotes {

        @Test
        @DisplayName("Retourne au plus `limit` livres avec note")
        void respecteLimite() {
            List<Livre> notes = List.of(
                    livre(1, "A", 1, new BigDecimal("4.8")),
                    livre(2, "B", 1, new BigDecimal("4.5")),
                    livre(3, "C", 1, new BigDecimal("4.0")),
                    livre(4, "D", 1, new BigDecimal("3.5"))
            );
            when(livreRepository.findByNoteMoyenneIsNotNullOrderByNoteMoyenneDesc()).thenReturn(notes);

            List<Livre> result = livreService.findMieuxNotes(2);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Aucun livre noté → liste vide")
        void aucunLivreNote_listeVide() {
            when(livreRepository.findByNoteMoyenneIsNotNullOrderByNoteMoyenneDesc())
                    .thenReturn(List.of());

            assertThat(livreService.findMieuxNotes(4)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // deleteById
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("Cas nominal → livre supprimé")
        void happyPath_supprime() {
            when(livreRepository.existsById(42)).thenReturn(true);
            when(livreRepository.hasEmpruntsActifs(42)).thenReturn(false);

            livreService.deleteById(42);

            verify(livreRepository).deleteById(42);
        }

        @Test
        @DisplayName("Livre introuvable → ResourceNotFoundException")
        void livreInconnu_throwsResourceNotFoundException() {
            when(livreRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> livreService.deleteById(999))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(livreRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Livre avec emprunts actifs → BusinessException")
        void avecEmpruntsActifs_throwsBusinessException() {
            when(livreRepository.existsById(42)).thenReturn(true);
            when(livreRepository.hasEmpruntsActifs(42)).thenReturn(true);

            assertThatThrownBy(() -> livreService.deleteById(42))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("emprunts en cours");

            verify(livreRepository, never()).deleteById(any());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // findById / findAll
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Lecture simple")
    class LectureSimple {

        @Test
        @DisplayName("findById existant → Optional présent")
        void findById_existant_optionalPresent() {
            Livre l = livre(1, "Dune", 2, null);
            when(livreRepository.findById(1)).thenReturn(Optional.of(l));

            assertThat(livreService.findById(1)).isPresent().contains(l);
        }

        @Test
        @DisplayName("findById inconnu → Optional vide")
        void findById_inconnu_optionalVide() {
            when(livreRepository.findById(999)).thenReturn(Optional.empty());

            assertThat(livreService.findById(999)).isEmpty();
        }

        @Test
        @DisplayName("findAll → délègue au repository")
        void findAll_delegueRepository() {
            List<Livre> liste = List.of(livre(1, "A", 1, null));
            when(livreRepository.findAll()).thenReturn(liste);

            assertThat(livreService.findAll()).hasSize(1);
            verify(livreRepository).findAll();
        }
    }
}
