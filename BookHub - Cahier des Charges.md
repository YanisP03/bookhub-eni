# Plateforme de Gestion de Bibliothèque Communautaire [BookHub - Cahier des Charges]
> DEV25_0364B - Projet Fullstack Java Angular

**Version :** 1.0
**Date :** Janvier 2026
**Client :** Association "Quartier Solidaire"
**Prestataire :** Équipe de développement (apprenants CDA)

---

# 📄 Sommaire

1. Présentation du projet
2. Contexte et enjeux
3. Objectifs
4. Périmètre fonctionnel
5. Spécifications fonctionnelles détaillées
6. Spécifications techniques
7. Spécifications de sécurité
8. Contraintes et exigences
9. Livrables et planning
10. Conditions de validation

---

# 1. Présentation du projet

## 1.1 Nom du projet

**BookHub** - Plateforme collaborative de gestion de bibliothèque

## 1.2 Contexte

L'association "Quartier Solidaire" gère depuis 15 ans une bibliothèque communautaire avec un fonds de 2000 ouvrages et environ 300 adhérents actifs. La gestion actuelle repose sur un système papier (fiches cartonnées, registres manuscrits) qui présente de nombreuses limites.

## 1.3 Problématiques identifiées

| Problématique | Impact | Priorité |
|---------------|--------|----------|
| Gestion manuelle des emprunts | Erreurs, perte de temps (30 min/jour) | ⭐⭐⭐ Haute |
| Pas de catalogue en ligne | Impossibilité de consulter à distance | ⭐⭐⭐ Haute |
| Pas de système de réservation | Déplacements inutiles des adhérents | ⭐⭐ Moyenne |
| Difficultés à gérer les retards | Relances manuelles chronophages | ⭐⭐⭐ Haute |
| Pas de statistiques | Impossibilité de piloter l'activité | ⭐ Basse |

## 1.4 Solution proposée

Développement d'une application web moderne permettant :

- La gestion complète des emprunts et réservations
- Un catalogue en ligne accessible 24/7
- Des tableaux de bord pour le pilotage
- La mise en conformité RGPD

---

# 2. Contexte et enjeux

## 2.1 Contexte organisationnel

### Association "Quartier Solidaire"

- **Type :** Association loi 1901
- **Activités :** Bibliothèque, ateliers culturels, aide aux devoirs
- **Effectif :** 3 salariés, 15 bénévoles
- **Adhérents :** 300 actifs, 450 total

### Équipe bibliothèque

- 1 bibliothécaire à temps partiel (Ahmed, 45 ans)
- 4 bénévoles (présence hebdomadaire)
- **Horaires d'ouverture :** Mardi au samedi, 14h-19h

## 2.2 Enjeux stratégiques

### Pour l'association

- **Modernisation :** Image plus moderne, attractive
- **Efficacité :** Automatisation, gain de temps
- **Accessibilité :** Service accessible à tous
- **Conformité :** Mise en conformité RGPD
- **Éco-responsabilité :** Réduction du papier

### Indicateurs de succès

- 80% des adhérents utilisent la plateforme après 6 mois
- Réduction de 50% du temps de gestion
- Note satisfaction ≥ 4/5

---

# 3. Objectifs

## 3.1 Objectif général

Créer une plateforme web complète, accessible et éco-conçue pour la gestion de la bibliothèque associative.

## 3.2 Objectifs spécifiques

### Fonctionnels

- Consultation du catalogue en ligne
- Automatisation des emprunts/retours
- Système de réservation
- Notation et commentaires
- Tableaux de bord statistiques

### Techniques

- Architecture multicouche REST
- Authentification JWT
- Base de données normalisée
- Procédures stockées et triggers présents et pertinents
- Responsive design
- Tests unitaires

### Qualité

- Sécurité ANSSI
- Performance < 2s
- Documentation complète

---

# 4. Périmètre fonctionnel

## 4.1 Inclus (MVP)

### Module 1 : Authentification ⭐⭐⭐

- Inscription / Connexion
- Gestion profil
- Rôles (USER, LIBRARIAN, ADMIN)

### Module 2 : Catalogue ⭐⭐⭐

- Liste et détail des livres
- Recherche et filtres
- Gestion CRUD (bibliothécaire)

### Module 3 : Emprunt ⭐⭐⭐

- Emprunter un livre
- Mes emprunts
- Enregistrer un retour

### Module 4 : Réservation ⭐⭐

- Réserver un livre
- File d'attente

### Module 5 : Notation ⭐⭐

- Noter (1-5 étoiles)
- Commenter
- Modération

### Module 6 : Dashboards ⭐⭐

