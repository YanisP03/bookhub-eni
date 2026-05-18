package com.example.backend.services;

import com.example.backend.model.entity.Utilisateur;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    // Injection par constructeur (Clean Code)
    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Méthode obligatoire de l'interface UserDetailsService de Spring Security.
     * Ici, le "username" correspond à l'email de l'utilisateur.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. On cherche l'utilisateur dans la base de données par son email
        Utilisateur utilisateur = utilisateurRepository.findByMail(email) // Adapte par findByEmail si besoin
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // 2. On extrait le rôle de l'utilisateur (ex: "ROLE_LECTEUR")
        String roleNom = "ROLE_LECTEUR"; // Rôle par défaut par sécurité
        if (utilisateur.getRole() != null) {
            roleNom = utilisateur.getRole().getLibelle();

            // Sécurité : Spring Security s'attend souvent à ce que les rôles commencent par "ROLE_"
            if (!roleNom.startsWith("ROLE_")) {
                roleNom = "ROLE_" + roleNom;
            }
        }

        // 3. On retourne l'objet User officiel de Spring Security
        // Il contient l'email, le mot de passe haché de la BDD, et la liste des rôles (authorities)
        return new User(
                utilisateur.getMail(),
                utilisateur.getMotDePasse(),
                Collections.singletonList(new SimpleGrantedAuthority(roleNom))
        );
    }
}