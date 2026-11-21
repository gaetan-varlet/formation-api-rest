---
title: "Stratégies de tests sur un backend Spring Boot"
---

Du test unitaire au test d'intégration boîte noire en développement moderne et assisté par IA

## 1. Typologie des tests — Unitaire & Intégration

**Test unitaire** *(en mémoire, sans Spring ni I/O, exécution en ms)*
- **Isolé** : dépendances internes remplacées par des *mocks* ou *stubs*.
- **Élargi** : collaboration avec de vrais objets métier en mémoire (objets-valeurs, validateurs).

**Test d'intégration** *(contexte Spring, sécurité, JSON et SQL réels)*
- **De tranche (*Slice Test*)** : une couche technique isolée (`@WebMvcTest`, `@DataJpaTest`).
- **Complet (*Composant*)** : application entière interrogée par HTTP + base de données de test (**la référence sur une API**).

## 1. Typologie des tests — Contrat & Bout en bout

**Test de contrat** *(Pact, Spring Cloud Contract)*
- Vérifie la compatibilité du **format d'échange** (schéma JSON, types, codes HTTP) entre un **Consommateur** (front) et un **Fournisseur** (API), sans les déployer ensemble.

**Test de bout en bout (*End-to-End* / *E2E*)** *(ex. Playwright)*
- Vérifie un parcours utilisateur complet sur un **environnement déployé** réunissant tous les composants réels (navigateur, front, back, BDD, serveur OIDC).

## 2. Stratégie historique : la Pyramide des tests (2009)

Conçue quand le démarrage d'un serveur prenait plusieurs minutes : **~80 % Unitaires isolés / 15 % Intégration / 5 % E2E**.

**Limites sur une API Spring Boot orientée données :**
- **Faux sentiment de sécurité** : les mocks masquent les requêtes SQL/JPQL invalides, erreurs de mapping JPA, oublis de `@Transactional`, bugs de sérialisation JSON ou failles `@RolesAllowed`.
- **Couplage fort à l'implémentation** : renommer ou découper un `@Service` oblige à réécrire les mocks.

## 2. Stratégies modernes : *Honeycomb* & *Trophy*

Grâce à la mise en cache du contexte Spring, l'effort se concentre sur le **test d'intégration complet en boîte noire** :

1. **Majorité de tests d'intégration par HTTP** : l'application démarre une seule fois. Chaque scénario valide sécurité, validation, métier, SQL et JSON en une passe. Mocks limités aux **frontières externes**.
2. **Base ciblée de tests unitaires purs** : réservée aux algorithmes complexes sans Spring ni BDD.
3. **Sommet minimal (E2E / Contrat)** : parcours critiques déployés et interfaces entre équipes.

## 2. Pyramide vs *Honeycomb* / *Trophy*

- **Volume principal** : Unitaires isolés (~80 %) ➔ **Intégration complète par HTTP**
- **Usage des mocks** : Massif (`@Service`, `@Repository`) ➔ **Strictement aux frontières externes**
- **Point d'entrée testé** : Méthodes Java internes ➔ **Contrat externe (requêtes HTTP / JSON)**
- **Garanties** : Logique d'une classe isolée ➔ **Chaîne complète (sécurité, MVC, SQL, JSON)**
- **Résistance au refactoring** : Faible ➔ **Élevée** (tant que le contrat HTTP reste stable)

## 3. Règles d'arbitrage : quand écrire quel test ?

- **Intégration HTTP par défaut** : cas d'usage métier, sécurité (`401`/`403`), validation (`400`), JSON, persistance BDD.
- **Règles de calcul — Tout en intégration** *(test paramétré / `Scenario Outline`)* :
  - Si contexte partagé rapide, données légères et **moins de 15–20 cas**.
- **Règles de calcul — 1 Intégration + N Unitaires purs** :
  - Si combinatoire élevée (parseurs, regex, matrices de dates) ou setup BDD lourd.
- **Anti-pattern** : appeler directement les méthodes Java d'un `@RestController` (contourne sécurité, `@Valid` et Jackson).

## 3. Outillage et client HTTP (Spring Boot 4.0+)

- **Un outil principal unique** *(tout-JUnit ou tout-Cucumber)* pour homogénéiser le style, sans démarrer Spring sur les tests unitaires.
- **`MockMvc` / `RestTestClient` lié à `MockMvc` (en mémoire)** :
  - Teste sécurité, `@Valid`, `@RestControllerAdvice` et Jackson, avec `@WithMockUser`. Ne teste pas Tomcat.
- **`RestTestClient` avec `RANDOM_PORT` (vrai serveur)** :
  - Teste aussi Tomcat (taille des headers, compression, réseau), mais impose un vrai header `Authorization`.

## 3. Simuler les frontières externes et la base

Éviter `@MockitoBean` sur les clients internes : simuler au niveau du protocole.

- **APIs HTTP externes** : serveur HTTP local (`HttpServer` du JDK, `MockWebServer`, ou `WireMock` pour pannes/latence).
- **Horloge système** : injecter `java.time.Clock` ➔ `Clock.fixed` (unitaire) ou `MutableClock` `@Primary` (intégration).
- **Messagerie & Infra** : `GreenMail` / `Mailpit` (SMTP), `Testcontainers` (Kafka, RabbitMQ, S3/MinIO).
- **Base de données** : `H2` pour du JPA standard ; **`Testcontainers` (PostgreSQL)** dès l'usage de SQL natif ou types SGBD spécifiques.

## 4. Développement assisté par agents (IA)

En développement agentique, le **test d'intégration HTTP en boîte noire** est indispensable :

- **Filet de sécurité objectif** : évite que l'agent mocke ses propres hypothèses fausses (SQL, JPA, sécurité) avec des tests unitaires complaisants.
- **Maîtrise du contrat** : lister les cas attendus dans le prompt (`200`, `400`, `403`, `404`), puis relire en priorité les assertions et modifications de tests.
- **Boucle rapide** : contexte Spring unique partagé + exécution ciblée + algorithmes isolés en unitaire pur (retour **< 1 s**).
