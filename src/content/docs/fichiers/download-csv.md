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

L'approche présentée ici génère le CSV simplement en mémoire : elle convient très bien aux **petits volumes** (jusqu'à quelques milliers de lignes). Pour des extractions volumineuses, il faudra basculer vers le [streaming](#passer-à-léchelle--export-csv-en-streaming).

## Export CSV en mémoire (petits volumes)

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

### 2. Classe utilitaire : CsvUtils

- Encapsule l'écriture du flux dans un `StringWriter` et la configuration explicite du séparateur (`;`) et des guillemets d'échappement.
- **Bonne pratique d'isolation :** `CsvUtils` est la **seule classe du projet qui importe OpenCSV** ; si vous changez un jour de bibliothèque CSV (vers Apache Commons CSV ou FastCSV), aucun Service ni aucun Controller n'est impacté.

```java
package fr.insee.formationapirest.util;
// import ...
public final class CsvUtils {
    private CsvUtils() {}

    public static String convertDataToString(String[] titres, List<String[]> donnees) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (ICSVWriter writer = createCsvWriter(stringWriter)) {
            writer.writeNext(titres);
            writer.writeAll(donnees);
        }
        return stringWriter.toString();
    }

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

### 3. Implémentation du Service

- Récupération des entités et transformation en tableau de chaînes (`String[]`)
- Sécurisation des valeurs nulles avec `Objects.toString()`

```java
@Service
@RequiredArgsConstructor
public class VinService {
    private final VinRepository vinRepository;

    public String exportCsv() throws IOException {
        List<Vin> vins = vinRepository.findAll();
        String[] titres = {"id", "chateau", "appellation", "prix"};
        List<String[]> lignes = vins.stream()
                .map(this::convertVinToString)
                .toList();
        return CsvUtils.convertDataToString(titres, lignes);
    }
    private String[] convertVinToString(Vin vin) {
        return new String[] {
            Objects.toString(vin.getId(), ""),
            Objects.toString(vin.getChateau(), ""),
            Objects.toString(vin.getAppellation(), ""),
            Objects.toString(vin.getPrix(), "")
        };
    }
}
```

:::tip[Sécurité : neutraliser les formules tableur à l'export]
Si votre export contient des champs texte libres saisis par des utilisateurs, c'est **au moment de générer le CSV** (et non lors du stockage en base) qu'il faut neutraliser les valeurs commençant par `=`, `+`, `-`, `@`, `\t` ou `\r` afin qu'Excel ou Calc ne les exécute pas comme des formules :

```java
private static final String FORMULA_PREFIXES = "=+-@\t\r";

private String neutraliserFormuleCsv(String val) {
    if (val == null || val.isEmpty()) {
        return val;
    }
    return FORMULA_PREFIXES.indexOf(val.charAt(0)) >= 0 ? "'" + val : val;
}
```

Cette séparation suit la même règle que pour le HTML : on valide les données à l'entrée sans les altérer en base, et on neutralise à la sortie selon le format cible (voir [Neutralisation des entrées utilisateur](/formation-api-rest/securite/neutralisation-entrees/)).
:::

### 4. Exposition dans le Controller

- Construction type-safe du `Content-Disposition` avec Spring (`StandardCharsets.UTF_8`)
- Spécification du type MIME `text/csv` *(Spring Boot gère déjà UTF-8 automatiquement sur un retour `String`)*

```java
@RestController
@RequestMapping("vin")
@RequiredArgsConstructor
public class VinController {

    private final VinService vinService;

    @GetMapping(value = "export", produces = "text/csv")
    public ResponseEntity<String> exportCsv() throws IOException {
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("vins.csv", StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(vinService.exportCsv());
    }
}
```

Test dans un navigateur : [http://localhost:8080/vin/export](http://localhost:8080/vin/export) déclenche directement le téléchargement du fichier `vins.csv`.

## Quand passer au streaming ?

**✅ Quand l'approche en mémoire suffit :**

- **Simple et lisible :** logique synchrone minimale, facile à tester.
- **Adaptée au quotidien :** idéale pour des tables de référence ou des listes filtrées (jusqu'à quelques milliers de lignes).

**⚠️ Ses limites sur gros volumes :**

- **Cumul en mémoire :** la requête charge toutes les entités JPA, le buffer `StringWriter` et la `String` finale en même temps en RAM.
- **Risque de saturation mémoire :** sur de gros volumes ou en cas de téléchargements simultanés, l'application peut déclencher une `OutOfMemoryError` et s'arrêter.

## Passer à l'échelle : export CSV en streaming

Pour contourner cette limite sur les gros volumes, on s'appuie sur le mécanisme présenté dans [Streaming de gros volumes (SQL → JSON)](/formation-api-rest/acces-donnees/streaming/) : un curseur PostgreSQL projeté en `Stream<VinResponse>` écrit directement dans l'`OutputStream` HTTP via `StreamingResponseBody`.

### 1. Adapter `CsvUtils` pour écrire dans un `OutputStream`

On ajoute une méthode `writeCsv` dans `CsvUtils` qui réutilise la même configuration (`createCsvWriter(Writer)`) et encapsule la boucle d'écriture dans l'`OutputStream` :

```java
// Méthode à ajouter dans CsvUtils (seule classe qui importe OpenCSV)
public static void writeCsv(OutputStream outputStream, String[] titres, Stream<String[]> lignes) throws IOException {
    try (ICSVWriter csvWriter = createCsvWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
        csvWriter.writeNext(titres);
        Iterator<String[]> iterator = lignes.iterator();
        while (iterator.hasNext()) {
            csvWriter.writeNext(iterator.next());
        }
        csvWriter.flush();
    }
}
```

### 2. Service transactionnel en flux

```java
@Service
@RequiredArgsConstructor
public class VinService {

    private static final String[] EN_TETES = {"ID", "CHATEAU", "APPELLATION", "ANNEE", "PRIX"};
    private final VinRepository vinRepository;

    @Transactional(readOnly = true)
    public void streamVinsAsCsv(OutputStream outputStream) throws IOException {
        // Curseur JDBC ouvert uniquement pendant le streaming dans le try-with-resources
        try (Stream<VinResponse> vinStream = vinRepository.streamAllDtos()) {
            CsvUtils.writeCsv(outputStream, EN_TETES, vinStream.map(this::toCsvRow));
        }
    }

    private String[] toCsvRow(VinResponse vin) {
        return new String[] {
            Objects.toString(vin.id(), ""),
            Objects.toString(vin.chateau(), ""),
            Objects.toString(vin.appellation(), ""),
            Objects.toString(vin.annee(), ""),
            Objects.toString(vin.prix(), "")
        };
    }
}
```

- **Aucun import OpenCSV dans `VinService` :** le service reste focalisé sur la transaction et le mapping métier (`toCsvRow` transforme chaque `VinResponse` en tableau de colonnes `String[]`).
- **Évaluation paresseuse :** `vinStream.map(this::toCsvRow)` ne charge rien en RAM ; chaque ligne est convertie et écrite une par une au fil de l'eau en **O(1)**.

### 3. Controller avec `StreamingResponseBody` et en-têtes horodatés

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
                // Content-Type: text/csv; charset=UTF-8
                // Content-Disposition: attachment; filename="vins_20261001.csv"
                .headers(exportFileUtils.buildCsvHeaders("vins"))
                .body(responseBody);
    }
}
```

```java
@Component
public class ExportFileUtils {
    private static final DateTimeFormatter DATE_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv", StandardCharsets.UTF_8);

    // Génère les en-têtes HTTP standards pour forcer le téléchargement côté client
    public HttpHeaders buildCsvHeaders(String nomFichierBase) {
        String horodatage = LocalDate.now().format(DATE_SUFFIX);
        String nomFichierComplet = String.format("%s_%s.csv", nomFichierBase, horodatage);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(nomFichierComplet, StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(CSV_MEDIA_TYPE);
        headers.setContentDisposition(contentDisposition);
        return headers;
    }
}
```

- **`Content-Disposition: attachment`** : indique au navigateur de télécharger le fichier au lieu de l'afficher dans l'onglet.
- **Horodatage du fichier** : évite les collisions de nom lors des téléchargements successifs (ex. `vins_20261001.csv`).
