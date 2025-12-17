# Jackalf-Gestion-de-Parc-Informatique-Ticketing-en-Java
Gestion de Parc Informatique &amp; Ticketing en Java

Projet : Under Jackal (Gestion d'Incidents)
Application de gestion de tickets et de parc informatique développée en JavaFX. L'interface simule un tableau de bord.
Fonctionnalités
L'application est divisée en deux espaces distincts selon le rôle de l'utilisateur (Admin ou User).
Espace Utilisateur
Création de ticket : Formulaire pour signaler un incident sur un appareil spécifique.

Suivi : Liste des tickets en cours avec leur statut (En attente, En cours, Résolu) et leur sévérité.

Feedback : Consultation des réponses et rapports techniques laissés par l'administrateur.

Espace Administrateur
Dashboard : Vue d'ensemble avec des compteurs (Tickets critiques, nombre d'appareils, etc.).

Gestion du Parc : Ajout, modification et suppression des équipements réseaux (Routeurs, Switchs, PC).

Traitement des Tickets :

Changement de statut.

Rédaction d'un rapport d'intervention.

Assignation du ticket à l'administrateur connecté.

Attribution : Possibilité de lier un appareil à un utilisateur spécifique.

Sécurité & Technique
Base de données : MySQL.

Sécurité : Les mots de passe sont hachés en SHA-256 avant d'être envoyés en base.

Design : Interface entièrement personnalisée via CSS 
rérequis
Java JDK 21 (ou supérieur)

MySQL Server

Librairie JavaFX

Driver MySQL Connector (JDBC)

Installation de la Base de Données

CREATE DATABASE IF NOT EXISTS jackal_db;
USE jackal_db;


CREATE TABLE utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    mdp VARCHAR(100) NOT NULL, -- Hash SHA-256
    role ENUM('ADMIN', 'USER') NOT NULL
);

CREATE TABLE appareils (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100),
    ip VARCHAR(50),
    etat VARCHAR(20),
    type VARCHAR(50),
    id_user INT DEFAULT NULL,
    FOREIGN KEY (id_user) REFERENCES utilisateurs(id) ON DELETE SET NULL
);

CREATE TABLE signalements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_user INT,
    id_appareil INT,
    sujet VARCHAR(255),
    description TEXT,
    date_signalement DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(50),
    severite VARCHAR(20) DEFAULT 'MOYENNE',
    rapport_admin TEXT,
    id_admin INT,
    FOREIGN KEY (id_user) REFERENCES utilisateurs(id),
    FOREIGN KEY (id_appareil) REFERENCES appareils(id),
    FOREIGN KEY (id_admin) REFERENCES utilisateurs(id)
);

-- 2. Données de test (Mots de passe : "admin" et "user")
INSERT INTO utilisateurs (nom, mdp, role) VALUES 
('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'ADMIN'),
('user', '04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb', 'USER');

INSERT INTO appareils (nom, ip, etat, type) VALUES 
('Router-Main', '192.168.1.1', 'UP', 'Routeur'),
('Switch-Floor-1', '192.168.1.20', 'MAINTENANCE', 'Switch');

 Comptes de TestPour tester l'application, utilisez les identifiants suivants : 
Administrateur	admin	admin123
Utilisateur	user	user123

Lancement & Configuration

Main.java : Point d'entrée "wrapper" (contourne les restrictions de modules JavaFX).

LauncherApp.java : Classe principale JavaFX qui lance l'interface de connexion.

BDManager.java : Singleton gérant la connexion unique à la base de données MySQL.

SecurityUtils.java : Utilitaire de sécurité pour le hachage des mots de passe (SHA-256).

Modèles & DAO (Backend)

Utilisateur.java : Objet métier représentant un utilisateur (ID, Nom, Rôle).

UtilisateurDAO.java : Gestion des requêtes SQL liées aux utilisateurs (Login, Création de compte).

Interfaces Principales (Vues)
UILogin.java : Écran d'authentification sécurisé.

UIAdmin.java : Dashboard complet pour l'administrateur (KPIs, Parc, Tickets).

UIUser.java : Interface simplifiée pour les employés (Signalement, Historique).

Fenêtres Modales (Popups)
DevicePopup.java : Formulaire d'ajout/modification d'un équipement réseau.

UserPopup.java : Formulaire de création rapide d'un nouvel utilisateur.

SignalementPopup.java : Détails d'un ticket (Vue Utilisateur).

SignalementAdminPopup.java : Interface de résolution et rapport technique (Vue Admin).

style/style.css : Feuille de style globale.
