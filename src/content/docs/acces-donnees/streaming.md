---
title: "Streaming de gros volumes (SQL → JSON)"
---

## Le problème du modèle classique (`List<T>`)

Pourquoi `findAll()` sature la JVM sur les gros volumes :

```java
// Modèle bloquant classique
@GetMapping
public List<Vin> findAll() {
    return vinRepository.findAll();
}
```

- **Mémoire en O(N)** : Les milliers de lignes SQL sont instanciées simultanément sur le tas (Heap), puis dupliquées lors de la conversion JSON avant l'envoi réseau.
- **Risque majeur** : Pics de charge mémoire non bornés, cycles violents du Garbage Collector, et risque d'`OutOfMemoryError: Java heap space`.
- **Latence (TTFB dégradé)** : Le client HTTP ne reçoit aucun octet tant que l'ensemble du jeu de données n'est pas entièrement chargé et sérialisé en mémoire.

## Architecture du streaming bout-en-bout

### Principe : Empreinte mémoire constante en O(1)

Au lieu de charger une collection en RAM, les données circulent dans un tuyau continu :

> PostgreSQL (Curseur JDBC) ==> Stream ==> Jackson (Sérialisation) ==> Socket HTTP

- **Unité de traitement** : Chaque enregistrement est lu, sérialisé vers le flux réseau (`OutputStream`), puis devient immédiatement éligible au Garbage Collector.
- **Consommation mémoire** : Constante, que la table contienne 10 lignes ou 2 millions de lignes.
- **Délai initial (TTFB) :** Immédiat ; les premiers octets partent vers le client dès le premier lot lu.

## Étape 1 — L'approche intuitive (Stream d'entités)

Déclarer un retour `Stream<T>` couplé au `fetchSize` :

```java
public interface VinRepository extends JpaRepository<Vin, Long> {
    @QueryHints(value = {
        // OBLIGATOIRE PostgreSQL : active le curseur par lots de X lignes
        @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "2000")
    })
    Stream<Vin> streamAllBy();
}
```

- **Le piège PostgreSQL (`fetch_size`)** : Par défaut (`fetchSize = 0`), le driver JDBC télécharge 100 % du résultat en RAM avant de rendre la main. Définir un lot (ex. 2000) active le curseur côté base.
:::caution[La limite des entités JPA]
Hibernate garde en mémoire chaque objet lu pour surveiller d'éventuelles modifications. Sur un gros volume, cette mémoire interne grossit jusqu'au crash (`OutOfMemoryError`) → d'où le recours indispensable au DTO (section suivante).
:::

## Étape 1 bis — Le pattern de production : Stream de Records

Pour garantir une vraie mémoire en **O(1)** sur des centaines de milliers de lignes, projeter directement le stream dans un **Record** :

```java
public interface VinRepository extends JpaRepository<Vin, Long> {
    @QueryHints(value = {
        @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "2000")
    })
    @Query("""
        SELECT new fr.insee.formationapirest.dto.VinResponse(
            v.id, v.chateau, v.appellation, v.prix
        )
        FROM Vin v
    """)
    Stream<VinResponse> streamAllDtos();
}
```

- **Zéro gestion d'état :** Hibernate instancie le Record à la volée sans rien stocker dans le contexte de persistance (cache L1 désactivé).

## Étape 2 — Le Service (Transaction & Sérialisation)

Consommer le flux et écrire au fil de l'eau :

```java
@Service
@RequiredArgsConstructor
public class VinService {
    private final VinRepository vinRepository;
    private final ObjectMapper objectMapper;
    @Transactional(readOnly = true) // Obligatoire pour maintenir le curseur JDBC ouvert
    public void streamVinsAsJson(OutputStream outputStream) throws IOException {
        // try-with-resources obligatoire : libère le curseur DB et la connexion du pool
        try (Stream<VinResponse> vinStream = vinRepository.streamAllDtos();
             SequenceWriter seqWriter = objectMapper.writerFor(VinResponse.class).writeValuesAsArray(outputStream)) {
            Iterator<VinResponse> iterator = vinStream.iterator();
            while (iterator.hasNext()) {
                seqWriter.write(iterator.next()); // Écriture dans le buffer Jackson
            }
        }
    }
}
```

- **Isolation transactionnelle** : Ne jamais retourner un `Stream` hors de la méthode `@Transactional`, sinon le curseur se ferme avant la lecture.
- **Sérialisation continue** : `SequenceWriter` ouvre le tableau JSON `[`, sépare chaque élément par `,` et le ferme `]` à la clôture du flux.

## Étape 3 — Le Contrôleur Spring MVC

Délégation asynchrone via `StreamingResponseBody` :

```java
@RestController
@RequestMapping("/vins")
@RequiredArgsConstructor
public class VinController {
    private final VinService vinService;

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> streamVins() {
        // Lambda exécutée de manière asynchrone par Spring MVC
        StreamingResponseBody responseBody = outputStream ->
                vinService.streamVinsAsJson(outputStream);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody);
    }
}
```

- **Pourquoi `StreamingResponseBody` ?** Il branche directement le service sur l'`OutputStream` réseau de la servlet sans allouer de buffer global en RAM.
- **Libération des threads** : Le thread HTTP initial est libéré immédiatement ; l'écriture s'exécute sur un pool asynchrone dédié.

## Synthèse : Classique vs Streaming

| Critère | Approche classique (`List<T>`) | Approche Streaming (`Stream<Record>`) |
| :--- | :--- | :--- |
| **Empreinte mémoire (Heap)** | **O(N)** proportionnelle au volume | **O(1) constante (quelques Mo)** |
| **Latence (TTFB)** | Dégradée (attend la fin de la requête) | **Instantanée dès le 1er lot** |
| **Connexion DB (HikariCP)** | Empruntée brièvement | **Mobilisée pendant tout l'envoi réseau** |

:::tip[Appliquer le même principe à un export CSV]
Cette mécanique (`Stream<VinResponse>` + `StreamingResponseBody`) s'applique de manière identique à la génération d'un fichier CSV volumineux : voir [Passer à l'échelle : export CSV en streaming](/formation-api-rest/fichiers/download-csv/#passer-à-léchelle--export-csv-en-streaming).
:::
