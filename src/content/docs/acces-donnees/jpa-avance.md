---
title: "Spring Data JPA avancé"
---

## Panorama des approches JPA

| Approche | Abstraction | Cas d'usage type |
| :--- | :--- | :--- |
| **Query Methods** | Maximale | Filtres simples dérivés du nom |
| **`@Query` (JPQL / SQL)** | Déclarative | Requêtes complexes ou spécifiques |
| **Projections (`Record`)** | Élevée | Lecture seule sur colonnes ciblées |
| **Repo Custom (`EntityManager`)** | Impérative | Requêtes dynamiques (critères optionnels) |
| **Specifications (`Criteria API`)** | Composée | Filtres dynamiques modulaires et pagination |

*(Pour les traitements massifs ou le SQL direct hors ORM, voir [SQL direct avec JDBC & Batching](/formation-api-rest/acces-donnees/jdbc-sql/).)*

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
