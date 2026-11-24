---
title: "Stratégies de tests sur un backend Spring Boot"
---

## Pourquoi tester ?

Au-delà de la conformité fonctionnelle, de l'ergonomie (UI/UX, accessibilité), de la performance, de la fiabilité et de la sécurité, une application réussie doit répondre dans la durée à deux exigences souvent sous-estimées :

- **Fonctionner en rythme courant sans intervention de l'équipe informatique** (utilisateurs autonomes en production).
- **Conserver un coût de MCO / MCS faible** (*Maintien en Condition Opérationnelle et de Sécurité*).

Or deux constats reviennent sur de nombreux projets :

1. **Un temps important est absorbé par le suivi de production et la correction de bugs**, au détriment des nouvelles fonctionnalités. Concevoir l'application pour rendre les utilisateurs autonomes et sécuriser les comportements libère ce temps de développement.
2. **Les montées de version des bibliothèques sont repoussées voire abandonnées**, par crainte de casser l'existant.

:::note[Idée reçue : « Écrire des tests est inutile, la MOA fait déjà une recette — et en plus ça prend du temps »]
La recette manuelle valide un instant T, pas les modifications du lendemain. Investir dans des tests automatisés améliore directement la **productivité future du SI** :

- **Automatiser les tâches répétitives :** un informaticien évite de refaire sans cesse les mêmes vérifications à la main à chaque livraison.
- **Évoluer et refactorer sans crainte :** les tests détectent immédiatement les régressions dans la base de code.
- **Faciliter les montées de version (MCS) :** si la suite de tests automatisés passe après une mise à jour de dépendance, la vérification manuelle devient réduite et ciblée — voire nulle si la couverture de tests est vraiment complète.
:::

![Évolution du coût de maintenance avec et sans tests automatisés](../../../assets/images/cout-maintenance-tests.svg "Coût de maintenance (MCO/MCS) avec et sans tests automatisés")

### Tests manuels vs tests automatisés : deux rôles complémentaires

Automatiser les tests ne rend pas les tests manuels inutiles : **les deux répondent à des besoins différents**. En automatisant les vérifications répétitives, l'équipe libère justement du temps pour les tests humains à forte valeur ajoutée :

- **Tests automatisés :** idéaux pour tout ce qui est **répétitif et déterministe** (non-régression à chaque commit, respect du contrat HTTP, règles métier, sécurité, montées de version).
- **Tests manuels :** indispensables pour les **tests exploratoires** (chercher des usages imprévus), la validation métier d'une nouvelle fonctionnalité, ainsi que l'évaluation de l'ergonomie (UX) et de l'accessibilité réelle.

## Typologie des tests

Avant de définir une stratégie de tests, il est indispensable de distinguer les **cinq niveaux complémentaires** selon leur périmètre d'isolation et le niveau de confiance apporté, ainsi que deux angles d'approche :

