---
title: "Présentation de l'univers Spring"
---

## L'univers Spring

Spring est un framework libre pour concevoir des applications Java. Il s'occupe de la plomberie et de l'exécution globale de l'application, ce qui permet aux développeurs de se concentrer sur la configuration du projet et les traitements métiers.

- **Spring Core** : cœur de Spring qui gère la création et le cycle de vie des objets via les annotations (`@Autowired`, `@Controller`, `@Service`, `@Repository`, etc.).
- **Spring MVC** : permet de construire des applications web et API REST basées sur l'API Servlet.
- **Spring Data** : simplifie et unifie l'accès aux bases de données.
- **Spring Batch** : permet de gérer les traitements par lots (*batchs*).
- **Spring Security** : fournit les mécanismes d'authentification, d'autorisation et de protection des requêtes.
- **Spring Boot** : permet de créer facilement et rapidement des applications Spring prêtes à démarrer (avec un serveur Tomcat embarqué par défaut).

## Cycle de vie et versionnage de Spring Boot

Format : `MAJOR.MINOR.PATCH` (*Semantic Versioning*) :

| Type | Contenu et socle | Cadence et support |
| --- | --- | --- |
| **Majeure** (`X.0.0`) | *Breaking changes*, refonte d'architecture, suppression des éléments dépréciés et changement de socle (ex. SB2 : Java 8, SB3 : passage à `jakarta.*`) | Tous les 3 à 4 ans |
| **Mineure** (`x.Y.0`) | Nouvelles fonctionnalités, nouvelles auto-configurations, annonces de dépréciations (rétrocompatible) | **Tous les 6 mois** (mai et novembre) — **12 mois** de support gratuit par branche |
| **Patch** (`x.y.Z`) | Corrections de bugs, correctifs de sécurité (CVE) et mises à jour de dépendances (aucune nouvelle fonctionnalité) | Mensuelle (ou en urgence sur faille critique *zero-day*) |

:::caution[Conséquence pour la production]
Avec une sortie mineure tous les 6 mois et 12 mois de support gratuit par branche, une équipe dispose en pratique d'une fenêtre de **6 mois** après la sortie d'une nouvelle version mineure pour migrer en production et continuer à recevoir les correctifs de sécurité.
:::

## Spring Boot : les principes

- **Les starters** :
  - Un starter est un ensemble cohérent de dépendances prêt à l'emploi (par exemple, le starter web embarque *Spring MVC*, *Jackson*, *Tomcat*…).
  - Plus besoin de gérer individuellement la version de chaque bibliothèque : seule la version du starter parent pilote les versions compatibles de toutes les dépendances.
- **L'auto-configuration** :
  - Configure automatiquement l'application à partir des bibliothèques détectées dans le *classpath*.
  - Supprime les anciens fichiers de configuration XML (`appconfig-mvc.xml`, `web.xml`, `persistence.xml`).

## Les principaux starters

### Starters « métiers »

- **Spring Web** : création d'API REST (Spring MVC, Jackson, serveur Tomcat embarqué).
- **Spring Data JPA** : couche d'accès aux données (Hibernate, Spring Data, pool de connexions HikariCP).

### Outillage développeur

- **Lombok** : élimine le code répétitif (génération des getters, setters, constructeurs à la compilation).
- **Spring Boot DevTools** : confort de développement (redémarrage rapide lors des modifications de code, désactivé automatiquement en production).

### Bases de données

- **H2 Database** : base embarquée en mémoire, démarrage immédiat sans installer de serveur SQL pour travailler en local ou en test.
- **PostgreSQL** : base relationnelle d'entreprise via le driver `postgresql` pour les environnements hors-prod et production.

## H2 vs PostgreSQL : le rôle et les limites d'Hibernate

### Pourquoi l'alternance fonctionne (la force de l'ORM)

- **Abstraction du SQL** : Spring Data génère du code abstrait (JPQL / requêtes dérivées).
- **Rôle du dialecte** : Hibernate détecte le driver (H2 ou PostgreSQL) et traduit le JPQL en SQL adapté au moteur sous-jacent :
  - gestion automatique de la pagination (`LIMIT / OFFSET`) ;
  - fonctions de date et concaténation adaptées au SGBD ;
  - stratégies de génération des clés primaires (`IDENTITY` vs `SEQUENCE`).

### Où s'arrête la magie ?

- **Requêtes natives (`nativeQuery = true`)** :
  - tolérées tant que le SQL reste strictement standard ;
  - piège immédiat dès qu'on utilise des types ou fonctions propriétaires (JSONB, opérateurs PostgreSQL, etc.).

:::tip[Comment limiter les écarts entre H2 et PostgreSQL ?]
- **L'émulation (`MODE=PostgreSQL`)** ajuste l'URL JDBC de H2 pour tolérer une partie de la syntaxe PostgreSQL, mais reste une solution partielle.
- **Testcontainers** (approche moderne) démarre un conteneur Docker PostgreSQL éphémère lors des tests, garantissant une parité stricte avec la production.
:::
