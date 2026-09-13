# 📋 CAHIER DES CHARGES FONCTIONNEL & TECHNIQUE
## PORTAIL WEB D'ADMINISTRATION & DE GESTION HÔTELIÈRE PANAFRICAINE • BookZZZ

---

## 🎨 CHARTE GRAPHIQUE & PALETTE DE COULEURS OFFICIELLE

Pour garantir une harmonie visuelle parfaite entre l'application mobile Android native et le portail Web d'administration, voici la palette de couleurs officielle **BookZZZ Hospitality** (valeurs Hexadécimales exactes à utiliser dans Tailwind CSS / CSS / Figma) :

### 1. Couleurs d'Identité de Marque (Brand Identity)
- **Bleu Signature (Azure Blue) :** `#5DADE2` *(Couleur d'action principale, boutons primaires, liens interactifs, badges actifs)*
- **Or Hôtelier Luxueux (Brand Gold) :** `#CCA865` *(Accents de luxe, étoiles de notation, badges VIP / SuperAdmin, bordures dorées)*
- **Or Clair (Brand Gold Light) :** `#D4B26F` *(Survols, reflets, arrière-plans de mise en avant)*
- **Caramel Chaud (Brand Caramel) :** `#8D7159` *(Accents secondaires chaleureux, cuirs & boiseries)*
- **Taupe Bronze (Brand Taupe) :** `#5A5845` *(Bordures élégantes, séparateurs subtils)*
- **Espresso Profond (Brand Espresso) :** `#4F3F31` *(Surfaces sombres raffinées)*
- **Bleu Nuit Marine (Brand Navy) :** `#1F3A5F` *(En-têtes, navigation principale, contrastes élevés)*

### 2. Couleurs de Statut & Paiements Panafricains
- **Vert Succès / Validé :** `#27AE60` *(Réservations confirmées, preuves de paiement validées, badge temps réel)*
- **Rouge Alerte / Rejeté :** `#E74C3C` *(Réservations annulées, paiements non conformes)*
- **Orange / Ambre En Attente :** `#F39C12` *(Preuves de paiement Mobile Money en attente de vérification)*
- **Rouge Opérateur M-Pesa :** `#E60000` *(Identifiant visuel Vodacom M-Pesa)*
- **Rouge Opérateur Airtel Money :** `#ED1C24` *(Identifiant visuel Airtel Money)*
- **Orange Opérateur Orange Money :** `#FF7900` *(Identifiant visuel Orange Money)*
- **Bleu Ciel Opérateur Wave :** `#1DC3F0` *(Identifiant visuel Wave Mobile)*

### 3. Fonds & Typographie (Light & Dark Modes)
- **Thème Clair (Default Web Admin) :**
  - Fond de page : `#F8F7F4` *(Blanc cassé chaleureux / Warm Cream)*
  - Cartes & Conteneurs : `#FFFFFF` *(Blanc pur avec ombre subtile)*
  - Texte Principal : `#221C2B` *(Gris nuit profond haute lisibilité)*
  - Texte Secondaire : `#5A5845` ou `#64748B` *(Gris neutre adouci)*
  - Bordures : `#D5CEBF` ou `#E2E8F0` *(Bordure discrète)*
- **Thème Sombre (Optionnel / Mode Nuit) :**
  - Fond de page sombre : `#1D1726` *(Aubergine nuit luxe)*
  - Cartes sombres : `#2A2234`
  - Texte clair : `#FFFFFF` / `#CCCCCC`
  - Bordures sombres : `#5A5845`

### 4. Typographie Recommandée
- **Police Principale :** **`Plus Jakarta Sans`** ou **`Inter`** (Google Fonts).

---

## 📌 1. CONTEXTE ET OBJECTIF DU PROJET

### 1.1 Contexte
**BookZZZ** est une plateforme panafricaine de réservation hôtelière reliant les voyageurs aux établissements d'hébergement (hôtels, lodges, resorts, appart-hôtels) à travers la RDC, le Rwanda, la Côte d'Ivoire, le Sénégal, le Kenya et l'Afrique entière.

### 1.2 Objectif du Portail Web d'Administration
Ce portail Web indépendant permet :
1. **Aux Établissements Hôteliers Partenaires (Managers & Réceptionnistes)** de gérer leur hôtel en toute autonomie : catalogue de chambres, disponibilités, suivi des arrivées/départs, validation des paiements Mobile Money et comptabilité.
2. **À l'Équipe Centrale BookZZZ (SuperAdmin)** de piloter l'ensemble de la plateforme : onboarding des nouveaux hôtels, fixation des taux de commission, validation des transactions litigieuses et suivi du chiffre d'affaires global.

---

## 👥 2. PROFILS UTILISATEURS & GESTION DES RÔLES (RBAC)

Le portail Web doit implémenter un contrôle d'accès strict basé sur les rôles :

| Rôle | Périmètre d'accès | Droits principaux |
| :--- | :--- | :--- |
| **👑 SuperAdmin (BookZZZ HQ)** | Tous les pays & tous les hôtels | Créer/valider des hôtels partenaires, modifier les commissions, voir les stats globales, bannir/valider des comptes. |
| **🏨 Hotel Manager (Directeur d'Hôtel)** | Uniquement son établissement | Gérer les chambres/prix, valider les paiements, exporter les rapports financiers de son hôtel, gérer son équipe de réception. |
| **🛎️ Receptionist / Front-Desk** | Uniquement son établissement | Consulter le planning des arrivées (Check-in/Check-out), marquer une chambre occupée, vérifier les bordereaux Mobile Money. |
| **📊 Financial Auditor (Comptable)** | Vue financière dédiée | Consulter et exporter les flux de trésorerie, factures de commission et justificatifs fiscaux. |

---

## ⚙️ 3. SPÉCIFICATIONS FONCTIONNELLES DÉTAILLÉES

### 🔐 MODULE 1 : Authentification & Sécurité
- **Méthodes de connexion supportées :**
  - Connexion par Email professionnel + Mot de passe fort (avec réinitialisation sécurisée par lien).
  - Connexion rapide via **Google Workspace (Google Sign-In / OAuth2)**.
  - Authentification à deux facteurs (2FA) par SMS/Email pour les actions sensibles.
- **Session & Déconnexion automatique :** Expiration de token après 60 minutes d'inactivité avec renouvellement par Refresh Token (Firebase Auth ou JWT).

---

### 📊 MODULE 2 : Tableau de Bord Exécutif (Dashboard)
Chaque utilisateur accède à un tableau de bord personnalisé selon son rôle :
- **KPIs en Temps Réel :**
  - Chiffre d'affaires encaissé ($ USD, CDF, XOF, RWF, KES).
  - Nombre total de réservations (Confirmées, En attente, Annulées).
  - Taux d'occupation moyen (%) journalier, hebdomadaire et mensuel.
  - Nombre de preuves de paiement en attente de vérification.
- **Visualisations Graphiques (Chart.js ou Recharts) :**
  - Courbe d'évolution des réservations sur les 12 derniers mois (Couleur `#5DADE2` et `#CCA865`).
  - Diagramme circulaire de répartition des paiements par opérateur (**M-Pesa, Airtel Money, Orange Money, Wave, MTN MoMo, Carte**).
- **Flux d'Activités en Direct :** Notifications en temps réel lors d'une nouvelle réservation ou de la soumission d'une preuve de paiement.

---

### 🛏️ MODULE 3 : Gestion du Catalogue des Chambres & Disponibilités
- **Gestion des Types d'Hébergement :**
  - Création / Modification / Suppression de chambres (Standard, Deluxe, Suite Junior, Suite Exécutive, Villa Présidentielle).
  - Définition du prix par nuitée avec support multi-devises (USD par défaut + devise locale).
  - Capacité d'accueil (nombre d'adultes, nombre d'enfants).
  - Galerie photos HD avec recadrage automatique et hébergement Cloud (Cloudinary ou Firebase Storage).
  - Liste des équipements cochables : Climatisation, Vue Lac/Fleuve/Jardin, WiFi très haut débit, Balcon, Petit-déjeuner inclus, Coffre-fort, Mini-bar, Jacuzzi.
- **Contrôle d'Inventaire en Temps Réel :**
  - Basculeur instantané d'état : `Disponible` (`#27AE60`) / `Occupée` (`#E74C3C`) / `En Nettoyage` (`#F39C12`) / `Hors Service`.
  - Calendrier interactif des disponibilités avec blocage manuel de dates.

---

### 📅 MODULE 4 : Gestion des Réservations (Booking Engine)
- **Tableau Central des Réservations :**
  - Recherche multi-critères : Par nom du client, numéro de téléphone, référence de réservation (ex: `BKZ-9042`), dates de séjour ou hôtel.
  - Filtres par statut : `TOUTES`, `EN ATTENTE DE PAIEMENT`, `CONFIRMÉE`, `CHECK-IN EN COURS`, `COMPLÉTÉE`, `ANNULÉE`.
- **Détails d'une Réservation :**
  - Fiche voyageur complète (Nom, Téléphone WhatsApp, Email, Pays d'origine, Demandes spéciales).
  - Dates d'arrivée (Check-in) et de départ (Check-out) avec calcul automatique du nombre de nuitées et du total.
  - Historique des actions (horodatage de la réservation, date de confirmation, agent ayant validé).
- **Actions Opérationnelles :**
  - Bouton **"Valider Check-In"** (Le client est arrivé à la réception).
  - Bouton **"Valider Check-Out"** (Libération automatique de la chambre pour le service de ménage).
  - Annulation avec motif et gestion des politiques de remboursement.

---

### 💳 MODULE 5 : Validation des Preuves de Paiement Panafricaines (Mobile Money & Banque)
Ce module est le cœur de la résilience financière en Afrique :
- **Réception des Justificatifs :**
  - Affichage des captures d'écran des bordereaux de transfert soumis par les clients depuis l'application mobile.
  - Affichage de l'ID de transaction extrait (ex: `PP260912.1432.A12345`).
- **Opérateurs Supportés :**
  - 🇨🇩 RDC : M-Pesa (Vodacom), Airtel Money, Orange Money, Virement Rawbank/EquityBCDC.
  - 🇷🇼 Rwanda : MTN Mobile Money, Airtel Money, Bank of Kigali.
  - 🇨🇮 Côte d'Ivoire & 🇸🇳 Sénégal : Wave, Orange Money, Moov Money.
  - 🇰🇪 Kenya & 🇹🇿 Tanzanie : M-Pesa (Safaricom/Vodacom), Tigo Pesa.
  - 🌍 International : Cartes Visa / Mastercard / Stripe.
- **Actions de Contrôle :**
  - Bouton **"Valider le Paiement"** -> Déclenche automatiquement l'envoi d'un SMS de confirmation au client avec son code de réservation et passe le statut à `CONFIRMÉE`.
  - Bouton **"Rejeter / Preuve Invalide"** -> Envoi d'une notification push/SMS au voyageur pour lui demander un justificatif valide.

---

### 🏢 MODULE 6 : Initialisation & Création Complète d'un Établissement (Hôtel, Résidence, Lodge, Villa)

Ce formulaire d'enregistrement (Onboarding) doit collecter toutes les informations nécessaires à la publication dans l'application mobile et aux versements financiers.

#### 1. Typologie & Identité de l'Établissement
- **Type d'hébergement :** `Hôtel d'Affaires`, `Resort Balnéaire / Lacustre`, `Écolodge Safari`, `Appart-Hôtel / Résidence Meublée`, `Maison d'Hôtes / Villa Privée`.
- **Nom officiel commercial :** Ex: *Goma Serena Hotel*, *Résidence Fleuve Congo*.
- **Classification & Standing :** Nombre d'étoiles (1★ à 5★ Luxe) ou label d'authenticité.
- **Slogan & Description multilingue :** Présentation immersive, histoire, points forts (Français / Anglais).
- **Règlement intérieur & Politiques :**
  - Heure d'arrivée minimale (Check-in standard, ex: 14h00).
  - Heure limite de départ (Check-out standard, ex: 11h00).
  - Politiques relatives aux animaux, fêtes, tabac et enfants.
  - Conditions d'annulation (Flexible 24h, Modérée, Non remboursable).

#### 2. Localisation Géographique Précise
- **Pays Panafricain :** RDC, Rwanda, Côte d'Ivoire, Sénégal, Kenya, Tanzanie, Cameroun, etc.
- **Province / Région & Ville :** Ex: *Nord-Kivu / Goma*, *Kinshasa / Gombe*, *Abidjan / Cocody*.
- **Adresse physique complète :** Rue, Avenue, Quartier, Numéro de parcelle.
- **Points de repère notables :** Ex: *À 5 min de l'Aéroport International, Bord du Lac Kivu*.
- **Coordonnées GPS exactes :** Latitude & Longitude (pour centrage Google Maps et calcul d'itinéraire).

#### 3. Équipements & Services Généraux de l'Établissement (Cochables)
- **Connectivité & Travail :** WiFi Haut Débit Fibre Optique gratuit, Espace Co-working, Salles de conférence / Banquets.
- **Bien-être & Loisirs :** Piscine (Extérieure/Chauffée), Spa / Massage, Salle de sport (Fitness), Jardin tropical, Plage privée.
- **Restauration :** Restaurant gastronomique sur place, Bar / Lounge panoramique, Room Service 24h/24, Petit-déjeuner buffet inclus/en option.
- **Transports & Sécurité :** Navette aéroport gratuite/payante, Parking privé sécurisé avec gardiennage 24/7, Groupe électrogène industriel (Garantie énergie 24/24), Réserve d'eau autonome.

#### 4. Galerie Médias & Photos HD
- **Photo de couverture / Bannière principale :** Format 16:9 haute résolution.
- **Galerie générale de l'établissement :** Minimum 5 photos HD (Façade, Réception, Espaces communs, Restaurant, Piscine/Vue).
- **Visite virtuelle 360° ou Vidéo de présentation (Optionnel).**

#### 5. Configuration Financière & Moyens de Reversement (Payouts)
- **Devises acceptées à la réception :** $ USD, Franc Congolais (CDF), Franc CFA (XOF), Franc Rwandais (RWF), Shilling Kenyan (KES).
- **Taux de commission négocié BookZZZ :** Pourcentage contractuel (ex: `10%`, `12%`, `15%`).
- **Coordonnées pour le versement des fonds :**
  - **Comptes Mobile Money Marchands de l'hôtel :** Numéro M-Pesa Marchand / Till Number, Numéro Airtel Money Marchand, Compte Orange Money / Wave.
  - **Coordonnées Bancaires (RIB / IBAN / SWIFT) :** Nom de la banque (Rawbank, EquityBCDC, Bank of Kigali, Ecobank), Titulaire du compte, Numéro de compte.

#### 6. Gestion des Contacts & Accès Administrateurs de l'Hôtel
- **Contact Directeur Général / Propriétaire :** Nom complet, Téléphone WhatsApp direct, Email officiel.
- **Contact Chef de Réception (Front-Desk) :** Téléphone pour les urgences de réservations de nuit.
- **Création du Premier Compte Manager :** Email d'accès initial et génération du mot de passe temporaire pour le portail hôtelier.

---

### 📈 MODULE 7 : Rapports Financiers, Facturation & Commissions
- **Rapprochement Financier :**
  - Calcul automatique du partage des revenus : **Revenu Brut Voyageur - Commission BookZZZ = Montant Net à Reverser à l'Hôtel**.
- **Génération de Factures & Reçus :**
  - Génération automatique de reçus fiscaux au format PDF téléchargeable pour les clients et les hôtels.
- **Export Comptable :**
  - Bouton d'exportation de l'ensemble des écritures comptables sous format **CSV et Excel (.xlsx)** filtrable par période (semaine, mois, année).

---

## 🛠️ 4. SPÉCIFICATIONS TECHNIQUES & STACK RECOMMANDÉE

Pour permettre à votre frère de développer ce portail de manière indépendante :

### 4.1 Front-End Web
- **Framework :** **Next.js 14+ (App Router)** ou **React 18+**.
- **Styling UI :** **Tailwind CSS** + Bibliothèque de composants **Shadcn/UI** ou **Tremor Dashboard UI**.
- **Icônes & Graphiques :** **Lucide-React** + **Chart.js** (ou **Recharts**).
- **Gestion d'État :** **Zustand** ou **TanStack React Query**.

### 4.2 Back-End & Base de Données
- **Base de données & Auth :** **Firebase (Firestore + Firebase Auth + Firebase Storage)**.
  - *Avantage majeur :* Partage directement la même base de données Firestore que votre application mobile Android native sans avoir à réécrire une API complète !

### 4.3 Passerelles de Communication & Hébergement
- **SMS & WhatsApp API :** Twilio, Infobip ou Termii (SMS transactionnels en Afrique).
- **Hébergement :** **Vercel** ou **Firebase Hosting**.

---

## 📅 5. DÉCOUPAGE DU PROJET EN SPRINTS (Planning de Réalisation)

| Sprint | Durée estimée | Livrables attendus |
| :--- | :--- | :--- |
| **Sprint 1** | 1 semaine | Configuration du projet (Next.js + Tailwind + Palette officielle), Système d'Authentification (SuperAdmin / HotelAdmin). |
| **Sprint 2** | 1 semaine | Module Gestion des Chambres (CRUD, upload photos, modification prix/disponibilité). |
| **Sprint 3** | 1.5 semaine | Module Réservations & Planning (Filtres, statuts, actions Check-in / Check-out). |
| **Sprint 4** | 1 semaine | Module Validation des Preuves Mobile Money + Passerelle d'envoi de SMS de confirmation. |
| **Sprint 5** | 1 semaine | Tableaux de bord analytics (Chart.js), Rapports de commissions et Export CSV. |
| **Sprint 6** | 3 jours | Tests fonctionnels, optimisation mobile/tablette et Déploiement en production sur Vercel/Firebase. |
