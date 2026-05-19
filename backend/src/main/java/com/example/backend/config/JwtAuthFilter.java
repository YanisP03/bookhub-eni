package com.example.backend.config;

import com.example.backend.services.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtils jwtUtils, CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        String uri    = request.getRequestURI();

        try {
            String jwt = parseJwt(request);

            System.out.printf("[JWT] %s %s | token présent: %b%n", method, uri, jwt != null);

            if (jwt != null) {
                boolean valid = jwtUtils.validateJwtToken(jwt);
                System.out.printf("[JWT] Token valide: %b%n", valid);

                if (valid) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    System.out.printf("[JWT] Username extrait: %s%n", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    System.out.printf("[JWT] User chargé: %s | roles: %s%n",
                            userDetails.getUsername(), userDetails.getAuthorities());

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.printf("[JWT] Authentification définie dans le contexte%n");
                }
            } else {
                System.out.printf("[JWT] Pas de token pour %s %s%n", method, uri);
            }
        } catch (Exception e) {
            System.err.printf("[JWT] ERREUR pour %s %s : %s - %s%n",
                    method, uri, e.getClass().getSimpleName(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
