# BookZzz – Application Native Android

**BookZzz** est une application mobile **native Android** développée en Kotlin. Elle permet aux utilisateurs de :

- **S'inscrire / se connecter** via Firebase Auth.
- **Consulter le catalogue d'hôtels** et les chambres disponibles.
- **Effectuer une réservation** en quelques taps.
- **Uploader une preuve de paiement** (image ou PDF) qui est stockée dans Firebase Storage et qui met à jour le statut de la réservation.
- **Recevoir des rappels** de réservation via le `BookingReminderReceiver`.

> **NOTE** : Le projet ne possède plus de code multiplatform ; toutes les parties *admin* ont été supprimées et restent uniquement dans le répertoire `web-admin` (une application web séparée).

## Architecture

- **UI** : Jetpack Compose + Material 3.
- **Gestion du cycle de vie** : ViewModel + Kotlin Coroutines.
- **Backend** : Firebase Firestore & Firebase Storage (pour les réservations et les preuves de paiement).
- **Notifications** : BroadcastReceiver (`BookingReminderReceiver`).

## Prérequis

- Android Studio Arctic Fox or newer.
- JDK 11 or 17.
- Un projet Firebase configuré avec les services **Auth**, **Firestore** et **Storage**.

## Installation

```bash
# Clone du dépôt (le repo Android uniquement)
git clone https://github.com/CHRISTIANAGANZE/BookZzz.git
cd BookZzz

# Copier le fichier d'exemple de configuration Firebase
cp .env.example .env.local   # puis remplissez les variables Firebase

# Installer les dépendances Gradle
./gradlew clean assembleDebug
```

Lancez l’application depuis Android Studio ou en installant le fichier `app-debug.apk` généré.

## Contribuer

Les contributions sont les bienvenues ! Veuillez :
1. Fork le dépôt.
2. Créer une branche `feature/…`.
3. Ouvrir une Pull Request.

Assurez‑vous que le code compile et que les tests (s’il y en a) passent.

## Licence

MIT License – voir le fichier `LICENSE` à la racine du dépôt.
