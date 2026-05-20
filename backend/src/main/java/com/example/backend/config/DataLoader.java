package com.example.backend.config;

import com.example.backend.model.entity.*;
import com.example.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final LivreRepository livreRepository;
    private final CategorieRepository categorieRepository;
    private final StatutRepository statutRepository;
    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmpruntRepository empruntRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(LivreRepository livreRepository,
                      CategorieRepository categorieRepository,
                      StatutRepository statutRepository,
                      RoleRepository roleRepository,
                      UtilisateurRepository utilisateurRepository,
                      EmpruntRepository empruntRepository,
                      PasswordEncoder passwordEncoder) {
        this.livreRepository = livreRepository;
        this.categorieRepository = categorieRepository;
        this.statutRepository = statutRepository;
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.empruntRepository = empruntRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // ── Rôles ────────────────────────────────────────────────────────────────
        Role roleLecteur = getOrCreateRole("LECTEUR");
        Role roleBiblio  = getOrCreateRole("BIBLIOTHECAIRE");
        Role roleAdmin   = getOrCreateRole("ADMIN");

        // ── Statuts ──────────────────────────────────────────────────────────────
        Statut disponible = getOrCreateStatut("DISPONIBLE");
        Statut emprunte   = getOrCreateStatut("EMPRUNTE");
        Statut rendu      = getOrCreateStatut("RENDU");
        getOrCreateStatut("DEMANDE");
        getOrCreateStatut("EN_COURS");
        getOrCreateStatut("RETOUR_DEMANDE");
        getOrCreateStatut("ANNULE");
        getOrCreateStatut("EN_ATTENTE");
        getOrCreateStatut("ANNULEE");
        getOrCreateStatut("NOTIFIEE");
        getOrCreateStatut("CONVERTIE");

        // ── Utilisateurs ─────────────────────────────────────────────────────────
        createUser("admin@bookhub.fr",        "Admin",    "BookHub",  "Admin@123456!", roleAdmin,   false, 0, null);
        createUser("test@bookhub.fr",          "Dupont",   "Jean",     "Test@123456!",  roleLecteur, false, 0, null);
        createUser("biblio@bookhub.fr",        "Martin",   "Paul",     "Admin@123456!", roleBiblio,  false, 0, null);
        createUser("retardataire@bookhub.fr",  "Durand",   "Marie",    "Admin@123456!", roleLecteur, false, 2, null);
        createUser("bloque@bookhub.fr",        "Bernard",  "Luc",      "Admin@123456!", roleLecteur, true,  0, LocalDateTime.now().plusDays(3));

        // ── Livres (seulement au premier démarrage) ──────────────────────────────
        if (livreRepository.count() == 0) {
            Categorie fantasy    = getOrCreateCategorie("Fantasy");
            Categorie scifi      = getOrCreateCategorie("Science-Fiction");
            Categorie roman      = getOrCreateCategorie("Roman");
            Categorie policier   = getOrCreateCategorie("Policier");
            Categorie manga      = getOrCreateCategorie("Manga");
            Categorie histoire   = getOrCreateCategorie("Histoire");
            Categorie jeunesse   = getOrCreateCategorie("Jeunesse");
            Categorie biographie = getOrCreateCategorie("Biographie");
            Categorie sciences   = getOrCreateCategorie("Sciences");

            saveLivre("Le Seigneur des Anneaux",     "J.R.R. Tolkien",     "978-2-07-061351-6", fantasy,    "La grande épopée fantastique.", 3, 2, disponible);
            saveLivre("Dune",                        "Frank Herbert",       "978-2-07-036024-1", scifi,      "Le destin de Paul Atréides sur Arrakis.", 2, 1, disponible);
            saveLivre("Harry Potter",                "J.K. Rowling",        "978-2-07-054127-1", fantasy,    "Harry découvre qu'il est sorcier.", 4, 3, disponible);
            saveLivre("Le Comte de Monte-Cristo",    "Alexandre Dumas",     "978-2-07-040850-4", roman,      "Edmond Dantès prépare sa vengeance.", 2, 2, disponible);
            saveLivre("1984",                        "George Orwell",       "978-2-07-036822-3", scifi,      "Winston résiste au Parti totalitaire.", 3, 0, emprunte);
            saveLivre("Sherlock Holmes",             "Arthur Conan Doyle",  "978-2-07-041239-6", policier,   "Les enquêtes du célèbre détective.", 2, 2, disponible);
            saveLivre("Naruto",                      "Masashi Kishimoto",   "978-2-87-182636-4", manga,      "Un jeune ninja rêve de devenir Hokage.", 5, 4, disponible);
            saveLivre("Sapiens",                     "Yuval Noah Harari",   "978-2-07-273698-6", histoire,   "Une brève histoire de l'humanité.", 3, 3, disponible);
            saveLivre("Le Petit Prince",             "Saint-Exupéry",       "978-2-07-040850-1", jeunesse,   "Un aviateur rencontre un petit prince.", 4, 4, disponible);
            saveLivre("Steve Jobs",                  "Walter Isaacson",     "978-2-07-044588-2", biographie, "La bio officielle du cofondateur d'Apple.", 2, 1, disponible);
            saveLivre("Une brève histoire du temps", "Stephen Hawking",     "978-2-07-053478-5", sciences,   "Des trous noirs aux origines de l'univers.", 2, 2, disponible);
        }

        // ── Emprunts de démo (historique de retards) ─────────────────────────────
        createDemoEmprunts(rendu);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────────

    private void createUser(String mail, String nom, String prenom, String mdp,
                             Role role, boolean bloque, int nbRetards, LocalDateTime dateBlocage) {
        if (utilisateurRepository.existsByMail(mail)) return;
        Utilisateur u = new Utilisateur();
        u.setNom(nom); u.setPrenom(prenom); u.setMail(mail);
        u.setMotDePasse(passwordEncoder.encode(mdp));
        u.setRole(role);
        u.setBloque(bloque);
        u.setNbRetards(nbRetards);
        u.setDateBlocageAuto(dateBlocage);
        utilisateurRepository.save(u);
    }

    private void createDemoEmprunts(Statut rendu) {
        Utilisateur retardataire = utilisateurRepository.findByMail("retardataire@bookhub.fr").orElse(null);
        Utilisateur bloque       = utilisateurRepository.findByMail("bloque@bookhub.fr").orElse(null);
        if (retardataire == null || bloque == null) return;

        List<Livre> livres = livreRepository.findAll();
        if (livres.size() < 3) return;

        // Ajoute les emprunts seulement si l'utilisateur n'en a pas encore
        if (empruntRepository.findByUtilisateur(retardataire).isEmpty()) {
            saveEmprunt(retardataire, livres.get(0), rendu, 60, 46, 40, 6);
            saveEmprunt(retardataire, livres.get(1), rendu, 30, 16, 10, 6);
        }
        if (empruntRepository.findByUtilisateur(bloque).isEmpty()) {
            saveEmprunt(bloque, livres.get(0), rendu, 90, 76, 70, 6);
            saveEmprunt(bloque, livres.get(1), rendu, 60, 46, 40, 8);
            saveEmprunt(bloque, livres.get(2), rendu, 20,  6,  1, 5);
        }
    }

    private void saveEmprunt(Utilisateur u, Livre livre, Statut statut,
                              int debutJoursAgo, int prevuJoursAgo, int retourJoursAgo, int joursRetard) {
        Emprunt e = new Emprunt();
        e.setUtilisateur(u);
        e.setLivre(livre);
        e.setStatut(statut);
        e.setDateEmprunt(LocalDateTime.now().minusDays(debutJoursAgo));
        e.setDateRetourPrevue(LocalDateTime.now().minusDays(prevuJoursAgo));
        e.setDateRetour(LocalDateTime.now().minusDays(retourJoursAgo));
        e.setJoursRetard(joursRetard);
        empruntRepository.save(e);
    }

    private Role getOrCreateRole(String libelle) {
        return roleRepository.findFirstByLibelle(libelle).orElseGet(() -> {
            Role r = new Role(); r.setLibelle(libelle); return roleRepository.save(r);
        });
    }

    private Statut getOrCreateStatut(String libelle) {
        return statutRepository.findFirstByLibelle(libelle).orElseGet(() -> {
            Statut s = new Statut(); s.setLibelle(libelle); return statutRepository.save(s);
        });
    }

    private Categorie getOrCreateCategorie(String nom) {
        return categorieRepository.findAll().stream()
                .filter(c -> c.getNom().equalsIgnoreCase(nom)).findFirst()
                .orElseGet(() -> { Categorie c = new Categorie(); c.setNom(nom); return categorieRepository.save(c); });
    }

    private void saveLivre(String titre, String auteur, String isbn, Categorie cat,
                            String desc, int nbEx, int nbDispo, Statut statut) {
        Livre l = new Livre();
        l.setTitre(titre); l.setAuteur(auteur); l.setIsbn(isbn);
        l.setCategorie(cat); l.setDescription(desc);
        l.setNbExemplaires(nbEx); l.setNbDisponibles(nbDispo);
        l.setStatut(statut); l.setDateAjout(LocalDateTime.now());
        livreRepository.save(l);
    }
}
