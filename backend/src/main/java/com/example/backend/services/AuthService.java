package com.example.backend.services;

import com.example.backend.model.entity.dto.RegisterRequestDto;
import com.example.backend.model.entity.Role;
import com.example.backend.model.entity.Utilisateur;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void inscrire(RegisterRequestDto registerDto) {
        // Vérification si l'email existe déjà
        if (utilisateurRepository.existsByMail(registerDto.getMail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // Création de l'entité
        Utilisateur user = new Utilisateur();
        user.setNom(registerDto.getNom());
        user.setPrenom(registerDto.getPrenom());
        user.setMail(registerDto.getMail());

        // Hachage du mot de passe (BCrypt coût 12 via le Bean config)
        user.setMotDePasse(passwordEncoder.encode(registerDto.getMotDePasse()));

        // Récupération du rôle LECTEUR
        Role roleLecteur = roleRepository.findByLibelle("LECTEUR")
                .orElseThrow(() -> new RuntimeException("Erreur : Le rôle LECTEUR n'existe pas en base"));

        user.setRole(roleLecteur);

        // Sauvegarde finale en base
        utilisateurRepository.save(user);
    }
}