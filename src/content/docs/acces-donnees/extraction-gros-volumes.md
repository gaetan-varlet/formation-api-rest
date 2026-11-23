---
title: "Stratégies d'extraction et gros volumes"
---

## 1. Matrice comparative des approches d'extraction

| Approche | RAM | Débit | Usage cible |
| :--- | :---: | :---: | :--- |
| **JPA Entités (`List`)** | O(N) | Chute (GC) | CRUD unitaire (< 1 000) |
| **JPA Entités (`Stream`)** | **OOM** | Crash | Fausse piste (saturation session L1) |
| **JPA DTOs (`List`)** | O(N) | Baisse | Synchrone modéré (< 20 000) |
| **JPA DTO (`Stream`)** | O(1) | Stable | Flux pérenne + Spring Data |
| **JDBC Stream (`JdbcClient`)** | O(1) | Max | Export brut massif (CSV / JSON) |

:::caution[Piège du `Stream` d'entités JPA]
La session Hibernate garde chaque entité en mémoire (cache L1). Pour streamer de gros volumes, utilisez obligatoirement une **projection DTO Record** ou **`JdbcClient`** (voir aussi la mise en place HTTP dans [Le streaming](/formation-api-rest/fichiers-performance/streaming/)).
:::

## 2. Les 3 règles critiques d'infrastructure

1. **`@Transactional(readOnly = true)` obligatoire (JPA & JDBC) :**
   - Sans transaction active, PostgreSQL est en `autoCommit = true`.
   - Dans ce mode, le driver PostgreSQL **ignore totalement le streaming** et charge tout en RAM JVM.
2. **Dimensionner le `fetchSize` (1 000 à 2 000) :**
   - Compromis optimal latence/débit. Au-delà, gain marginal (< 3 %) et surcoût mémoire inutile.
3. **Tampon Jackson et gestion du `flush()` :**
   - `writer.write()` sérialise dans un buffer mémoire local (~8 Ko), pas directement sur la carte réseau.
   - **Ne jamais flusher dans la boucle :** un `flush()` unitaire forcerait l'envoi de micro-paquets TCP. On flushe **une seule fois** à la fin.

## 3. L'alternative haute performance : JDBC Stream

Accès direct via `jdbcClient`, **100 % indépendant de JPA/Hibernate** :

```java
@Transactional(readOnly = true)
public void streamJdbc(OutputStream os) throws IOException {
    try (SequenceWriter writer = objectMapper.writerFor(VinResponse.class).writeValuesAsArray(os);
         Stream<VinResponse> stream = jdbcClient
                 .sql("SELECT id, chateau, appellation, annee, prix FROM vin")
                 .query(VinResponse.class)
                 .stream()) {

        Iterator<VinResponse> iterator = stream.iterator();
        while (iterator.hasNext()) {
            writer.write(iterator.next());
        }
    }
}
```

- **Indépendance ORM :** ignore `@Entity`, n'ouvre aucun `EntityManager`, mappe directement en Record.
- **Réglage du `fetchSize` PostgreSQL :**
  - soit globalement : `spring.jdbc.template.fetch-size=2000` dans `application.properties` ;
  - soit en code Java : instancier un `JdbcClient` dédié avec `new JdbcTemplate(dataSource).setFetchSize(2000)`.

*(Pour les écritures massives, voir la comparaison `saveAll()` vs `batchUpdate()` dans [SQL direct avec JDBC & Batching](/formation-api-rest/acces-donnees/jdbc-et-batching/).)*

## 4. Synthèse des bonnes pratiques d'extraction

```txt
Quel est l'objectif ?
 │
 ├── 1. Affichage écran / Navigation IHM
 │       ├── Petit volume (< 1 000) ──► ✅ JPA classique (List<T>)
 │       └── Grand volume            ──► ⚠️ Bannir OFFSET / LIMIT
 │                                       ✅ Pagination par curseur (keyset) :
 │                                          WHERE id > :dernierId LIMIT 50
 │
 └── 2. Export de données (CSV / JSON)
         ├── Volume modéré (< 20 000) ──► ✅ JPA Projection DTO synchrone
         ├── Gros volume (Spring Data)──► ✅ JPA DTO Stream + SequenceWriter
         └── Débit maximal brut       ──► ✅ JDBC Stream (JdbcClient)
```
