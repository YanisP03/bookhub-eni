package com.example.backend.config;

import com.example.backend.model.entity.*;
import com.example.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final LivreRepository livreRepository;
    private final CategorieRepository categorieRepository;
    private final StatutRepository statutRepository;

    public DataLoader(LivreRepository livreRepository,
                      CategorieRepository categorieRepository,
                      StatutRepository statutRepository) {
        this.livreRepository = livreRepository;
        this.categorieRepository = categorieRepository;
        this.statutRepository = statutRepository;
    }

    @Override
    public void run(String... args) {
        if (livreRepository.count() > 0) return;

        Categorie fantasy    = getOrCreate("Fantasy");
        Categorie scifi      = getOrCreate("Science-Fiction");
        Categorie roman      = getOrCreate("Roman");
        Categorie policier   = getOrCreate("Policier");
        Categorie manga      = getOrCreate("Manga");
        Categorie histoire   = getOrCreate("Histoire");
        Categorie jeunesse   = getOrCreate("Jeunesse");
        Categorie biographie = getOrCreate("Biographie");
        Categorie sciences   = getOrCreate("Sciences");

        Statut disponible = statutRepository.findByLibelle("DISPONIBLE").orElseThrow();
        Statut emprunte   = statutRepository.findByLibelle("EMPRUNTE").orElseThrow();

        save("Le Seigneur des Anneaux", "J.R.R. Tolkien",  "978-2-07-061351-6", fantasy,    "La grande épopée fantastique.", 3, 2, disponible);
        save("Dune",                   "Frank Herbert",    "978-2-07-036024-1", scifi,      "Le destin de Paul Atréides sur Arrakis.", 2, 1, disponible);
        save("Harry Potter",           "J.K. Rowling",     "978-2-07-054127-1", fantasy,    "Harry découvre qu'il est sorcier.", 4, 3, disponible);
        save("Le Comte de Monte-Cristo","Alexandre Dumas", "978-2-07-040850-4", roman,      "Edmond Dantès prépare sa vengeance.", 2, 2, disponible);
        save("1984",                   "George Orwell",    "978-2-07-036822-3", scifi,      "Winston résiste au Parti totalitaire.", 3, 0, emprunte);
        save("Sherlock Holmes",        "Arthur Conan Doyle","978-2-07-041239-6",policier,   "Les enquêtes du célèbre détective.", 2, 2, disponible);
        save("Naruto",                 "Masashi Kishimoto","978-2-87-182636-4", manga,      "Un jeune ninja rêve de devenir Hokage.", 5, 4, disponible);
        save("Sapiens",                "Yuval Noah Harari","978-2-07-273698-6", histoire,   "Une brève histoire de l'humanité.", 3, 3, disponible);
        save("Le Petit Prince",        "Saint-Exupéry",    "978-2-07-040850-1", jeunesse,   "Un aviateur rencontre un petit prince.", 4, 4, disponible);
        save("Steve Jobs",             "Walter Isaacson",  "978-2-07-044588-2", biographie, "La bio officielle du cofondateur d'Apple.", 2, 1, disponible);
        save("Une brève histoire du temps","Stephen Hawking","978-2-07-053478-5",sciences, "Des trous noirs aux origines de l'univers.", 2, 2, disponible);
    }

    private Categorie getOrCreate(String nom) {
        return categorieRepository.findAll().stream()
                .filter(c -> c.getNom().equalsIgnoreCase(nom))
                .findFirst().orElseGet(() -> {
                    Categorie c = new Categorie();
                    c.setNom(nom);
                    return categorieRepository.save(c);
                });
    }

    private void save(String titre, String auteur, String isbn, Categorie cat,
                      String desc, int nbEx, int nbDispo, Statut statut) {
        Livre l = new Livre();
        l.setTitre(titre); l.setAuteur(auteur); l.setIsbn(isbn);
        l.setCategorie(cat); l.setDescription(desc);
        l.setNbExemplaires(nbEx); l.setNbDisponibles(nbDispo);
        l.setStatut(statut); l.setDateAjout(LocalDateTime.now());
        livreRepository.save(l);
    }
}