- Dashboard lecteur
- Dashboard bibliothécaire
- Dashboard admin

---

# 5. Spécifications fonctionnelles détaillées

Voici une liste non exhaustive des spécifications fonctionnelles.

## 5.1 Authentification

### US-AUTH-01 : Inscription

**En tant que** visiteur
**Je veux** créer un compte
**Afin de** accéder aux services

#### Critères

- Formulaire : email, password, prénom, nom
- Validation email (format, unicité)
- Politique de mot de passe forte
- Hachage BCrypt
- Rôle USER par défaut
- Redirection vers connexion

---

### US-AUTH-02 : Connexion

**En tant qu'** utilisateur inscrit
**Je veux** me connecter
**Afin d'** accéder à mon espace

#### Critères

- Formulaire : email, password
- Génération JWT (24h)
- Stockage token côté client
- Redirection selon rôle

---

### US-AUTH-03 : Profil

**En tant qu'** utilisateur connecté
**Je veux** gérer mon profil
**Afin de** maintenir mes informations

#### Critères

- Affichage informations
- Modification prénom/nom/téléphone
- Changement mot de passe
- Suppression compte (RGPD)

---

## 5.2 Catalogue

### US-BOOK-01 : Consultation

**En tant qu'** utilisateur
**Je veux** consulter les livres
**Afin de** découvrir les ouvrages

#### Critères

- Affichage grille responsive
- Informations : couverture, titre, auteur, disponibilité
- Pagination (20/page)
- Badge disponibilité
- Clic → détail

---

### US-BOOK-02 : Détail livre

**En tant qu'** utilisateur
**Je veux** voir les détails
**Afin de** décider si j'emprunte

#### Critères

- Toutes informations livre
- Note moyenne et commentaires
- Bouton "Emprunter" (si dispo)
- Bouton "Réserver" (si indispo)

---

### US-BOOK-03 : Recherche

**En tant qu'** utilisateur
**Je veux** rechercher
**Afin de** trouver rapidement

#### Critères

- Barre de recherche visible
- Recherche : titre, auteur, ISBN
- Insensible casse
- Filtres : catégorie, disponibilité
- Tri multiples

---

### US-BOOK-04 : Gestion (Bibliothécaire)

**En tant que** bibliothécaire
**Je veux** gérer le catalogue
**Afin d'** ajouter/modifier/supprimer

#### Critères

- Page admin protégée
- CRUD complet
- Validation champs
- Impossibilité supprimer si emprunts

---

## 5.3 Emprunt

### US-LOAN-01 : Emprunter

**En tant que** lecteur
**Je veux** emprunter un livre
**Afin de** le lire

#### Critères

- Bouton "Emprunter"
- **Vérifications :**
  - Livre disponible
  - Max 3 emprunts simultanés
  - Pas de retard
- Durée : 14 jours
- Décrémentation exemplaires
- Confirmation avec date retour

#### Règles de gestion

- **RG-LOAN-01 :** Max 3 emprunts/utilisateur
- **RG-LOAN-02 :** Durée = 14 jours
- **RG-LOAN-03 :** Bloqué si retard
- **RG-LOAN-04 :** Même livre ré-empruntable

---

### US-LOAN-02 : Mes emprunts

**En tant que** lecteur
**Je veux** voir mes emprunts
**Afin de** connaître les dates

#### Critères

- Onglets : "En cours" / "Historique"
- En cours : liste emprunts actifs
- Historique : emprunts passés
- Indication retards

---

### US-LOAN-03 : Retour (Bibliothécaire)

**En tant que** bibliothécaire
**Je veux** enregistrer un retour
**Afin de** libérer l'exemplaire

#### Critères

- Bouton "Retour"
- Calcul retard automatique
- Incrémentation exemplaires
- Statut RETURNED

---

## 5.4 Réservation

### US-RESA-01 : Réserver

**En tant que** lecteur
**Je veux** réserver
**Afin d'** être notifié

#### Critères

- Bouton "Réserver" (si indispo)
- Max 5 réservations
- Affichage rang

---

### US-RESA-02 : Mes réservations

**En tant que** lecteur
**Je veux** voir mes réservations
**Afin de** connaître mon rang

#### Critères

- Liste avec rang
- Statut (Attente/Dispo/Annulée)
- Annulation possible

---

## 5.5 Notation

### US-RATE-01 : Noter

**En tant que** lecteur ayant emprunté
**Je veux** noter
**Afin de** partager mon avis

#### Critères

- Étoiles 1-5
- Note moyenne calculée
- Modification possible

---

### US-RATE-02 : Commenter

