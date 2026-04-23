# 💣 Java Escape — Escape Game en Visual Novel JavaFX

> Un escape game interactif en style **Visual Novel** : dialogues animés, quiz dynamique et fins alternatives. Trouvez les indices, désamorcez la bombe et sauvez la ville.

---

## 📋 Sommaire

- [Aperçu](#-aperçu)
- [Fonctionnalités](#-fonctionnalités)
- [Prérequis](#-prérequis)
- [Lancer l'application](#-lancer-lapplication)
- [Structure du projet](#-structure-du-projet)
- [Déroulement du jeu](#-déroulement-du-jeu)
- [Technologies utilisées](#-technologies-utilisées)

---

## 🎮 Aperçu

Java Escape est un jeu narratif développé en **Java 17 + JavaFX 21**. Le joueur incarne un agent chargé de localiser et désamorcer une bombe. La progression se fait en alternant **dialogues avec effet machine à écrire** et un **quiz de culture générale** traduit en français à la volée.

---

## ✨ Fonctionnalités

| Fonctionnalité | Détail |
|---|---|
| 🗣️ Dialogues animés | Effet machine à écrire (30 ms/caractère) |
| ⌨️ Raccourci ESPACE | Premier appui : finit l'animation — Second appui : dialogue suivant |
| 🧠 Quiz dynamique | 50 questions récupérées depuis l'API **OpenTDB** |
| 🌍 Traduction FR | Questions traduites EN→FR via **Google Translate (gratuit)** |
| ⚡ Pré-chargement | La question suivante est traduite en arrière-plan pendant que tu réponds |
| ✅ Progression | 5 bonnes réponses suffisent pour avancer |
| 🏆 Fin Victoire | Dialogues de félicitations du Chef |
| 💥 Fin Défaite | Écran d'explosion avec messages d'échec |
| 🎨 UI premium | Thème CSS sombre avec dégradés, cartes, feedback coloré |

---

## 🛠️ Prérequis

- **Java 17** ou supérieur ([Temurin](https://adoptium.net/) recommandé)
- **Maven** (inclus via le wrapper `mvnw` — pas besoin d'installation)
- Connexion Internet (pour l'API de quiz et la traduction)

---

## 🚀 Lancer l'application

### Windows (PowerShell)

```powershell
.\mvnw.cmd javafx:run
```

### Linux / macOS

```bash
./mvnw javafx:run
```

### Via un IDE (IntelliJ IDEA, Eclipse, VS Code)

Exécuter directement la classe :

```
com.example.javaescape.Launcher
```

---

## 📁 Structure du projet

```
java-escape/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/javaescape/
│       │       ├── Launcher.java              # Point d'entrée principal
│       │       ├── HelloApplication.java      # Bootstrap JavaFX + styles globaux
│       │       ├── TitleController.java       # Écran titre
│       │       ├── HelloController.java       # Dialogue d'introduction (Chef)
│       │       ├── QuizApp.java               # Logique quiz + traduction + dialogues
│       │       ├── IntermediaryController.java # Dialogue intermédiaire
│       │       └── Question.java              # Modèle question OpenTDB
│       └── resources/
│           └── com/example/javaescape/
│               ├── app.css                    # Thème CSS global
│               ├── title-view-fr.fxml         # Écran d'accueil
│               └── hello-view-fr.fxml         # Écran dialogue intro
├── pom.xml
└── README.md
```

---

## 🎯 Déroulement du jeu

```
[Écran Titre]
      ↓
[Dialogue d'introduction — Le Chef × 4 répliques]
      ↓
[Quiz — API OpenTDB traduit EN→FR]
  → Continue jusqu'à 5 bonnes réponses
      ↓
[Dialogue intermédiaire — Le Chef × 5 répliques]
      ↓
     / \
    /   \
[VICTOIRE]  [DÉFAITE]
  Chef × 3   Explosion × 4
  répliques   messages
```

### Règle du quiz

- Les questions sont tirées aléatoirement depuis **OpenTDB** (culture générale, toutes catégories).
- Chaque question propose **4 réponses** (1 correcte, 3 incorrectes).
- **5 bonnes réponses** = passage au dialogue intermédiaire.
- Aucune limite de tentatives — on continue jusqu'à atteindre le score.

---

## 🧰 Technologies utilisées

| Technologie | Version | Rôle |
|---|---|---|
| Java | 17 | Langage principal |
| JavaFX | 21.0.6 | Interface graphique |
| Gson | 2.11.0 | Parsing JSON (réponses API) |
| OpenTDB API | — | Banque de questions (gratuit, sans clé) |
| Google Translate API | — | Traduction EN→FR (gratuit, sans clé) |
| Maven | via wrapper | Build & dépendances |
| JUnit Jupiter | 5.12.1 | Tests unitaires |

---

## 👥 Équipe

Projet réalisé dans le cadre d'un cours de développement Java — **Java Escape Game**.

---

## 📜 Licence

Usage pédagogique uniquement.
