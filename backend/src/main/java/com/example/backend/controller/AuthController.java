package com.example.backend.controller;

import com.example.backend.config.JwtUtils;
import com.example.backend.model.entity.Utilisateur;
import com.example.backend.model.entity.dto.LoginRequestDto;
import com.example.backend.model.entity.dto.LoginResponseDto;
import com.example.backend.repository.UtilisateurRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UtilisateurRepository utilisateurRepository;

    @PostMapping("/connexion")
    public ResponseEntity<?> connexion(@Valid @RequestBody LoginRequestDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getMail(), loginDto.getMotDePasse())
            );
            String jwt = jwtUtils.generateJwtToken(authentication);
            Utilisateur user = utilisateurRepository.findByMail(loginDto.getMail()).orElseThrow();
            return ResponseEntity.ok(new LoginResponseDto(
                    jwt,
                    user.getMail(),
                    user.getNom(),
                    user.getPrenom(),
                    user.getRole().getLibelle()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants incorrects");
        }
    }
}