**En tant que** lecteur
**Je veux** commenter
**Afin de** donner un avis détaillé

#### Critères

- Zone texte (max 1000 car.)
- Affichage sous fiche livre
- Modification possible
- Modération bibliothécaire

---

## 5.6 Dashboards

### US-DASH-01 : Dashboard lecteur

#### Widgets

- Emprunts en cours
- Retards (alerte)
- Réservations
- Livres lus

---

### US-DASH-02 : Dashboard bibliothécaire

#### Widgets

- Total livres
- Emprunts actifs
- Retards
- Top 10 livres
- Graphique évolution

---

### US-DASH-03 : Dashboard admin

#### Widgets

- Toutes stats bibliothécaire

---

# 6. Spécifications techniques

Les spécifications technologiques ont été établies lors de la rédaction du cahier des charges.

## 6.1 Technologies

### Backend

- Java 17+
- Spring Boot 3.2+
- Spring Security + JWT
- Spring Data JPA
- SQL Server 2019+
- Gradle
- Swagger

### Frontend

- Angular 17+
- TypeScript 5+
- CSS
- HttpClient

---

## 6.2 API REST - Endpoints

### Authentification

- `POST /api/auth/register`
- `POST /api/auth/login`

### Livres

- `GET /api/books`
- `GET /api/books/{id}`
- `GET /api/books/search`
- `POST /api/books` (LIBRARIAN)
- `PUT /api/books/{id}` (LIBRARIAN)
- `DELETE /api/books/{id}` (ADMIN)

### Emprunts

- `POST /api/loans`
- `GET /api/loans/my`
- `GET /api/loans` (LIBRARIAN)
- `PUT /api/loans/{id}/return` (LIBRARIAN)

### Réservations

- `POST /api/reservations`
- `GET /api/reservations/my`
- `DELETE /api/reservations/{id}`

### Notations

- `POST /api/books/{id}/ratings`
- `PUT /api/ratings/{id}`
- `DELETE /api/ratings/{id}` (LIBRARIAN)

### Codes HTTP

- **Succès :** 200 OK, 201 Created, 204 No Content
- **Erreurs client :** 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found

---

# 7. Spécifications de sécurité

Voici les spécifications de sécurité établies afin de protéger les utilisateurs.

## 7.1 JWT

- **Algorithme :** HS256
- **Secret :** 256 bits minimum
- **Expiration :** 24h
- **Payload :** email, role

## 7.2 Mots de passe

- **Hachage :** BCrypt (coût 12)
- **Politique :** 12+ caractères, 1 maj, 1 min, 1 chiffre, 1 spécial

## 7.3 Protection OWASP

### Injection SQL

- Requêtes paramétrées (JPA)
- Spring Data exclusivement

### XSS

- Sanitization Angular
- Échappement templates

### CSRF

- Tokens CSRF activés
- SameSite cookies

### Validation

- **Client :** Validators Angular
- **Serveur :** @Valid, Jakarta Validation

## 7.4 RGPD

- Minimisation données
- Consentement obligatoire
- Droit accès (profil)
- Droit rectification
- Droit à l'oubli (suppression compte)

---

# 8. Contraintes et exigences

Quelques contraintes de performance, compatibilité et qualité sont à respecter.

## 8.1 Performance

- Chargement < 2s
- API < 500ms
- Recherche < 1s

## 8.2 Compatibilité

- Chrome/Firefox/Edge (2 dernières versions)
- Responsive (mobile/tablette/desktop)

## 8.3 Qualité

- Code commenté
- Conventions respectées
- Tests ≥ 20% (backend), ≥ 20% (frontend)

---

# 9. Livrables et planning

Les livrables doivent respecter le format suivant.

## 9.1 Livrables

### Documentation conception (J2)

- User stories
- Diagrammes UML
- Maquettes
- MCD/MLD/MPD
- Architecture

### Code source (J9)

- Backend Spring Boot
- Frontend Angular
- Scripts SQL
- README

### Présentation (J10)

- Swagger
- Tests unitaires

- Diaporama
- Démo
- Tous membres interviennent

---

## 9.2 Planning

Voici un planning indicatif.

| Phase | Jours | Focus |
|-------|-------|-------|
| Sprint 0 | J1-J2 | Conception |
| Sprint 1 | J3-J5 | MVP (auth, catalogue, emprunts) |
| Sprint 2 | J6-J9 | Complétion (résa, notation, tests) |
| Clôture | J10 | Documentation, présentation |

---

**Document rédigé par :** [Formateur]
**Date :** [Date]
**Version :** 1.0

# 🚀 Bon courage !
