package com.example.backend.services;

import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.entity.Role;
import com.example.backend.model.entity.Utilisateur;
import com.example.backend.dto.ChangerMotDePasseDto;
import com.example.backend.dto.CreerUtilisateurDto;
import com.example.backend.dto.ProfilUpdateDto;
import com.example.backend.dto.RegisterRequestDto;
import com.example.backend.dto.UtilisateurDto;
import com.example.backend.repository.AvisRepository;
import com.example.backend.repository.EmpruntRepository;
import com.example.backend.repository.ReservationRepository;
import com.example.backend.repository.RetourRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RetourRepository retourRepository;
    private final EmpruntRepository empruntRepository;
    private final ReservationRepository reservationRepository;
    private final AvisRepository avisRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder,
                              RetourRepository retourRepository,
                              EmpruntRepository empruntRepository,
                              ReservationRepository reservationRepository,
                              AvisRepository avisRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.retourRepository = retourRepository;
        this.empruntRepository = empruntRepository;
        this.reservationRepository = reservationRepository;
        this.avisRepository = avisRepository;
    }

    @Transactional(readOnly = true)
    public UtilisateurDto recupererProfilParEmail(String email) {
        return toDto(findByMail(email));
    }

    @Transactional
    public UtilisateurDto mettreAJourProfil(String email, ProfilUpdateDto dto) {
        Utilisateur u = findByMail(email);
        if (dto.getNom() != null && !dto.getNom().isBlank()) u.setNom(dto.getNom());
        if (dto.getPrenom() != null && !dto.getPrenom().isBlank()) u.setPrenom(dto.getPrenom());
        u.setTelephone(dto.getTelephone());
        return toDto(utilisateurRepository.save(u));
    }

    @Transactional
    public void changerMotDePasse(String email, ChangerMotDePasseDto dto) {
        Utilisateur u = findByMail(email);
        if (!passwordEncoder.matches(dto.getAncienMotDePasse(), u.getMotDePasse()))
            throw new BusinessException("Ancien mot de passe incorrect.");
        u.setMotDePasse(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        utilisateurRepository.save(u);
    }

    @Transactional
    public void supprimerCompte(String email, String motDePasse) {
        Utilisateur u = findByMail(email);
        if (!passwordEncoder.matches(motDePasse, u.getMotDePasse()))
            throw new BusinessException("Mot de passe incorrect.");
        retourRepository.deleteByUtilisateur(u);
        empruntRepository.deleteByUtilisateur(u);
        reservationRepository.deleteByUtilisateur(u);
        avisRepository.deleteByUtilisateur(u);
        utilisateurRepository.delete(u);
    }

    @Transactional
    public UtilisateurDto promouvoirBibliothecaire(String mail) {
        Utilisateur u = findByMail(mail);
        Role role = roleRepository.findFirstByLibelle("BIBLIOTHECAIRE")
                .orElseThrow(() -> new ResourceNotFoundException("Rôle BIBLIOTHECAIRE introuvable"));
        u.setRole(role);
        return toDto(utilisateurRepository.save(u));
    }

    @Transactional
    public UtilisateurDto creerBibliothecaire(RegisterRequestDto dto) {
        if (utilisateurRepository.existsByMail(dto.getMail()))
            throw new BusinessException("Cet email est déjà utilisé.");
        Role role = roleRepository.findFirstByLibelle("BIBLIOTHECAIRE")
                .orElseThrow(() -> new ResourceNotFoundException("Rôle BIBLIOTHECAIRE introuvable"));
        Utilisateur u = new Utilisateur();
        u.setNom(dto.getNom());
        u.setPrenom(dto.getPrenom());
        u.setMail(dto.getMail());
        u.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        u.setRole(role);
        return toDto(utilisateurRepository.save(u));
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDto> listerRetardataires() {
        return utilisateurRepository.findAll().stream()
                .filter(u -> u.isBloque() || u.getNbRetards() > 0)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UtilisateurDto debloquerUtilisateur(Integer id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        u.setBloque(false);
        u.setDateBlocageAuto(null);
        u.setNbRetards(0);
        return toDto(utilisateurRepository.save(u));
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDto> listerTous() {
        return utilisateurRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void supprimerParId(Integer id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        retourRepository.deleteByUtilisateur(u);
        empruntRepository.deleteByUtilisateur(u);
        reservationRepository.deleteByUtilisateur(u);
        avisRepository.deleteByUtilisateur(u);
        utilisateurRepository.delete(u);
    }

    @Transactional
    public UtilisateurDto creerUtilisateur(CreerUtilisateurDto dto) {
        if (utilisateurRepository.existsByMail(dto.getMail()))
            throw new BusinessException("Cet email est déjà utilisé.");
        String roleLibelle = "BIBLIOTHECAIRE".equalsIgnoreCase(dto.getRole()) ? "BIBLIOTHECAIRE" : "LECTEUR";
        Role role = roleRepository.findFirstByLibelle(roleLibelle)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle " + roleLibelle + " introuvable."));
        Utilisateur u = new Utilisateur();
        u.setNom(dto.getNom());
        u.setPrenom(dto.getPrenom());
        u.setMail(dto.getMail());
        u.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        u.setRole(role);
        return toDto(utilisateurRepository.save(u));
    }

    private Utilisateur findByMail(String email) {
        return utilisateurRepository.findByMail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
    }

    private UtilisateurDto toDto(Utilisateur u) {
        return new UtilisateurDto(
                u.getId(), u.getNom(), u.getPrenom(), u.getMail(),
                u.getTelephone(),
                u.getRole() != null ? u.getRole().getLibelle() : null,
                u.getDateInscription(),
                u.getNbRetards(), u.isBloque(), u.getDateBlocageAuto()
        );
    }
}
