---
title: "SQL direct avec JDBC & Batching"
---

## L'overhead Hibernate : que gagne-t-on hors ORM ?

| Dimension | Spring Data JPA / Hibernate | `JdbcClient` / `JdbcTemplate` |
| :--- | :--- | :--- |
| **Mémoire (RAM)** | Entités en session + snapshots de *dirty-checking* | Zéro rétention d'objet technique |
| **CPU & GC** | Parsing JPQL, *dirty-checking*, forte pression GC | SQL brut direct, latence plancher |
| **Écritures** | Batching complexe (bloqué par `IDENTITY`) | `batchUpdate` natif à plein débit |

- **Le compromis :** débit maximal et contrôle SQL total vs automatisation de l'ORM (relations, *dirty-checking*, portabilité).

## L'alternative moderne : `JdbcClient`

Introduit dans **Spring 6.1** (novembre 2023), `JdbcClient` unifie les templates historiques via une API fluide nativement compatible avec les **Records**.

### Lecture (`SELECT`)

```java title="VinDaoModern.java"
@Repository
@RequiredArgsConstructor
public class VinDaoModern {

    private final JdbcClient jdbcClient;

    public List<VinResponse> findVinsAbordables(String appellation, BigDecimal prixMax) {
        return jdbcClient.sql("""
                    SELECT id, chateau, appellation, annee, prix
                    FROM vin
                    WHERE appellation = :appellation AND prix <= :prixMax
                """)
                .param("appellation", appellation)
                .param("prixMax", prixMax)
                .query(VinResponse.class) // Mapping automatique colonne -> Record
                .list();
    }
}
```

- **Zéro `RowMapper` :** instancie directement le Record par correspondance de noms.
- **Limite sur le `fetchSize` :** l'API fluide n'a pas de méthode `.fetchSize()`. Sans impact pour `.list()`, mais critique pour le **streaming** où il faudra passer par `application.properties` (voir [Stratégies d'extraction et gros volumes](/formation-api-rest/acces-donnees/extraction-gros-volumes/)).

### Écritures unitaires (`UPDATE`, `INSERT`, `DELETE`)

```java
// Retourne le nombre de lignes modifiées
public int updatePrix(Integer id, BigDecimal nouveauPrix) {
    return jdbcClient.sql("UPDATE vin SET prix = :prix WHERE id = :id")
            .param("prix", nouveauPrix)
            .param("id", id)
            .update();
}
```

*(Pour insérer ou mettre à jour des milliers de lignes en lot, `JdbcClient` ne propose pas d'équivalent à `batchUpdate` : on passe par `NamedParameterJdbcTemplate`.)*

## Écritures massives : `saveAll()` vs Batch JDBC

| Critère | `saveAll()` (JPA) | `batchUpdate()` (JDBC) |
| :--- | :--- | :--- |
| **Usage cible** | Petits volumes (< 1 000 lignes) | Imports / Batchs (> 10 000 lignes) |
| **Maintenance** | Standard Spring, 1 ligne de code | Requête SQL et DAO dédiés |
| **Réseau** | $N$ requêtes (par défaut) | 1 seul envoi groupé (`executeBatch`) |
| **Mémoire RAM** | Accumulation dans le cache L1 | Empreinte minimale $O(1)$ |

- **Sous le capot :** par défaut, `saveAll()` boucle sur `save()` et génère **N** requêtes unitaires *(voir le détail du fonctionnement et du batching JPA dans [Spring Data JPA : requêtes et optimisation](/formation-api-rest/acces-donnees/spring-data-jpa/#écriture-synchronisation-et-saveall))*.

### La règle d'arbitrage

1. **Réflexe par défaut (`saveAll()`) :** pour les CRUD standards d'API REST (quelques dizaines à centaines de lignes), productivité maximale et gestion automatique des relations/cascades.
2. **Bascule vers le batch JDBC (`batchUpdate()`) :** pour les traitements lourds (imports CSV, synchronisations, batchs de nuit), afin de passer de $N$ allers-retours réseau à un envoi groupé sans saturer la mémoire de l'ORM.

### Exemple avec `NamedParameterJdbcTemplate.batchUpdate()`

```java title="VinDaoModern.java"
@Repository
@RequiredArgsConstructor
public class VinDaoModern {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void updatePrixEnMasse(List<VinResponse> vins) {
        // Les noms des paramètres nommés (:prix, :id) DOIVENT correspondre aux champs du Record
        String sql = "UPDATE vin SET prix = :prix WHERE id = :id";
        // Mapping automatique : vin.prix() -> :prix, vin.id() -> :id
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(vins);
        // Exécute l'ensemble de la liste en un seul lot JDBC (executeBatch)
        jdbcTemplate.batchUpdate(sql, batch);
    }
}
```

- **Convention stricte :** le mapping automatique repose sur l'égalité stricte entre le nom du champ Record (`prix`) et le paramètre SQL (`:prix`).
- **Envoi groupé :** toute la liste est transmise en un seul appel réseau. S'il y a trop de lignes à insérer, découpez manuellement la liste en sous-listes (via `List.subList` ou par paquets) avant d'appeler `batchUpdate`.

## L'approche historique : `NamedParameterJdbcTemplate` (lecture)

Avant `JdbcClient`, la lecture hors ORM imposait l'écriture d'un `RowMapper` manuel :

```java title="VinDaoClassic.java"
@Repository
@RequiredArgsConstructor
public class VinDaoClassic {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<VinResponse> findVinsAbordables(String appellation, BigDecimal prixMax) {
        String sql = """
            SELECT id, chateau, appellation, annee, prix
            FROM vin
            WHERE appellation = :appellation AND prix <= :prixMax
        """;

        Map<String, Object> params = Map.of(
            "appellation", appellation,
            "prixMax", prixMax
        );

        // Mapping manuel ligne par ligne depuis le ResultSet JDBC
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new VinResponse(
            rs.getInt("id"),
            rs.getString("chateau"),
            rs.getString("appellation"),
            rs.getInt("annee"),
            rs.getBigDecimal("prix")
        ));
    }
}
```

## L'ancêtre Java : JDBC natif avec `PreparedStatement`

La méthode historique manuelle (avant Spring) :

```java title="VinDaoLegacyJdbc.java"
@Repository
@RequiredArgsConstructor
public class VinDaoLegacyJdbc {

    private final DataSource dataSource;

    public List<String> findAllAppellationsJdbc() {
        List<String> retour = new ArrayList<>();
        String sql = "SELECT DISTINCT appellation FROM vin";

        // Try-with-resources pour fermer manuellement Connection, Statement et ResultSet
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                retour.add(rs.getString("appellation"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur d'accès JDBC", e);
        }
        return retour;
    }
}
```
