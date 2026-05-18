package com.example.backend.services;

import com.example.backend.model.entity.Utilisateur;
import com.example.backend.model.entity.dto.UtilisateurDto;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    // Injection par constructeur
    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Récupère un utilisateur par son email et le transforme en DTO pour le contrôleur
     */
    public UtilisateurDto recupererProfilParEmail(String email) {
        // 1. Recherche de l'utilisateur en BDD
        Utilisateur utilisateur = utilisateurRepository.findByMail(email) // Adapte par findByEmail si ta méthode s'appelle ainsi
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // 2. Transformation de l'entité en DTO (Mapping manuel clean)
        UtilisateurDto dto = new UtilisateurDto();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setMail(utilisateur.getMail());

        // Gestion du rôle (on extrait le libellé si ton entité possède un objet Role)
        if (utilisateur.getRole() != null) {
            dto.setRoleLibelle(utilisateur.getRole().getLibelle()); // Adapte selon les méthodes de ton entité Role
        } else {
            dto.setRoleLibelle("ROLE_LECTEUR");
        }

        return dto;
    }
}
