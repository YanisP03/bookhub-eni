package com.example.backend.controller;


import com.example.backend.config.JwtUtils;
import com.example.backend.model.entity.dto.LoginRequestDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.AuthenticationException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;


    @PostMapping("/connexion")
    public ResponseEntity<?> connexion(@Valid @RequestBody LoginRequestDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getMail(), loginDto.getMotDePasse())
            );

            //Génération Token
            String jwt = jwtUtils.generateJwtToken(authentication);

            // 3. PRÉPARATION DE LA RÉPONSE (JSON)

            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("message", "Connexion réussie");
            response.put("email", authentication.getName());

            // Si l'auth réussit
            return ResponseEntity.ok("Connexion réussie");

        } catch (AuthenticationException e) {
            // Si l'auth échoue
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants incorrects");
        }
    }
}

