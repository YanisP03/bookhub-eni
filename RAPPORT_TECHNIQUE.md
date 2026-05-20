# Rapport Technique — BookHub

## Table des matières
1. [Présentation du projet](#1-présentation-du-projet)
2. [Architecture générale](#2-architecture-générale)
3. [Stack technique](#3-stack-technique)
4. [Backend — Spring Boot](#4-backend--spring-boot)
5. [Frontend — Angular](#5-frontend--angular)
6. [Base de données](#6-base-de-données)
7. [Sécurité](#7-sécurité)
8. [Fonctionnalités implémentées](#8-fonctionnalités-implémentées)
9. [Difficultés rencontrées](#9-difficultés-rencontrées)
10. [Pistes d'amélioration](#10-pistes-damélioration)

---

## 1. Présentation du projet

**BookHub** est une application web full-stack de gestion de bibliothèque développée dans le cadre du projet de fin de formation ENI. L'application répond aux besoins suivants :

- Permettre aux **lecteurs** de consulter le catalogue, emprunter et réserver des livres, déposer des avis
- Permettre aux **bibliothécaires** de gérer les emprunts, retours, réservations et modérer les avis
- Permettre à l'**administrateur** de gérer les utilisateurs, bibliothécaires et surveiller l'activité

Le projet suit une architecture **client/serveur** avec une API REST découplée du frontend.

---

## 2. Architecture générale

```
┌─────────────────────────────────┐
│        Navigateur web           │
│     Angular 18 (port 4200)      │
│   Signals · TypeScript · CSS    │
└────────────┬────────────────────┘
             │ HTTP/REST + JWT
             ▼
┌─────────────────────────────────┐
│      Spring Boot 3.4.5          │
│         (port 8080)             │
│                                 │
│  ┌─────────────────────────┐    │
│  │   Spring Security 6     │    │
│  │  JWT · CORS · STATELESS │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │  Controllers REST API   │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │       Services          │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │  Repositories (JPA)     │    │
│  └─────────────────────────┘    │
└────────────┬────────────────────┘
             │ JDBC (SQL Server)
             ▼
┌─────────────────────────────────┐
│    Microsoft SQL Server         │
│       Base : BOOKHUB            │
└─────────────────────────────────┘
```

**Pattern adopté :** architecture en couches (Controller → Service → Repository → Entity)

---

## 3. Stack technique

### Backend
| Technologie | Version | Rôle |
|------------|---------|------|
| Java | 17 | Langage de programmation |
| Spring Boot | 3.4.5 | Framework applicatif |
| Spring Security | 6.x | Authentification et autorisation |
| Spring Data JPA | 3.4.5 | Accès aux données ORM |
| Hibernate | 6.6.x | Implémentation JPA |
| JJWT | 0.11.5 | Génération et validation des tokens JWT |
| springdoc-openapi | 2.5.0 | Documentation API (Swagger UI) |
| Microsoft SQL Server JDBC | 12.x | Driver base de données |
| Gradle | 8.x | Outil de build |

### Frontend
| Technologie | Version | Rôle |
|------------|---------|------|
| Angular | 18 | Framework SPA |
| TypeScript | 5.x | Langage typé |
| Angular Signals | 18 | Gestion d'état réactive |
| Angular Forms | 18 | Formulaires (template-driven) |
| Angular HTTP Client | 18 | Appels REST |
| Angular Router | 18 | Navigation SPA |

### Infrastructure
| Technologie | Rôle |
|------------|------|
| Microsoft SQL Server | Base de données relationnelle |
| Git / GitHub | Versionnement du code |

---

## 4. Backend — Spring Boot

### 4.1 Structure des packages

```
com.example.backend/
├── config/          # Configuration transversale
│   ├── CorsConfig       # Sécurité Spring Security + CORS
│   ├── JwtAuthFilter    # Filtre JWT (OncePerRequestFilter)
│   ├── JwtUtils         # Utilitaire de génération/validation JWT
│   ├── WebMvcConfig     # Ressources statiques (uploads)
│   ├── OpenApiConfig    # Configuration Swagger UI
│   └── DataLoader       # Injection des données de démo
├── controller/      # Endpoints REST (couche présentation)
├── dto/             # Objets de transfert de données
├── exception/       # Gestion centralisée des erreurs
├── model/entity/    # Entités JPA (couche données)
├── repository/      # Interfaces Spring Data JPA
└── services/        # Logique métier (couche service)
```

### 4.2 Entités principales

| Entité | Description | Relations |
|--------|-------------|-----------|
| `Utilisateur` | Compte utilisateur | → Role, ← Emprunt, Reservation, Avis |
| `Livre` | Livre du catalogue | → Categorie, Statut |
| `Emprunt` | Demande/emprunt en cours | → Utilisateur, Livre, Statut |
| `Reservation` | File d'attente | → Utilisateur, Livre, Statut |
| `Avis` | Commentaire sur un livre | → Utilisateur, Livre |
| `Retour` | Enregistrement physique du retour | → Utilisateur, Livre, Statut |
| `Statut` | État d'un emprunt/réservation | — |
| `Role` | Rôle utilisateur | — |

### 4.3 Statuts des emprunts

```
[Lecteur]  POST /api/emprunts/{livreId}
               ↓
           DEMANDE ──(refus bibliothécaire)──→ ANNULE
               ↓ (validation bibliothécaire)
           EN_COURS ──(signalement retour)──→ RETOUR_DEMANDE
                                                    ↓ (confirmation bibliothécaire)
                                                  RENDU
```

### 4.4 Statuts des réservations

```
EN_ATTENTE → NOTIFIEE → CONVERTIE (quand le livre est récupéré)
           → ANNULEE  (si le lecteur annule)
```

### 4.5 Services métier

| Service | Responsabilités principales |
|---------|----------------------------|
| `EmpruntService` | Demande, validation, refus, retour, conversion réservation |
| `ReservationService` | Création, annulation, file d'attente, notifications |
| `LivreService` | CRUD catalogue, recherche, populaires, mieux notés |
| `AvisService` | Soumission, modération, note moyenne |
| `UtilisateurService` | Profil, gestion admin, retards, blocages |
| `DashboardService` | Statistiques globales, évolution mensuelle |
| `RetourService` | Workflow alternatif de retour physique |
| `AuthService` | Inscription |
| `CustomUserDetailsService` | Chargement utilisateur pour Spring Security |

---

## 5. Frontend — Angular

### 5.1 Structure des pages

| Page | Route | Rôle |
|------|-------|------|
| Accueil | `/` | Présentation, livres populaires, mieux notés |
| Catalogue | `/catalogue` | Recherche et navigation dans les livres |
| Détail livre | `/catalogue/:id` | Fiche complète, avis, emprunt/réservation |
| Connexion | `/connexion` | Formulaire de login |
| Inscription | `/inscription` | Formulaire de création de compte |
| Mon espace | `/mon-espace` | Emprunts, réservations, avis, profil |
| Profil | `/profil` | Modification du profil |
| Dashboard | `/admin/dashboard` | Tableau de bord staff |
| Ajout/Modif livre | `/livres/nouveau`, `/livres/:id/modifier` | Formulaire livre |

### 5.2 Services Angular

| Service | Endpoints couverts |
|---------|--------------------|
| `AuthService` | login, register, logout (localStorage) |
| `BookService` | CRUD livres, recherche |
| `EmpruntService` | Emprunts, retours, réservations |
| `ProfilService` | Profil, dashboard stats, gestion admin |
| `AvisService` | Avis, modération |

### 5.3 Sécurité côté frontend

- **Guard de route** : `authGuard` vérifie le token JWT avant d'accéder aux pages protégées
- **HTTP Interceptor** : `AuthInterceptor` injecte automatiquement le token `Bearer` dans chaque requête sortante
- **Stockage** : token JWT et rôle stockés dans `localStorage`
- **Rôles** : `isLoggedIn()`, `isAdmin()`, `isBibliothecaire()`, `isStaff()` via Angular Signals

---

## 6. Base de données

### 6.1 Modèle de données simplifié

```
UTILISATEUR ←── EMPRUNT ──→ LIVRE ──→ CATEGORIE
     │               └──→ STATUT
     ├── RESERVATION ──→ LIVRE
     │         └──→ STATUT
     ├── AVIS ──→ LIVRE
     └── RETOUR ──→ LIVRE
                      └──→ STATUT
```

### 6.2 Stratégie de migration

- `spring.jpa.hibernate.ddl-auto=update` : Hibernate crée/modifie les tables automatiquement au démarrage
- Le `DataLoader` injecte les données de référence (rôles, statuts, livres de démo, comptes de test) à chaque démarrage si absentes
- Les procédures stockées (`sp_SaveLivre`, `sp_SaveAvis`) encapsulent une logique de validation complexe côté SQL

### 6.3 Colonnes notables

| Colonne | Table | Type | Particularité |
|---------|-------|------|---------------|
| `dateInscription` | UTILISATEUR | datetime2 | Valeur par défaut = `GETDATE()` |
| `bloque` | UTILISATEUR | bit | Gestion des suspensions (3 retards) |
| `dateBlocageAuto` | UTILISATEUR | datetime2 | Date d'expiration automatique de la suspension |
| `nbRetards` | UTILISATEUR | int | Compteur de retards (reset à 0 après suspension) |
| `noteMoyenne` | LIVRE | decimal(3,2) | Recalculée à chaque modération d'avis |

---

## 7. Sécurité

### 7.1 Authentification JWT

1. Le client envoie ses credentials (`POST /api/auth/login`)
2. Le backend valide via `DaoAuthenticationProvider` (BCrypt + UserDetailsService)
3. Un token JWT signé (algorithme HS256) est retourné avec une durée de validité
4. Le client stocke le token en `localStorage` et l'envoie via le header `Authorization: Bearer <token>`
5. Le `JwtAuthFilter` (s'exécute à chaque requête) valide le token et charge les authorities

### 7.2 Matrice des autorisations

| Ressource | Public | Lecteur | Bibliothécaire | Admin |
|-----------|--------|---------|----------------|-------|
| Catalogue (lecture) | ✅ | ✅ | ✅ | ✅ |
| Emprunter/Réserver | ❌ | ✅ | ✅ | ✅ |
| Valider emprunts | ❌ | ❌ | ✅ | ✅ |
| Ajouter/Modifier livre | ❌ | ❌ | ✅ | ✅ |
| Modérer avis | ❌ | ❌ | ✅ | ✅ |
| Voir retardataires | ❌ | ❌ | ✅ | ✅ |
| Débloquer un compte | ❌ | ❌ | ❌ | ✅ |
| Gérer utilisateurs | ❌ | ❌ | ❌ | ✅ |
| Créer bibliothécaire | ❌ | ❌ | ❌ | ✅ |

### 7.3 Protection CSRF et CORS

- **CSRF** : désactivé (API stateless, authentification par token)
- **CORS** : configuré pour autoriser uniquement `http://localhost:4200` (frontend Angular) pour les appels API
- **Sessions** : `SessionCreationPolicy.STATELESS` — aucune session côté serveur

---

## 8. Fonctionnalités implémentées

| # | Fonctionnalité | Backend | Frontend |
|---|---------------|---------|----------|
| 1 | Inscription / Connexion JWT | ✅ | ✅ |
| 2 | Catalogue avec recherche et filtres | ✅ | ✅ |
| 3 | Emprunt (demande → validation → retour) | ✅ | ✅ |
| 4 | File d'attente de réservations | ✅ | ✅ |
| 5 | Gestion des retards et suspensions | ✅ | ✅ |
| 6 | Avis avec modération | ✅ | ✅ |
| 7 | Tableau de bord avec statistiques | ✅ | ✅ |
| 8 | Graphique d'évolution mensuelle | ✅ | ✅ |
| 9 | Upload de couvertures de livres | ✅ | ✅ |
| 10 | Gestion des utilisateurs (admin) | ✅ | ✅ |
| 11 | Gestion des bibliothécaires (admin) | ✅ | ✅ |
| 12 | Documentation API (Swagger UI) | ✅ | — |
| 13 | Tests unitaires (services) | ✅ | — |

---

## 9. Difficultés rencontrées

### Compatibilité Spring Boot 4.x → 3.4.5
Le projet a initialement été créé avec Spring Boot 4.0.6 (Spring Framework 7.x). La bibliothèque `springdoc-openapi 2.5.0` n'étant pas compatible avec Spring 7, un retour à Spring Boot 3.4.5 a été nécessaire. Cela a entraîné des adaptations dans la configuration de `DaoAuthenticationProvider` (suppression du constructeur avec `UserDetailsService` qui n'existe qu'en Spring Security 7).

### Gestion des types SQL Server
SQL Server utilise `datetime2` pour les colonnes de date/heure. Hibernate 6 mappe `LocalDate` vers le type SQL `date`, ce qui a provoqué des tentatives d'ALTER TABLE bloquées par des contraintes DEFAULT. La solution a été de changer le type Java vers `LocalDateTime` pour correspondre au `datetime2` de la base.

### Valeurs NULL dans les colonnes booléennes
Les colonnes `bloque` (BIT) et `nbRetards` (INT) ajoutées après la création des comptes de test avaient des valeurs NULL pour les enregistrements existants. Java ne peut pas mapper NULL vers un type primitif (`boolean`, `int`), provoquant des `NullPointerException` silencieuses dans le filtre JWT. La solution a consisté à utiliser les types boxés (`Boolean`, `Integer`) avec des getters null-safe.

### Architecture CORS et Swagger
Le filtre CORS restreignait les origines à `localhost:4200`. Les requêtes JavaScript de Swagger UI (servi sur `localhost:8080`) incluant un en-tête `Origin: http://localhost:8080` étaient refusées. L'ajout de `localhost:8080` aux origines autorisées a résolu le problème.

---

## 10. Pistes d'amélioration

| Amélioration | Priorité | Description |
|-------------|---------|-------------|
| Notifications par email | Haute | Informer le lecteur quand sa réservation est notifiée |
| Pagination du catalogue | Haute | Optimisation pour un grand nombre de livres |
| Refresh token JWT | Moyenne | Éviter la déconnexion automatique sans action utilisateur |
| Variables d'environnement | Moyenne | Externaliser URL BDD, secrets JWT en variables d'env |
| Tests d'intégration | Moyenne | Tests REST avec MockMvc pour chaque endpoint |
| Rôle MODERATEUR | Basse | Séparer la modération des avis de la gestion des emprunts |
| Application mobile | Basse | API déjà prête pour un client mobile (React Native, Flutter) |