- **Boîte blanche (*clear box*) :** le test s'appuie sur l'implémentation interne des classes (typiquement les tests unitaires avec mocks).
- **Boîte noire (*opaque box*) :** le test ne connaît que les entrées/sorties externes sans dépendre du découpage interne (typiquement les tests d'intégration HTTP et E2E), ce qui autorise le refactoring sans casser les tests.

### Analyse statique

Contrôle le code **sans l'exécuter**, dès l'IDE et à chaque build : compilateur Java (typage fort), règles de style/formatage (Spotless, Checkstyle) et analyseur statique (**SonarQube**). C'est le filet de sécurité le plus rapide et le moins coûteux pour détecter erreurs de types, code mort, bogues évidents et complexité excessive.

:::tip[Exemple]
Le compilateur refuse un changement de type de retour dans un DTO, et SonarQube signale une exception avalée silencieusement ou une faille potentielle avant même l'exécution d'un test.
:::

### Test unitaire

Vérifie le comportement d'une unité de logique métier (une méthode ou une classe) **en mémoire, sans framework ni entrées/sorties** (pas de base de données, pas de réseau, pas de système de fichiers).

- **Test unitaire isolé :** toutes les dépendances internes de la classe testée sont remplacées par des doublures (*mocks* ou *stubs*).
- **Test unitaire élargi :** la classe testée collabore avec de vrais objets métier en mémoire (objets-valeurs, validateurs purs), toujours sans I/O ni contexte Spring.

:::tip[Exemple]
Tester une règle de calcul sur des cas limites via un test paramétré (exécution en quelques millisecondes).
:::

### Test d'intégration

Vérifie le fonctionnement de l'application avec son infrastructure technique réelle (contexte Spring, sécurité, sérialisation JSON, requêtes SQL/JPA). Seuls les systèmes externes hors du périmètre du service (APIs tierces, serveur mail, horloge système) sont simulés aux frontières. Ce niveau regroupe deux périmètres :

- **Test d'intégration de tranche (*Slice Test*) :** charge uniquement une couche technique Spring (`@WebMvcTest` pour la couche web, `@DataJpaTest` pour la persistance JPA).
- **Test d'intégration complet (ou *Test de composant*) :** démarre l'application complète et l'interroge par son point d'entrée externe (requête HTTP), connectée à une base de données de test. C'est la forme de référence dans les architectures orientées API/microservices.

:::tip[Exemple]
Envoyer une requête `POST /api/exclusions` avec un corps JSON et un rôle utilisateur, puis vérifier le statut HTTP `201`, la structure du JSON retourné et l'enregistrement en base de données.
:::

### Test de contrat

Vérifie uniquement la compatibilité du **format d'échange** (schéma JSON, noms et types des champs, codes HTTP) entre un **Consommateur** (client) et un **Fournisseur** (API), sans déployer les deux applications simultanément (outils : *Pact*, *Spring Cloud Contract*).

:::tip[Exemple]
Le front-end (consommateur) publie un contrat stipulant qu'il lit les champs `hostname` (string) et `critique` (booléen) sur `GET /api/serveurs`. Ce contrat est rejoué dans la CI du back-end (fournisseur) pour garantir qu'aucun renommage de champ ne cassera le front-end.
:::

### Test de bout en bout (*End-to-End* / *E2E*)

Vérifie un parcours utilisateur complet sur un environnement déployé réunissant tous les composants réels (navigateur, front-end, API back-end, base de données, fournisseur d'identité, services partenaires).

Lents, coûteux à maintenir et plus exposés aux faux positifs (*flaky tests*), les tests E2E doivent rester ciblés sur quelques parcours critiques et ne pas remplacer les tests d'intégration de l'API.

:::tip[Exemple]
Piloter un navigateur via Playwright pour s'authentifier sur un serveur OIDC, saisir un formulaire dans l'interface web et vérifier la mise à jour des données affichées.
:::

## Stratégie historique vs Stratégies modernes

### La Pyramide des tests (Mike Cohn, 2009) et ses limites

Conçue à une époque où le démarrage d'un serveur d'applications était long, elle préconise une isolation maximale : **~80 % de tests unitaires isolés, 15 % de tests d'intégration, 5 % de tests E2E**.

**Limites sur une API Spring Boot orientée données / orchestration :**

- **Couplage fort à l'implémentation (coût de maintenance) :** les tests reproduisent le câblage interne des classes ; dès qu'on renomme, fusionne ou découpe un service lors d'un refactoring, il faut réécrire les configurations de mocks.
- **Faux sentiment de sécurité :** une couverture élevée avec des mocks ne détecte ni les requêtes JPQL/SQL invalides, ni les erreurs de mapping JPA, ni les oublis de `@Transactional`, ni les régressions de sérialisation JSON ou de sécurité (`@RolesAllowed`).

#### Exemple : l'analogie des deux tiroirs

![Analogie des deux tiroirs d'angle : 2 tests unitaires isolés au vert ne garantissent pas l'absence de blocage mutuel sans test d'intégration](../../../assets/images/tiroirs-unitaire-vs-integration.svg "Tests unitaires isolés vs test d'intégration : l'analogie des tiroirs")

*Chaque tiroir fonctionne parfaitement individuellement (tests unitaires au vert), mais combinés ensemble, ils se bloquent mutuellement : l'objectif est que les composants fonctionnent ensemble et soient utilisables en même temps, ce que valide un test d'intégration.*

### Les stratégies modernes : *Testing Honeycomb* et *Testing Trophy*

Grâce au démarrage rapide des conteneurs modernes et à la mise en cache du contexte Spring, l'effort se concentre sur le **test d'intégration complet en boîte noire**, qui offre le meilleur ratio **confiance / coût de maintenance** :

- ***Testing Honeycomb* (Spotify Engineering) :** conçu pour les microservices et APIs. Il place les **tests d'intégration** au centre (service démarré avec sa BDD locale et des bouchons HTTP aux frontières), réduit les tests unitaires aux seuls détails d'implémentation complexes et proscrit les ***Integrated Tests***.
- ***Testing Trophy* (Kent C. Dodds) :** part du principe que *« plus vos tests ressemblent à l'usage réel, plus ils vous donnent confiance »*. Il ajoute un socle indispensable d'**analyse statique** (compilateur Java à typage fort, linters, **SonarQube**) sous les tests unitaires, et fait de la couche **intégration** le cœur de la stratégie.

:::caution[Distinction clé : *Integration Tests* vs *Integrated Tests*]
- **Integration Test *(recommandé)* :** teste **un seul service** de manière autonome dans sa CI (requête HTTP → contrôleur → service → BDD de test), en simulant les partenaires externes aux frontières.
- **Integrated Test *(à éviter dans la CI d'un service)* :** tout test où le service appelle réellement d'autres services en direct (API partenaire en recette, plusieurs microservices démarrés ensemble…). Cela englobe aussi les tests **E2E** complets : comme ces tests sont lents et fragiles, isolez chaque service dans sa CI via des bouchons HTTP + **tests de contrat**, et limitez l'E2E à quelques parcours critiques sur environnement déployé.
:::

### Synthèse comparative

![Comparaison entre la pyramide des tests historique, le Testing Honeycomb et le Testing Trophy](../../../assets/images/pyramide-vs-honeycomb.svg "Pyramide des tests vs Testing Honeycomb et Testing Trophy")

- **Volume principal :**
  - *Pyramide :* tests unitaires isolés (~80 %).
  - *Honeycomb / Trophy :* tests d'intégration complets par HTTP.
- **Socle préalable :**
  - *Pyramide :* aucun niveau dédié avant l'exécution.
  - *Trophy :* analyse statique (typage fort Java, SonarQube, linter).
- **Usage des mocks :**
  - *Pyramide :* massif sur les dépendances internes (`@Service`, `@Repository`).
  - *Honeycomb / Trophy :* limité aux **frontières externes** (APIs tierces, horloge).
- **Point d'entrée et garanties :**
  - *Pyramide :* méthodes Java internes → valide une classe isolée, fragile au refactoring.
  - *Honeycomb / Trophy :* contrat HTTP / JSON → valide sécurité, validation, MVC, SQL/JPA et sérialisation en résistant au refactoring interne.

### Et finalement, quelle stratégie choisir ?

Il n'existe pas une forme unique adaptée à tous les projets : la **Pyramide** reste pertinente pour une bibliothèque ou un moteur purement algorithmique, tandis que le **Testing Honeycomb** et le **Testing Trophy** conviennent nettement mieux aux API Spring Boot orientées données, sécurité et orchestration.

## Règles d'arbitrage et outillage sur Spring Boot

:::note[Quand une API est-elle « suffisamment testée » ?]
L'objectif n'est pas d'atteindre 100 % de couverture, mais d'obtenir **assez de confiance pour livrer en production sereinement** tout en respectant le temps et les **ressources de l'équipe**. On priorise donc par **criticité** : sécuriser d'abord les parcours nominaux (*happy paths*), les droits d'accès, la persistance et les règles métier clés plutôt que tester mécaniquement des DTO ou des getters. Sur Spring Boot, un test d'intégration HTTP avec contexte partagé reste rapide à écrire et peu coûteux à maintenir : c'est surtout l'effort sur les derniers pourcents techniques ou la multiplication des E2E navigateur qui devient peu rentable. En pratique, ce point d'équilibre se situe souvent autour de **90 %** *(à voir comme un simple repère empirique, jamais comme un seuil rigide)*.

Idéalement, une API est suffisamment testée lorsque l'on n'a plus besoin de retester manuellement sans cesse les mêmes comportements, et surtout **lorsque l'on peut déployer en production les montées de version de bibliothèques sans recette manuelle**.
:::

### Quand écrire quel test ?

- **Test d'intégration HTTP par défaut :** pour tout cas d'usage métier, contrôle d'accès (`401`/`403`), erreur de validation (`400`), sérialisation JSON et opération en base de données.
- **Arbitrage « tout en intégration » vs « unitaire + intégration » sur les règles de calcul :**
  - **Tout regrouper en intégration (test paramétré ou `Scenario Outline`) :** lorsque le contexte Spring est partagé, que le jeu de données est léger et que le nombre de cas reste raisonnable (< 15–20 cas). Cela évite de maintenir deux suites pour la même règle.
  - **Séparer en 1 test d'intégration nominal + N tests unitaires purs :** uniquement lorsque la combinatoire est élevée (parseurs complexes, regex, matrices de dates) ou que la préparation de la base noie l'intention métier.
- **Tests de contrat et E2E ciblés :** réserver les tests de contrat (*Pact*, *Spring Cloud Contract*) aux interfaces critiques entre équipes et limiter l'E2E (Playwright) à quelques parcours majeurs.

### Principes d'écriture

- **Tests simples, lisibles et indépendants :** chaque test vérifie un comportement précis, sans dépendre d'un ordre d'exécution ni muter un état partagé pour les tests suivants (données en base ou mocks aux frontières réinitialisés entre deux scénarios).

:::caution[À éviter]
Appeler directement les méthodes Java des `@RestController` dans les tests d'intégration. Cela contourne les filtres de sécurité, la validation `@Valid`, le binding `@RequestParam` et la sérialisation Jackson.
:::

### Choix des outils et simulation des frontières

![Architecture d'un test d'intégration boîte noire par HTTP sur Spring Boot](../../../assets/images/test-integration-boite-noire.svg "Périmètre du test d'intégration boîte noire")

- **Privilégier un outil unique (ou quasi unique) :** plusieurs choix sont valides (tout-JUnit, tout-Cucumber, etc.). Utiliser le même outil pour les tests unitaires et les tests d'intégration homogénéise l'écriture des tests et évite de multiplier les styles d'assertions, en veillant simplement à ne pas démarrer le contexte Spring sur les tests unitaires.
- **Simulation des systèmes externes aux frontières :** plutôt que de mocker les services clients internes avec `@MockitoBean` (ce qui court-circuite les clients HTTP et la désérialisation), simuler les dépendances au niveau du protocole :
  - **[APIs HTTP externes](/formation-api-rest/tests/api-externes/) :** démarrer un serveur HTTP local sur un port de test (ex. `HttpServer` du JDK ou **WireMock**) et y rediriger les URLs configurées.
  - **[Horloge système](/formation-api-rest/tests/horloge-temps/) :** injecter `java.time.Clock` et le remplacer en test (horloge fixe ou mutable `@Primary`) pour garder des tests déterministes sans redémarrer le contexte Spring.
  - **Messagerie et autres infrastructures tierces :** les envois d'e-mails, files de messages ou stockages externes doivent également être simulés ou isolés aux frontières pour préserver l'autonomie des tests *(non détaillé dans cette formation)*.
- **[Base de données de test](/formation-api-rest/tests/bdd-tests/) :** une base en mémoire rapide (H2) convient tant que l'accès aux données repose sur du JPA standard. Basculer sur un vrai moteur PostgreSQL en test (ex. **Testcontainers** ou **Zonky**) dès l'introduction de requêtes SQL natives (`nativeQuery = true`) ou de types spécifiques au SGBD de production.

## Spécificités en développement assisté par agents (IA)

:::note[Pourquoi cette stratégie compte encore plus avec l'IA]
En développement agentique, tester l'application en **boîte noire par HTTP** devient un garde-fou indispensable.
:::

- **Un filet de sécurité objectif contre les angles morts de l'IA :** un agent de code génère rapidement une implémentation crédible en apparence, mais peut introduire une régression sur un mapping JPA, une requête SQL, une sérialisation JSON ou une règle de sécurité. S'il écrit des tests unitaires isolés avec des mocks, il risque de mocker ses propres hypothèses fausses et de produire des tests complaisants qui passent au vert. Le test d'intégration HTTP vérifie le comportement réel de bout en bout (de la requête jusqu'à la base de données), ce qui réduit fortement le risque de valider une implémentation défaillante.
- **Garder la maîtrise du contrat sans ralentir le flux :** laisser l'agent générer le code et les tests à partir d'une liste claire des cas attendus dans le prompt (cas nominaux, erreurs `400`/`403`/`404`), puis concentrer la revue de code sur les assertions générées et sur toute modification apportée aux tests existants.
- **Préserver une boucle de vérification rapide et lisible :** un agent valide son travail par itérations successives en relançant fréquemment les tests. Maintenir un contexte Spring unique partagé, exécuter de manière ciblée les tests modifiés et isoler les algorithmes complexes dans des tests unitaires sans framework (retour sous la seconde, traces d'erreur concises) garantit des cycles de correction courts et efficaces.
