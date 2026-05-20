# BookHub — Application de Gestion de Bibliothèque

Projet de fin de formation ENI. Application web complète permettant la gestion d'une bibliothèque : emprunts, réservations, avis, et administration des utilisateurs.

---

## Stack technique

| Côté | Technologie |
|------|-------------|
| Backend | Java 17 · Spring Boot 3.4.5 · Spring Security 6 (JWT) · Hibernate 6 · SQL Server |
| Frontend | Angular 18 · TypeScript · Signals |
| Build | Gradle (backend) · npm / Angular CLI (frontend) |

---

## Comptes de démonstration

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Administrateur | `admin@bookhub.fr` | `Admin@123456!` |
| Bibliothécaire | `biblio@bookhub.fr` | `Admin@123456!` |
| Lecteur (2 retards) | `retardataire@bookhub.fr` | `Admin@123456!` |
| Lecteur (bloqué) | `bloque@bookhub.fr` | `Admin@123456!` |

---

## Lancer le projet

### Prérequis
- Java 17+
- Node.js 18+
- SQL Server (local ou Docker)

### Backend
```bash
cd backend
./gradlew bootRun
```
Disponible sur **http://localhost:8080**

### Frontend
```bash
cd FrontEnd
npm install
ng serve
```
Disponible sur **http://localhost:4200**

### Base de données
1. Créer la base `BOOKHUB` dans SQL Server
2. Configurer `backend/src/main/resources/application.properties` :
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=BOOKHUB;...
spring.datasource.username=SA
spring.datasource.password=VotreMotDePasse
```
3. La structure est créée automatiquement au démarrage (Hibernate `ddl-auto=update`)
4. Les données de démo sont injectées automatiquement par le `DataLoader`

---

## Fonctionnalités

### Lecteur
- Catalogue et recherche de livres
- Emprunt (max 3 simultanés, 14 jours)
- Réservation si le livre est indisponible (file d'attente)
- Suivi des emprunts et réservations depuis son espace
- Dépôt d'avis après retour d'un livre
- Gestion du profil (nom, téléphone, mot de passe, suppression de compte)

### Bibliothécaire
- Validation / refus des demandes d'emprunt
- Validation des retours physiques
- Remise du livre à un lecteur dont la réservation est notifiée
- Ajout et modification de livres (avec upload de couverture)
- Modération des avis (approbation / rejet)
- Consultation des retards et blocages

### Administrateur
- Toutes les actions bibliothécaire
- Gestion complète des utilisateurs (création, suppression, déblocage)
- Création et promotion de bibliothécaires
- Tableau de bord avec statistiques globales et graphique d'évolution

---

## Règles métier

| Règle | Détail |
|-------|--------|
| Limite d'emprunts | 3 emprunts actifs simultanés par lecteur |
| Durée d'emprunt | 14 jours calendaires |
| Retards | 3 retards → compte suspendu 5 jours automatiquement |
| Déblocage | L'admin peut débloquer un compte à tout moment |
| Réservations | Uniquement si le livre est indisponible · file d'attente ordonnée |
| Avis | Possible uniquement après avoir rendu un livre |
| Avis | Soumis à modération avant publication |

---

## Structure du projet

```
bookhub-eni/
├── backend/
│   └── src/main/java/com/example/backend/
│       ├── config/          # Sécurité JWT, CORS, DataLoader
│       ├── controller/      # Endpoints REST
│       ├── dto/             # Objets de transfert de données
│       ├── exception/       # Gestion centralisée des erreurs
│       ├── model/entity/    # Entités JPA
│       ├── repository/      # Couche accès données (Spring Data)
│       └── services/        # Logique métier
└── FrontEnd/
    └── src/app/
        ├── components/      # Composants partagés (navbar)
        ├── models/          # Interfaces TypeScript
        ├── pages/           # Pages de l'application
        └── services/        # Services HTTP (API)
```

---

## API — Principaux endpoints

| Méthode | URL | Rôle requis |
|---------|-----|-------------|
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/register` | Public |
| GET | `/api/livres` | Public |
| POST | `/api/livres` | Bibliothécaire / Admin |
| GET | `/api/emprunts/demandes` | Bibliothécaire / Admin |
| PUT | `/api/emprunts/{id}/valider` | Bibliothécaire / Admin |
| PUT | `/api/emprunts/{id}/valider-retour` | Bibliothécaire / Admin |
| GET | `/api/utilisateurs/retardataires` | Bibliothécaire / Admin |
| PUT | `/api/utilisateurs/{id}/debloquer` | Admin |
| GET | `/api/utilisateurs` | Admin |

Documentation complète disponible sur **http://localhost:8080/swagger-ui/index.html**
