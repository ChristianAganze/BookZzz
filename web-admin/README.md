# 🌐 BookZZZ • Portail d'Administration Web (RDC)

Ce dossier contient le **Portail Web d'Administration et de Gestion Hôtelière BookZZZ** complet, conçu pour être déployé sur n'importe quel hébergeur web moderne (**Vercel, Netlify, Firebase Hosting, Cloudflare Pages** ou serveur Apache/Nginx).

---

## 🚀 Fonctionnalités Clés du Portail Web

1. **Double Espace de Gestion (RBAC)** :
   - **👑 SuperAdmin (Plateforme)** : Supervision globale des établissements en RDC (Goma, Kinshasa, Lubumbashi), rapports de commissions, suivi des flux de trésorerie globaux.
   - **🏨 Manager Hôtelier (Partenaire)** : Suivi en direct des arrivées/départs de son propre hôtel, contrôle des réservations, gestion des disponibilités des chambres.

2. **Validation des Preuves de Paiement RDC** :
   - Interface de vérification des bordereaux et transactions **M-Pesa (Vodacom)**, **Airtel Money** et **Orange Money**.
   - Confirmation en 1 clic avec envoi automatique de confirmation client.

3. **Catalogue des Chambres & Tarification Dynamique** :
   - Modification des prix par nuit ($ USD).
   - Basculement instantané d'état : *Disponible* / *Occupée* / *En Maintenance*.
   - Formulaire d'ajout de nouvelles suites et chambres.

4. **Tableaux de Bord Graphiques & Analytics** :
   - Courbes d'évolution des revenus (Chart.js).
   - Répartition par canal de paiement mobile.
   - Export comptable des réservations au format CSV en 1 clic.

---

## 💻 Comment le Tester et le Déployer en Local ou en Production

### 1. Test Immédiat en Local
Ouvrez simplement le fichier `index.html` dans n'importe quel navigateur moderne (Chrome, Edge, Firefox, Safari) ou lancez un serveur local rapide :
```bash
# Avec Python
cd web-admin
python3 -m http.server 8080

# Ou avec Node.js (npx serve)
npx serve web-admin
```
Puis accédez à `http://localhost:8080`.

---

### 2. Déploiement en 1 Clic sur Firebase Hosting
Pour connecter ce portail directement à votre projet Firebase :
```bash
npm install -g firebase-tools
firebase login
firebase init hosting
# Spécifiez "web-admin" comme dossier public
firebase deploy --only hosting
```

---

### 3. Comptes de Test Rapides Intégrés

- **Compte SuperAdmin :** `aganzec29@gmail.com`
- **Compte Hôtelier (Goma Serena) :** `manager.serena@bookzzz.com`
- **Authentification Google Workspace** intégrée.
