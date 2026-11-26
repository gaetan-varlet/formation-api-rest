---
title: "Téléchargement et export CSV (Download)"
---

## Principe et contexte

Une API REST ne sert pas uniquement à renvoyer du JSON : un endpoint HTTP `GET` peut aussi retourner directement un fichier à télécharger (`CSV`, `PDF`, `Excel`, image…).

Pour que le navigateur ou le client HTTP comprenne qu'il reçoit un fichier et déclenche un téléchargement, le serveur positionne deux en-têtes HTTP clés dans la réponse :

- **`Content-Type`** : indique la nature du fichier renvoyé (`text/csv`, `application/pdf`, `image/png`…).
- **`Content-Disposition`** : indique au client comment traiter le fichier reçu, via deux valeurs possibles :
  - **`inline`** *(comportement par défaut)* : affiche le fichier directement dans l'onglet du navigateur s'il sait le rendre (ex. image, `PDF`, texte brut).
  - **`attachment; filename="..."`** : force le téléchargement du fichier sur le poste de l'utilisateur sous le nom indiqué (ex. export `CSV`, fichier Excel).

:::tip[Construire `Content-Disposition` avec Spring plutôt qu'à la main]
Côté Java, plutôt que d'écrire manuellement une chaîne `"attachment; filename=\"vins.csv\""`, Spring fournit le builder typé `ContentDisposition` :

```java
ContentDisposition.attachment()
        .filename("vins.csv", StandardCharsets.UTF_8)
        .build();
```

En passant `StandardCharsets.UTF_8`, Spring génère **automatiquement deux paramètres `filename`** dans l'en-tête HTTP :

```http
Content-Disposition: attachment; filename="vins.csv"; filename*=UTF-8''vins.csv
```

En pratique, quand on construit cet en-tête à la main, on n'écrit jamais les deux : on se limite à `filename="..."`, ce qui casse les accents sur de nombreux clients. Avoir les deux est pourtant la bonne pratique HTTP :
- **`filename="..."`** : paramètre historique servant de repli (*fallback*) pour les clients HTTP ou outils anciens qui ne reconnaissent que `filename`.
- **`filename*=UTF-8''...`** : standard moderne (RFC 5987) lu en priorité par les navigateurs récents pour gérer proprement les accents et caractères spéciaux (ex. `châteaux.csv` → `filename*=UTF-8''ch%C3%A2teaux.csv`).

En plus de générer ces deux formats tout seul, le builder gère l'échappement des noms dynamiques (sécurité) et évite les fautes de frappe silencieuses (`attachment()` vs `inline()`).
:::

### Cas d'usage : exporter des données en CSV

Un besoin fréquent consiste à exporter les données de l'application (tables de référence, résultats filtrés, listes métiers) vers un format exploitable dans un tableur (Calc, Excel) ou par un autre système.

Plutôt que de concaténer manuellement des valeurs séparées par `;` (ce qui casse dès qu'une donnée contient un `;`, des guillemets `"` ou un retour à la ligne), on s'appuie sur une bibliothèque pour produire un fichier conforme.

## En mémoire ou en streaming ?

Selon la volumétrie attendue, deux approches existent pour générer le CSV :

**En mémoire (petits volumes) :**

- **Avantages :** logique simple, synchrone et facile à tester ; bien adaptée aux volumes petits et bornés (ordre de grandeur : quelques Mo générés, soit de **quelques milliers à quelques dizaines de milliers de lignes selon le nombre de colonnes**), comme une table de référence ou une liste filtrée.
- **Limites :** charge en même temps en RAM les entités JPA (plus le cache Hibernate), le buffer `StringWriter` et la `String` finale — soit facilement **5 à 10 fois** la taille du fichier. Risque d'`OutOfMemoryError` dès que le volume grossit ou en cas de téléchargements simultanés.

**En streaming (gros volumes) :**

- **Avantages :** lit les données au fil de l'eau via un curseur SQL et écrit directement dans le flux HTTP (`StreamingResponseBody`) avec une empreinte mémoire constante (**O(1)**). Indispensable dès que l'export peut atteindre plusieurs dizaines de Mo, que la table grossit dans le temps (volume non borné) ou que plusieurs utilisateurs exportent en parallèle.
- **Contraintes :** mise en œuvre un peu plus technique (projection DTO en `Stream`, transaction `@Transactional(readOnly = true)` et connexion BDD maintenues ouvertes pendant toute la durée du téléchargement).

## Socle commun aux deux approches

Quelle que soit l'approche retenue (en mémoire ou en streaming), la dépendance CSV, le formatage et la construction des en-têtes HTTP sont partagés.

### 1. Dépendance OpenCSV

Ajout de la bibliothèque **OpenCSV** dans le `pom.xml` pour gérer l'échappement des caractères réservés et les séparateurs :

```xml
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.12.0</version>
</dependency>
```

:::note[Alternatives à OpenCSV]
OpenCSV est très répandu dans les projets existants, mais il embarque plusieurs dépendances transitives (comme `commons-beanutils`) qu'il faut surveiller vis-à-vis des scanners CVE. Pour un nouveau projet Spring Boot, trois alternatives plus légères méritent d'être considérées :

- **Apache Commons CSV (`org.apache.commons:commons-csv`, Apache 2.0) :** proche d'OpenCSV dans sa philosophie, plus léger et porté par la fondation Apache.
- **Jackson CSV (`jackson-dataformat-csv`, Apache 2.0) :** directement géré par le BOM Spring Boot, permet de sérialiser des DTO / `Record` sans conversion manuelle en `String[]`.
- **FastCSV (`de.siegmar:fastcsv`, MIT) :** bibliothèque moderne (Java 17+), très rapide et **sans aucune dépendance transitive** (surface CVE minimale).
:::

### 2. Génération CSV : `CsvUtils`

- Encapsule l'écriture en mémoire (`StringWriter`) ou en flux (`OutputStream`), la configuration explicite du séparateur (`;`) et des guillemets, ainsi que la conversion `List<String>` → `String[]` attendue par OpenCSV.
- **Bonne pratique d'isolation :** `CsvUtils` est la **seule classe du projet qui importe OpenCSV** ; si vous changez un jour de bibliothèque CSV (vers Apache Commons CSV ou FastCSV), aucun Service ni aucun Controller n'est impacté.
- **Pas de `throws IOException` en mémoire :** `StringWriter` travaille uniquement en RAM et ne lève jamais d'`IOException` en pratique — encapsuler le `catch (IOException e)` dans `UncheckedIOException` évite de propager une exception vérifiée inutile dans les `Service` et `Controller` *(à l'inverse, `writeCsv` conserve `throws IOException` pour le streaming réseau, absorbé naturellement par `StreamingResponseBody`)*.

```java
package fr.insee.formationapirest.util;
// import ...
public final class CsvUtils {
    private CsvUtils() {}

    // Pour petits volumes (en mémoire)
    public static String convertDataToString(List<String> titres, List<List<String>> donnees) {
        StringWriter stringWriter = new StringWriter();
        try (ICSVWriter writer = createCsvWriter(stringWriter)) {
            writer.writeNext(titres.toArray(String[]::new));
            for (List<String> ligne : donnees) {
                writer.writeNext(ligne.toArray(String[]::new));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Erreur lors de la génération du CSV", e);
        }
        return stringWriter.toString();
    }

    // Pour gros volumes (en streaming)
    public static void writeCsv(OutputStream outputStream, List<String> titres, Stream<List<String>> lignes)
            throws IOException {
        try (ICSVWriter csvWriter = createCsvWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            csvWriter.writeNext(titres.toArray(String[]::new));
            Iterator<List<String>> iterator = lignes.iterator();
            while (iterator.hasNext()) {
                csvWriter.writeNext(iterator.next().toArray(String[]::new));
            }
        }
    }

    // Configuration commune du format CSV (partagée entre mémoire et streaming)
    private static ICSVWriter createCsvWriter(Writer writer) {
        return new CSVWriterBuilder(writer)
                // séparateur point-virgule (virgule par défaut)
                .withSeparator(';')
                // caractère autour de chaque attribut (doubles quotes par défaut)
                .withQuoteChar(ICSVWriter.DEFAULT_QUOTE_CHARACTER)
                .build();
    }
}
```

:::tip[Sécurité : neutraliser les formules tableur à l'export]
Si votre export contient des champs texte libres saisis par des utilisateurs et est destiné à être ouvert dans un tableur (Excel, Calc), neutralisez les valeurs commençant par `=`, `+`, `-`, `@`, `\t` ou `\r` afin qu'elles ne soient pas interprétées comme des formules. Placez cette logique dans une classe utilitaire dédiée (`ExportSecurityUtils`) pour pouvoir la réutiliser aussi bien pour vos exports CSV qu'Excel ou Calc :

```java
public final class ExportSecurityUtils {
    private static final String FORMULA_PREFIXES = "=+-@\t\r";

    private ExportSecurityUtils() {}

    public static String neutraliserFormule(String val) {
        if (val == null || val.isEmpty()) {
            return val;
        }
        return FORMULA_PREFIXES.indexOf(val.charAt(0)) >= 0 ? "'" + val : val;
    }
}
```

Appelez-la **dans le `Service`** (dans `toCsvRow`), **uniquement sur les colonnes de texte libre** (ex. `ExportSecurityUtils.neutraliserFormule(Objects.toString(vin.getChateau(), ""))`). On ne l'applique pas automatiquement à toutes les cellules dans `CsvUtils` pour ne pas transformer les nombres négatifs (`-12.50` → `'-12.50`) en texte dans Excel/Calc, ni altérer les CSV consommés par un autre programme plutôt que par un tableur.

Cette séparation suit la même règle que pour le HTML : on valide les données à l'entrée sans les altérer en base, et on neutralise à la sortie selon le format cible (voir [Neutralisation des entrées utilisateur](/formation-api-rest/securite/neutralisation-entrees/)).
:::

### 3. En-têtes HTTP : `ExportFileUtils`

Plutôt que de dupliquer la construction des en-têtes HTTP dans chaque contrôleur d'export, on centralise les bonnes pratiques dans un composant dédié (`ExportFileUtils`) :

```java
import java.time.Clock;

@Component
@RequiredArgsConstructor
public class ExportFileUtils {
    private static final DateTimeFormatter DATE_TIME_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final Clock clock;

    public HttpHeaders buildCsvHeaders(String nomFichierBase) {
        return buildAttachmentHeaders(nomFichierBase, "csv", CSV_MEDIA_TYPE);
    }

    public HttpHeaders buildAttachmentHeaders(String nomFichierBase, String extension, MediaType mediaType) {
        String horodatage = LocalDateTime.now(clock).format(DATE_TIME_SUFFIX);
        String nomFichierComplet = String.format("%s_%s.%s", nomFichierBase, horodatage, extension);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(nomFichierComplet, StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(contentDisposition);
        return headers;
    }
}
```

- **Réutilisable multi-formats (`buildAttachmentHeaders`) :** la même méthode sert aussi bien pour un CSV que pour un PDF ou un fichier Excel (`.xlsx`).
- **`Content-Disposition: attachment` via `ContentDisposition` :** force le téléchargement avec gestion propre de l'UTF-8 (`filename` + `filename*`).
- **Horodatage à la seconde (`yyyyMMdd_HHmmss`) :** évite les collisions de nom même lors de plusieurs exports successifs dans la même journée (ex. `vins_20261001_143025.csv`).
- **Pourquoi `@Component` ici (et pas `static` comme `CsvUtils`) ?** `CsvUtils` effectue une transformation pure sans dépendance externe : il reste `static`. À l'inverse, `ExportFileUtils` dépend de l'heure courante (`Clock`) : en faire un bean `@Component` permet d'injecter `Clock` et de figer la date dans les tests (cf. [L'injection de dépendances](/formation-api-rest/spring-boot/injection-dependances/#bean-spring-component-ou-classe-utilitaire-static-) et [Horloge et gestion du temps](/formation-api-rest/tests/horloge-temps/)). Il suffit d'exposer un bean `@Bean Clock clock() { return Clock.systemDefaultZone(); }` dans une classe `@Configuration`.

## Export CSV en mémoire (petits volumes)

### 1. Service

- Récupération des entités et transformation en liste de colonnes (`List<String>`)
- Sécurisation des valeurs nulles avec `Objects.toString()` (indispensable avec `List.of(...)` qui interdit `null`)

```java
@Service
@RequiredArgsConstructor
public class VinService {

    private static final List<String> EN_TETES = List.of("id", "chateau", "appellation", "prix");
    private final VinRepository vinRepository;

    public String exportCsv() {
        List<Vin> vins = vinRepository.findAll();
        List<List<String>> lignes = vins.stream()
                .map(this::toCsvRow)
                .toList();
        return CsvUtils.convertDataToString(EN_TETES, lignes);
    }

    private List<String> toCsvRow(Vin vin) {
        return List.of(
            Objects.toString(vin.getId(), ""),
            Objects.toString(vin.getChateau(), ""),
            Objects.toString(vin.getAppellation(), ""),
            Objects.toString(vin.getPrix(), "")
        );
    }
}
```

### 2. Controller

```java
@RestController
@RequestMapping("/vins")
@RequiredArgsConstructor
public class VinController {

    private final VinService vinService;
    private final ExportFileUtils exportFileUtils;

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> exportCsv() {
        return ResponseEntity.ok()
                .headers(exportFileUtils.buildCsvHeaders("vins"))
                .body(vinService.exportCsv());
    }
}
```

Test dans un navigateur : [http://localhost:8080/vins/export](http://localhost:8080/vins/export) déclenche directement le téléchargement d'un fichier horodaté (ex. `vins_20261001_143025.csv`).

## Passer à l'échelle : export CSV en streaming

Pour contourner les limites mémoire sur les gros volumes, on s'appuie sur le mécanisme présenté dans [Streaming de gros volumes (SQL → JSON)](/formation-api-rest/acces-donnees/streaming/) : un curseur PostgreSQL projeté en `Stream<VinResponse>` écrit directement dans l'`OutputStream` HTTP via `StreamingResponseBody`.

### 1. Service transactionnel en flux

Par rapport à la version en mémoire, la principale différence est l'utilisation d'une **projection DTO en flux** (`Stream<VinResponse>` via `vinRepository.streamAllDtos()`) au lieu de charger des entités JPA (`List<Vin>`), afin d'éviter de saturer le cache Hibernate. La transformation en ligne CSV (`toCsvRow`) reste quasi identique :

```java
@Service
@RequiredArgsConstructor
public class VinService {

    private static final List<String> EN_TETES = List.of("id", "chateau", "appellation", "prix");
    private final VinRepository vinRepository;

    @Transactional(readOnly = true)
    public void streamVinsAsCsv(OutputStream outputStream) throws IOException {
        // Curseur JDBC ouvert uniquement pendant le streaming dans le try-with-resources
        try (Stream<VinResponse> vinStream = vinRepository.streamAllDtos()) {
            CsvUtils.writeCsv(outputStream, EN_TETES, vinStream.map(this::toCsvRow));
        }
    }

    private List<String> toCsvRow(VinResponse vin) {
        return List.of(
            Objects.toString(vin.id(), ""),
            Objects.toString(vin.chateau(), ""),
            Objects.toString(vin.appellation(), ""),
            Objects.toString(vin.prix(), "")
        );
    }
}
```

- **Aucun import OpenCSV ni tableau `String[]` dans `VinService` :** le service reste focalisé sur la transaction et le mapping métier (`toCsvRow` transforme chaque `VinResponse` en `List<String>`).
- **Évaluation paresseuse :** `vinStream.map(this::toCsvRow)` ne charge rien en RAM ; chaque ligne est convertie et écrite une par une au fil de l'eau en **O(1)**.

### 2. Controller avec `StreamingResponseBody`

Le contrôleur réutilise directement `ExportFileUtils` : la seule différence avec la version en mémoire est le retour `StreamingResponseBody` qui branche le service sur le flux HTTP.

```java
@RestController
@RequestMapping("/vins")
@RequiredArgsConstructor
public class VinController {

    private final VinService vinService;
    private final ExportFileUtils exportFileUtils;

    @Operation(summary = "Export de tous les vins au format CSV")
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportVinsCsv() {
        StreamingResponseBody responseBody = outputStream -> vinService.streamVinsAsCsv(outputStream);
        return ResponseEntity.ok()
                .headers(exportFileUtils.buildCsvHeaders("vins"))
                .body(responseBody);
    }
}
```
