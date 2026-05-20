package com.example.backend.controller;

import com.example.backend.config.JwtUtils;
import com.example.backend.dto.LoginRequestDto;
import com.example.backend.dto.LoginResponseDto;
import com.example.backend.dto.RegisterRequestDto;
import com.example.backend.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Authentification", description = "Connexion et inscription des utilisateurs")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.authService = authService;
    }

    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un token JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Connexion réussie — token JWT retourné"),
        @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    })
    @PostMapping("/login")
    public ResponseEntity<?> connexion(@Valid @RequestBody LoginRequestDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getMail(), loginDto.getMotDePasse())
            );
            String jwt = jwtUtils.generateJwtToken(authentication);
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_LECTEUR");
            return ResponseEntity.ok(new LoginResponseDto(jwt, authentication.getName(), role));
        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Identifiants incorrects");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @Operation(summary = "Inscription", description = "Crée un nouveau compte lecteur")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Compte créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Email déjà utilisé ou données invalides")
    })
    @PostMapping("/register")
    public ResponseEntity<?> inscription(@Valid @RequestBody RegisterRequestDto registerDto) {
        try {
            authService.inscrire(registerDto);
            Map<String, String> ok = new HashMap<>();
            ok.put("message", "Utilisateur enregistré avec succès !");
            return ResponseEntity.status(HttpStatus.CREATED).body(ok);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Profil JWT", description = "Retourne l'email et les rôles de l'utilisateur connecté")
    @ApiResponse(responseCode = "200", description = "Informations de l'utilisateur courant")
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non authentifié");
        return ResponseEntity.ok(Map.of("email", user.getUsername(), "roles", user.getAuthorities()));
    }
}
