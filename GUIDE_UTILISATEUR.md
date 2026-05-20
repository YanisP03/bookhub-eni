# Guide Utilisateur — BookHub

## Table des matières
1. [Présentation](#1-présentation)
2. [Accès à l'application](#2-accès-à-lapplication)
3. [Lecteur — Espace membre](#3-lecteur--espace-membre)
4. [Bibliothécaire — Tableau de bord](#4-bibliothécaire--tableau-de-bord)
5. [Administrateur — Gestion complète](#5-administrateur--gestion-complète)
6. [Questions fréquentes](#6-questions-fréquentes)

---

## 1. Présentation

**BookHub** est une application web de gestion de bibliothèque. Elle permet aux membres d'emprunter et de réserver des livres en ligne, et au personnel (bibliothécaires, administrateur) de gérer les opérations quotidiennes depuis un tableau de bord centralisé.

---

## 2. Accès à l'application

### Se connecter
1. Ouvrir `http://localhost:4200` dans votre navigateur
2. Cliquer sur **Connexion** dans la barre de navigation
3. Saisir votre **email** et votre **mot de passe**
4. Cliquer sur **Se connecter**

### Créer un compte lecteur
1. Cliquer sur **S'inscrire**
2. Remplir le formulaire (nom, prénom, email, mot de passe)
3. Le mot de passe doit contenir **au minimum 12 caractères**, une majuscule, un chiffre et un caractère spécial (`@$!%*?&`)
4. Valider l'inscription

### Se déconnecter
Cliquer sur **Déconnexion** dans la barre de navigation.

---

## 3. Lecteur — Espace membre

### 3.1 Consulter le catalogue

- Cliquer sur **Catalogue** dans la barre de navigation
- Utiliser la barre de recherche pour trouver un livre par titre ou auteur
- Filtrer par catégorie ou disponibilité

Chaque fiche livre affiche :
- Titre, auteur, catégorie
- Nombre d'exemplaires disponibles
- Note moyenne des avis
- Statut : `DISPONIBLE` ou `EMPRUNTÉ`

### 3.2 Emprunter un livre

1. Cliquer sur un livre dans le catalogue
2. Si des exemplaires sont disponibles, cliquer sur **Emprunter**
3. La demande est créée en statut **EN ATTENTE** — un bibliothécaire doit la valider
4. Une fois validée, l'emprunt passe en **EN COURS** pour **14 jours**

> **Limite :** maximum **3 emprunts actifs** simultanément.

### 3.3 Réserver un livre indisponible

1. Sur la fiche d'un livre dont tous les exemplaires sont empruntés, cliquer sur **Réserver**
2. Votre place en **file d'attente** est enregistrée
3. Quand un exemplaire est rendu, vous recevez une notification (statut `NOTIFIÉE`)
4. Vous avez alors **5 jours** pour venir récupérer le livre en bibliothèque

### 3.4 Rendre un livre

1. Aller dans **Mes emprunts** (menu **Mon espace**)
2. Cliquer sur **Rendre** à côté de l'emprunt concerné
3. La demande de retour est transmise au bibliothécaire pour confirmation physique

### 3.5 Suivre ses emprunts et réservations

Depuis **Mon espace → Mes emprunts** :
- Onglet **En cours** : emprunts actifs avec date de retour prévue
- Onglet **Historique** : tous les emprunts passés
- Onglet **Réservations** : statut de la file d'attente avec possibilité d'annuler

### 3.6 Laisser un avis

1. Aller dans **Mon espace → Mes avis**
2. Cliquer sur **Laisser un avis** pour un livre déjà rendu
3. Attribuer une note (1 à 5 étoiles) et rédiger un commentaire
4. L'avis est soumis à modération avant publication

> L'avis n'est possible **qu'après avoir rendu le livre**.

### 3.7 Gérer son profil

Depuis **Mon espace → Mon profil** :
- Modifier nom, prénom, téléphone
- Changer le mot de passe (saisir l'ancien mot de passe pour confirmer)
- Supprimer son compte (irréversible, saisir le mot de passe pour confirmer)

### 3.8 Règles sur les retards

| Situation | Conséquence |
|-----------|-------------|
| 1 ou 2 retours en retard | Avertissement, compteur de retards visible |
| 3 retours en retard | Compte **suspendu 5 jours** automatiquement |
| Compte suspendu | Impossible d'emprunter ou de réserver |
| Après 5 jours | Suspension levée automatiquement |

---

## 4. Bibliothécaire — Tableau de bord

Accéder au tableau de bord via **Dashboard** dans la barre de navigation.

### 4.1 Gérer les demandes d'emprunt

La section **Demandes d'emprunt** liste les demandes en attente :
- **Valider** : confirme l'emprunt (le livre est remis au lecteur)
- **Refuser** : annule la demande et restitue l'exemplaire

### 4.2 Confirmer les retours

La section **Retours à valider** liste les livres rapportés physiquement :
- Cliquer **Confirmer retour** après avoir réceptionné le livre
- Si le retour est en retard, les jours de retard sont calculés automatiquement
- Si une réservation est en attente pour ce livre, elle passe en statut **NOTIFIÉE**

### 4.3 Remettre un livre à un lecteur notifié

La section **Réservations à convertir** affiche les lecteurs dont la réservation est passée en statut NOTIFIÉE (livre disponible pour eux) :
- Cliquer **Remettre le livre** quand le lecteur se présente physiquement
- Un emprunt est créé directement en statut EN COURS

### 4.4 Modérer les avis

La section **Modération des avis** liste les avis soumis par les lecteurs :
- **Approuver** : l'avis devient visible sur la fiche du livre
- **Rejeter** : l'avis n'est pas publié

### 4.5 Suivre les retards

La section **Retards & blocages** liste les lecteurs avec des retards ou des comptes suspendus :
- Affiche le nombre de retards cumulés
- Affiche la date de fin de suspension pour les comptes bloqués

### 4.6 Consulter la file d'attente

La section **File d'attente** affiche toutes les réservations en attente, ordonnées par position.

### 4.7 Ajouter un livre

Cliquer sur **+ Ajouter un livre** en haut du tableau de bord ou dans la barre de navigation.

Remplir le formulaire :
- Titre, auteur, ISBN
- Catégorie, description
- Nombre d'exemplaires
- Image de couverture (upload optionnel, max 5 Mo)

---

## 5. Administrateur — Gestion complète

L'administrateur dispose de toutes les fonctionnalités du bibliothécaire, plus les sections suivantes dans le tableau de bord.

### 5.1 Gestion des utilisateurs

La section **Gestion des utilisateurs** liste tous les comptes :
- Voir le nom, l'email et le rôle de chaque utilisateur
- **Supprimer** un compte (supprime aussi tous les emprunts, réservations et avis associés)
- **Ajouter un utilisateur** : créer un compte lecteur ou bibliothécaire directement

### 5.2 Gestion des bibliothécaires

La section **Gestion bibliothécaires** permet :
- **Promouvoir** un lecteur existant en bibliothécaire (saisir son email)
- **Créer** un nouveau compte bibliothécaire directement

### 5.3 Débloquer un compte

Dans la section **Retards & blocages**, l'administrateur peut cliquer **Débloquer** sur un compte suspendu pour lever la suspension immédiatement (remet aussi le compteur de retards à zéro).

---

## 6. Questions fréquentes

**Je ne peux pas emprunter un livre disponible.**
→ Vérifiez que vous n'avez pas déjà 3 emprunts actifs. Vérifiez aussi que votre compte n'est pas suspendu (3 retards cumulés).

**Ma réservation est passée en « NOTIFIÉE » mais je n'ai pas pu venir.**
→ Contactez un bibliothécaire. Si vous ne vous présentez pas, la réservation peut être annulée.

**Mon avis n'est pas visible sur le livre.**
→ Les avis sont soumis à modération par un bibliothécaire avant publication. Ce processus peut prendre quelques heures.

**Je veux changer mon email.**
→ La modification de l'email n'est pas disponible en libre-service. Contactez un administrateur.

**J'ai oublié mon mot de passe.**
→ Contactez un administrateur pour réinitialiser votre compte.
