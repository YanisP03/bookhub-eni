package com.example.backend.controller;

import com.example.backend.config.JwtUtils;
import com.example.backend.model.entity.Utilisateur;
import com.example.backend.model.entity.dto.LoginRequestDto;
import com.example.backend.model.entity.dto.LoginResponseDto;
import com.example.backend.model.entity.dto.RegisterRequestDto;
import com.example.backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;

    @Autowired
    // Injection par constructeur (meilleure pratique que @Autowired sur les champs)
    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.authService = authService;
    }

    /**
     * Endpoint de connexion (US-AUTH-02 du CDC)
     * URL obligatoire : /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> connexion(@Valid @RequestBody LoginRequestDto loginDto) {
        try {
            // 1. Authentification de l'utilisateur via Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getMail(), loginDto.getMotDePasse())
            );

            // 2. Génération du Token JWT via votre classe utilitaire
            String jwt = jwtUtils.generateJwtToken(authentication);

            // 3. Extraction du rôle de l'utilisateur connecté pour le passer au Front-end
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_LECTEUR");

            // 4. Renvoi du LoginResponseDto contenant le token, l'email et le rôle
            // C'est cet objet que votre application Angular va intercepter et stocker
            LoginResponseDto response = new LoginResponseDto(jwt, authentication.getName(), role);
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            // En cas d'échec, renvoi d'une erreur 401 Unauthorized propre au format JSON
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Identifiants incorrects");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Endpoint d'inscription (US-AUTH-01 du CDC)
     * URL obligatoire : /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> inscription(@Valid @RequestBody RegisterRequestDto registerDto) {
        try {
            // Appel au service pour enregistrer le lecteur
            authService.inscrire(registerDto);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Utilisateur enregistré avec succès !");
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);

        } catch (RuntimeException e) {
            // Si l'email existe déjà par exemple (Erreur 400 Bad Request)
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}