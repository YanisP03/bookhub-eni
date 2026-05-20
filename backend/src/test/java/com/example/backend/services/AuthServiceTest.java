package com.example.backend.services;

import com.example.backend.dto.RegisterRequestDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.entity.Role;
import com.example.backend.model.entity.Utilisateur;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UtilisateurRepository utilisateurRepository;
    @Mock RoleRepository        roleRepository;
    @Mock PasswordEncoder       passwordEncoder;

    @InjectMocks AuthService authService;

    private RegisterRequestDto dto;
    private Role roleLecteur;

    @BeforeEach
    void setUp() {
        dto = new RegisterRequestDto();
        dto.setNom("Dupont");
        dto.setPrenom("Jean");
        dto.setMail("jean@test.fr");
        dto.setMotDePasse("MonMotDePasse@1234!");

        roleLecteur = new Role();
        roleLecteur.setLibelle("LECTEUR");
    }

    // ═══════════════════════════════════════════════════════════
    // inscrire
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("inscrire()")
    class Inscrire {

        @Test
        @DisplayName("Cas nominal → utilisateur créé avec rôle LECTEUR et mot de passe haché")
        void happyPath_creeUtilisateur() {
            when(utilisateurRepository.existsByMail("jean@test.fr")).thenReturn(false);
            when(roleRepository.findFirstByLibelle("LECTEUR")).thenReturn(Optional.of(roleLecteur));
            when(passwordEncoder.encode(anyString())).thenReturn("$hashed$");
            when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            authService.inscrire(dto);

            ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
            verify(utilisateurRepository).save(captor.capture());

            Utilisateur saved = captor.getValue();
            assertThat(saved.getNom()).isEqualTo("Dupont");
            assertThat(saved.getPrenom()).isEqualTo("Jean");
            assertThat(saved.getMail()).isEqualTo("jean@test.fr");
            assertThat(saved.getMotDePasse()).isEqualTo("$hashed$");
            assertThat(saved.getRole().getLibelle()).isEqualTo("LECTEUR");
        }

        @Test
        @DisplayName("Mot de passe haché et non stocké en clair")
        void motDePasseHache_nonStockeEnClair() {
            when(utilisateurRepository.existsByMail(anyString())).thenReturn(false);
            when(roleRepository.findFirstByLibelle("LECTEUR")).thenReturn(Optional.of(roleLecteur));
            when(passwordEncoder.encode("MonMotDePasse@1234!")).thenReturn("$2a$12$hashed");
            when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            authService.inscrire(dto);

            ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
            verify(utilisateurRepository).save(captor.capture());
            assertThat(captor.getValue().getMotDePasse()).isNotEqualTo("MonMotDePasse@1234!");
        }

        @Test
        @DisplayName("Email déjà utilisé → RuntimeException")
        void emailDejaPris_throwsRuntimeException() {
            when(utilisateurRepository.existsByMail("jean@test.fr")).thenReturn(true);

            assertThatThrownBy(() -> authService.inscrire(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("mail", "utilisé", "Email", "déjà")
                    .satisfiesAnyOf(
                            e -> assertThat(e.getMessage()).containsIgnoringCase("mail"),
                            e -> assertThat(e.getMessage()).containsIgnoringCase("email")
                    );

            verify(utilisateurRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rôle LECTEUR absent en base → RuntimeException")
        void roleLecteurAbsent_throwsRuntimeException() {
            when(utilisateurRepository.existsByMail(anyString())).thenReturn(false);
            when(roleRepository.findFirstByLibelle("LECTEUR")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.inscrire(dto))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Inscription ne sauvegarde pas sans rôle valide")
        void sansPRole_neSauvegardePas() {
            when(utilisateurRepository.existsByMail(anyString())).thenReturn(false);
            when(roleRepository.findFirstByLibelle("LECTEUR")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.inscrire(dto)).isInstanceOf(RuntimeException.class);
            verify(utilisateurRepository, never()).save(any());
        }
    }
}
