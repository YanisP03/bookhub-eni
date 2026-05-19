package com.example.backend.repository;

import com.example.backend.model.entity.Livre;
import com.example.backend.model.entity.Reservation;
import com.example.backend.model.entity.Statut;
import com.example.backend.model.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByUtilisateur(Utilisateur utilisateur);
    List<Reservation> findByLivreAndStatutOrderByDateReservationAsc(Livre livre, Statut statut);
    Optional<Reservation> findByUtilisateurAndLivreAndStatut(Utilisateur u, Livre l, Statut s);
    List<Reservation> findByLivreIdAndStatutLibelleOrderByPositionFileAttenteAsc(Integer livreId, String statutLibelle);
}