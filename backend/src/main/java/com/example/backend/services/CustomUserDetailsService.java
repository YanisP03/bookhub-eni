//package com.example.backend.services;
//
//import com.example.backend.model.entity.Utilisateur;
//import com.example.backend.repository.UtilisateurRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    @Autowired
//    private UtilisateurRepository repository;
//
//    @Override
//    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
//        Utilisateur user = repository.findByMail(mail)
//                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
//
//        return org.springframework.security.core.userdetails.User
//                .withUsername(user.getMail())
//                .password(user.getMotDePasse())
//                .authorities("ROLE_" + user.getRole().getLibelle()) // Assure-toi que Role a un nom
//                .build();
//    }
//}
