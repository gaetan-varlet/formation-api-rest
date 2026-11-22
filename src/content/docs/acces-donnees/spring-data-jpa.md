---
title: "Spring Data JPA : requêtes et optimisation"
---

## Méthodes fournies nativement par `JpaRepository`

:::note[Prérequis]
La déclaration de l'interface `JpaRepository<T, ID>` est présentée dans [Configuration de la BDD](/formation-api-rest/spring-boot/configuration-bdd/#création-du-dao-repository), et sa première utilisation dans un contrôleur REST dans [Création d'une API CRUD](/formation-api-rest/api-rest/creation-crud/).
:::

Avant d'écrire la moindre requête personnalisée, hériter de `JpaRepository<T, ID>` fournit un socle complet de méthodes prêtes à l'emploi (issues de `ListCrudRepository` et `ListPagingAndSortingRepository`) :

### Lecture & référence *lazy*

- **Requêtes immédiates (`SELECT`) :** `findById(id)` (`Optional<T>`), `findAll()`, `findAllById(ids)`, `findAll(Sort)`, `findAll(Pageable)` (`Page<T>`), `existsById(id)` et `count()`.
- **Référence sans requête (`getReferenceById(id)`) :** au lieu d'exécuter un `SELECT` immédiat comme le fait `findById`, Hibernate crée un objet factice (*proxy*) qui ne connaît que son `id`. Très utile lorsque l'appelant de l'API vous fournit déjà l'identifiant d'une entité liée (ex. `regionId` dans le JSON d'un `POST`) et que vous voulez rattacher la clé étrangère sans lecture inutile en base :
  ```java
  // regionId provient directement de la requête HTTP (ex. {"chateau": "...", "regionId": 3})
  // Aucun SELECT sur la table region : Hibernate sait qu'il faut écrire region_id = 3 lors de l'INSERT
  vin.setRegion(regionRepository.getReferenceById(regionId));
  vinRepository.save(vin);
  ```
  *(Attention : dès que vous appelez un autre getter comme `region.getNom()`, Hibernate déclenche le `SELECT` — ce qui nécessite une transaction ouverte et lève `EntityNotFoundException` si cet ID n'existe pas en base.)*

### Écriture, synchronisation et `saveAll()`

- **`save(entity)` :** gère à la fois la création et la mise à jour. Si la clé primaire est `null` (ou considérée nouvelle), Hibernate prépare un `INSERT` ; sinon, il charge l'entité puis prépare un `UPDATE` (`merge`).
- **En mémoire vs immédiat (`flush()` / `saveAndFlush()`) :** par défaut, `save()` garde la modification en mémoire dans le cache L1 Hibernate jusqu'au `COMMIT` de la transaction. `flush()` et `saveAndFlush(entity)` forcent l'exécution SQL **immédiate** en base, sans pour autant valider la transaction ni vider le cache L1 (utile par exemple pour lire juste après un champ calculé par un trigger SQL, ou attraper tout de suite une erreur de contrainte). Dans les cas CRUD classiques, `save()` suffit.
- **`saveAll(entities)` (unitaire vs par lot) :** sous le capot, `saveAll()` boucle simplement sur `save()`. Par défaut, enregistrer 500 entités génère donc **500 requêtes SQL unitaires** :
  - **Activer le batching Hibernate :** configurez `spring.jpa.properties.hibernate.jdbc.batch_size=50` **et** évitez `GenerationType.IDENTITY` (qui oblige Hibernate à exécuter chaque `INSERT` immédiatement pour récupérer l'ID généré ; préférez `GenerationType.SEQUENCE`).
  - **Gros volumes :** sur plusieurs milliers de lignes, il faut purger régulièrement le cache L1 (voir ci-dessous) ou basculer sur du batch JDBC pur ([Écritures massives : `saveAll()` vs Batch JDBC](/formation-api-rest/acces-donnees/jdbc-et-batching/#écritures-massives--saveall-vs-batch-jdbc)).

### Suppression

- **`deleteById(id)`, `delete(entity)`, `deleteAll()` :** suppriment entité par entité en respectant les cascades et le cycle de vie JPA (coûteux sur gros volumes car `deleteAll()` charge d'abord les entités puis émet un `DELETE` par ligne).
- **`deleteAllInBatch()`, `deleteAllByIdInBatch(ids)` :** exécutent une **unique** requête SQL `DELETE` directe en base, beaucoup plus rapide en masse, mais sans déclencher les cascades JPA ni les callbacks d'entités.

### Le cache L1 Hibernate : fonctionnement, impacts et alternatives

- **Ce que c'est :** le **cache L1** (ou contexte de persistance / `EntityManager`) est une mémoire de travail propre à chaque transaction. Hibernate y conserve toutes les entités lues (`findById`, `findAll`) ou enregistrées (`save`, `saveAll`), accompagnées d'une copie de leur état initial (*snapshot*). Cela lui permet d'éviter deux `SELECT` identiques dans la même transaction et de détecter automatiquement les champs modifiés (*dirty-checking*).
- **Impact mémoire et performances :**
  - **Mémoire (RAM / `OutOfMemoryError`) :** tant que la transaction reste ouverte, aucune entité n'est libérée ; accumuler des milliers de lignes sature la mémoire JVM.
  - **CPU et GC :** à chaque `flush()` ou `COMMIT`, Hibernate parcourt **l'ensemble** des entités du cache L1 pour comparer chaque attribut à son snapshot (*dirty-checking*), ce qui ralentit les traitements et augmente la pression sur le Garbage Collector.
- **`flush()`, `clear()` et `COMMIT` — ne pas confondre :**

  | Action | Effet en base de données | Effet sur le cache L1 (RAM) |
  | :--- | :--- | :--- |
  | **`entityManager.flush()`** *(ou `saveAndFlush`)* | Envoie le SQL dans la transaction ouverte (*rollback* encore possible) | **Ne le vide pas** : les entités restent en mémoire |
  | **`entityManager.clear()`** | Aucun *(les modifs non flushées sont perdues)* | **Vide le cache L1** : libère la RAM immédiatement |
  | **`COMMIT`** *(fin de `@Transactional`)* | Déclenche un `flush()` final puis **valide définitivement** | **Détruit le cache L1** avec la fin de la transaction |

- **Comment le vider en pratique :**
  - **Automatiquement :** à la fin de la transaction (`COMMIT` ou `rollback`), ou après une requête directe annotée `@Modifying(clearAutomatically = true)`.
  - **Manuellement en cours de traitement :** lors d'un import par lots, découpez en paquets et appelez **`flush()` puis `clear()`** dans cet ordre *(exemple dans [Réception de fichier](/formation-api-rest/fichiers-performance/reception-fichier/#traitement-en-flux--transaction-atomique))*.
- **Alternatives abordées plus loin pour contourner le cache L1 :**
  - **Lecture seule sur colonnes ciblées :** [Projections légères (`Record`)](#projections-légères--requêter-sans-charger-lentité) *(plus bas)* — aucun objet n'entre dans le cache L1.
  - **Mises à jour / suppressions directes :** [`@Modifying`](#requêtes-décriture--modifying) *(plus bas)* ou [Batch JDBC (`batchUpdate`)](/formation-api-rest/acces-donnees/jdbc-et-batching/#écritures-massives--saveall-vs-batch-jdbc) *(page suivante)*.
  - **Lecture / export de gros volumes :** [Stratégies d'extraction et gros volumes](/formation-api-rest/acces-donnees/extraction-gros-volumes/) et [Le streaming](/formation-api-rest/fichiers-performance/streaming/).

## Panorama des approches JPA

Dès que les méthodes natives de `JpaRepository` ne suffisent plus (filtres métier, agrégations, requêtes dynamiques ou optimisations), plusieurs approches s'offrent à vous :

| Approche | Abstraction | Cas d'usage type |
| :--- | :--- | :--- |
| **Query Methods** | Maximale | Filtres simples dérivés du nom |
| **`@Query` (JPQL / SQL)** | Déclarative | Requêtes complexes ou spécifiques |
| **Projections (`Record`)** | Élevée | Lecture seule sur colonnes ciblées |
| **Repo Custom (`EntityManager`)** | Impérative | Requêtes dynamiques (critères optionnels) |
| **Specifications (`Criteria API`)** | Composée | Filtres dynamiques modulaires et pagination |

*(Pour les traitements massifs ou le SQL direct hors ORM, voir [SQL direct avec JDBC & Batching](/formation-api-rest/acces-donnees/jdbc-et-batching/).)*

## Spring Data Query Methods

Spring Data analyse le **nom de la méthode** dans l'interface et génère automatiquement la requête au démarrage :

```java title="VinRepository.java"
public interface VinRepository extends JpaRepository<Vin, Integer> {

    // WHERE appellation = ?
    List<Vin> findByAppellation(String appellation);

    // WHERE prix <= ? ORDER BY annee DESC
    List<Vin> findByPrixLessThanEqualOrderByAnneeDesc(BigDecimal prixMax);

    // Contrôle d'existence sans ramener l'entité
    boolean existsByChateau(String chateau);
}
```

- **Validation stricte au boot :** si une propriété est mal orthographiée, Spring Boot refuse de démarrer.
- **Documentation officielle des mots-clés :** [Spring Data JPA Query Methods](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html)

## Optimisation des relations : le problème du N+1 SELECT

Par défaut, les relations (`@ManyToOne`, `@OneToMany`) chargées à la demande (*LAZY*) déclenchent une cascade de requêtes SQL lors du parcours des données :

```sql
1. SELECT * FROM vin;                         --> 1 requête (ramène N lignes)
2. SELECT * FROM region WHERE id = ?;         --> N requêtes exécutées
3. SELECT * FROM producteur WHERE id = ?;     --> N requêtes exécutées
                                              ─────────────────────────
                                              Total : 1 + 2N requêtes !
```

- **Conséquence :** effondrement des performances et saturation de la base de données.
- **Mauvaise réponse :** passer les relations en `FetchType.EAGER` (dégrade les performances sur toutes les autres requêtes).

### Résoudre le N+1 avec `@EntityGraph`

L'annotation **`@EntityGraph`** surcharge ponctuellement la stratégie de chargement pour la méthode ciblée en forçant une jointure SQL :

```java title="VinRepository.java"
public interface VinRepository extends JpaRepository<Vin, Integer> {

    // Charge Vin, sa Region et son Producteur en une SEULE requête SQL
    @EntityGraph(attributePaths = {"region", "producteur"})
    List<Vin> findAll();

    // Fonctionne également combiné avec une Query Method
    @EntityGraph(attributePaths = {"region", "producteur"})
    List<Vin> findByAppellation(String appellation);
}
```

- **Résultat :** les données liées sont récupérées en un seul aller-retour réseau vers la base de données.

## Requêtes déclaratives avec `@Query`

Nécessaire dès que la requête comporte des agrégations, des sous-requêtes ou devient trop verbeuse pour une *Query Method* :

```java title="VinRepository.java"
public interface VinRepository extends JpaRepository<Vin, Integer> {

    // 1. JPQL : opère sur l'entité Java (vérifié au boot, portable)
    @Query("SELECT DISTINCT v.appellation FROM Vin v WHERE v.prix <= :prixMax ORDER BY v.appellation")
    List<String> findAppellationsAbordables(BigDecimal prixMax);

    // 2. SQL natif : syntaxe directe du moteur SGBD (PostgreSQL)
    @Query(value = "SELECT DISTINCT appellation FROM formation.vin WHERE date_mise_en_bouteille IS NOT NULL", nativeQuery = true)
    List<String> findAppellationsEnBouteille();
}
```

## Projections légères : requêter sans charger l'entité

Évite de charger l'entité complète (`Vin`) en projetant directement le résultat vers un **Record** :

```java title="VinRepository.java"
public record VinResponse(Integer id, String chateau, String appellation, Integer annee, BigDecimal prix) {}

public interface VinRepository extends JpaRepository<Vin, Integer> {
    // 1. Dérivé du nom : colonnes déduites automatiquement du Record
    List<VinResponse> findByAppellation(String appellation);

    // 2. Via @Query : instanciation explicite en JPQL
    @Query("""
        SELECT new fr.insee.formationapirest.dto.VinResponse(
            v.id, v.chateau, v.appellation, v.annee, v.prix
        )
        FROM Vin v WHERE v.annee = :annee
    """)
    List<VinResponse> findResumeByAnnee(Integer annee);
}
```

- **Gains :** seules les colonnes utiles sont lues (pas de `SELECT *`) et aucun objet n'est conservé dans le cache L1 Hibernate.

## Requêtes d'écriture : `@Modifying`

Par défaut, `@Query` n'accepte que des lectures (`SELECT`). Pour exécuter un `UPDATE` ou un `DELETE`, deux annotations sont requises :

```java title="VinRepository.java"
public interface VinRepository extends JpaRepository<Vin, Integer> {
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Vin v SET v.prix = :nouveauPrix WHERE v.id = :id")
    int updatePrix(Integer id, BigDecimal nouveauPrix);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Vin v WHERE v.annee < :anneeLimite")
    int deleteVinsAnciens(Integer anneeLimite);
}
```

- **`@Modifying(clearAutomatically = true)` :** exécute un `executeUpdate()` et purge le cache L1 Hibernate pour éviter de conserver des entités désynchronisées en mémoire.
- **`@Transactional` :** impose un contexte transactionnel obligatoire pour l'écriture (généralement porté par le Service).

## Le Repository Personnalisé (*Custom*)

Indispensable quand la requête doit être construite **dynamiquement** avec des filtres optionnels reçus à l'exécution :

### 1. Définir l'interface du fragment

```java title="VinRepositoryCustom.java"
package fr.insee.formationapirest.repository;

import java.util.List;

public interface VinRepositoryCustom {
    List<String> findAppellationsDynamiques(String filtre, Integer anneeMin);
}
```

### 2. Implémenter avec l'`EntityManager`

Convention stricte : nommer la classe `<NomDuRepoPrincipal>Impl`.

```java title="VinRepositoryImpl.java"
package fr.insee.formationapirest.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class VinRepositoryImpl implements VinRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<String> findAppellationsDynamiques(String filtre, Integer anneeMin) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT v.appellation FROM Vin v WHERE 1=1");

        if (filtre != null && !filtre.isBlank()) {
            jpql.append(" AND LOWER(v.appellation) LIKE :filtre");
        }
        if (anneeMin != null) {
            jpql.append(" AND v.annee >= :anneeMin");
        }

        TypedQuery<String> query = entityManager.createQuery(jpql.toString(), String.class);
        if (filtre != null && !filtre.isBlank()) {
            query.setParameter("filtre", "%" + filtre.toLowerCase() + "%");
        }
        if (anneeMin != null) {
            query.setParameter("anneeMin", anneeMin);
        }

        return query.getResultList();
    }
}
```

### 3. Étendre l'interface et utiliser dans le Service

```java title="VinRepository.java"
public interface VinRepository extends JpaRepository<Vin, Integer>, VinRepositoryCustom {
    // Combine JpaRepository, les Query Methods, @EntityGraph et le code Custom
}
```

```java title="VinService.java"
@Service
@RequiredArgsConstructor
public class VinService {

    private final VinRepository vinRepository;

    public List<String> getAppellations(String filtre, Integer anneeMin) {
        return vinRepository.findAppellationsDynamiques(filtre, anneeMin);
    }
}
```

## Au-delà du Repo Custom : Spring Data Specifications

Pour les écrans de recherche complexes (nombreux filtres optionnels combinables) :

- **Principe :** le Repository étend l'interface standard `JpaSpecificationExecutor<Vin>`.
- **Fonctionnement :** découpe chaque filtre dans une fonction réutilisable (`Specification<T>`), puis les assemble dynamiquement à la volée avec `.and()` et `.or()`.
- **Atouts majeurs :**
  - standard officiel Spring Data basé sur la Criteria API de Jakarta ;
  - gestion native et automatique de la pagination (`Pageable` + calcul du `COUNT`).
- **Pourquoi pas Querydsl ?** Bibliothèque peu maintenue sous Jakarta EE / Spring Boot 3+ ; `Specifications` est le standard intégré sans dépendance ni plugin externe.

Voir la [documentation Spring Data Specifications](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html).

## Arbre de décision : quelle approche choisir ?

```txt
Besoin d'accéder aux données ?
 │
 ├── 1. Filtres simples sur des attributs ?
 │    └── ✅ Query Method (dérivée du nom)
 │
 ├── 2. Requête complexe, agrégation ou jointure spécifique ?
 │    └── ✅ @Query (JPQL par défaut, SQL natif si spécifique SGBD)
 │
 ├── 3. Requête dynamique avec filtres conditionnels ?
 │    ├── Filtres simples (2-3 critères) ──► ✅ Repo Custom + EntityManager
 │    └── Formulaire riche + pagination  ──► ✅ Spring Data Specifications
 │
 └── 4. Volume massif, batch ou performance brute hors ORM ?
      └── ✅ JdbcClient / NamedParameterJdbcTemplate (page suivante)
```

:::tip[Les 2 réflexes d'optimisation JPA]
- **Lecture seule / colonnes partielles :** projeter vers un Record (`VinResponse`).
- **Chargement de relations (`@ManyToOne`) :** ajouter `@EntityGraph` (anti N+1).
:::
