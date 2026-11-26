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

Plutôt que de concaténer manuellement des valeurs séparées par `;` (ce qui casse dès qu'une donnée contient un `;`, des guillemets `"` ou un retour à la ligne), on s'appuie sur la bibliothèque **OpenCSV** pour produire un fichier conforme.

L'approche présentée ici génère le CSV simplement en mémoire : elle convient très bien aux **petits volumes** (jusqu'à quelques milliers de lignes). Pour des extractions volumineuses, il faudra basculer vers le [streaming](/formation-api-rest/fichiers-performance/streaming/).

## Implémentation pas à pas

### 1. Dépendance OpenCSV

Ajout de la bibliothèque **OpenCSV** dans le `pom.xml` pour gérer l'échappement des caractères réservés et les séparateurs :

```xml
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.12.0</version>
</dependency>
```

### 2. Classe utilitaire : CsvUtils

- Encapsule l'écriture du flux dans un `StringWriter`
- Configuration explicite du séparateur (`;`) et des guillemets d'échappement

```java
package fr.insee.formationapirest.util;
// import ...
public final class CsvUtils {
    private CsvUtils() {}

    public static String convertDataToString(String[] titres, List<String[]> donnees) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (ICSVWriter writer = constructionCsvWriter(stringWriter)) {
            writer.writeNext(titres);
            writer.writeAll(donnees);
        }
        return stringWriter.toString();
    }
    private static ICSVWriter constructionCsvWriter(Writer writer) {
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

## Compromis : Simplicité vs Scalabilité

### ✅ Idéal pour les petits volumes

- **Simple et lisible :** Logique synchrone minimale, facile à tester unitairement.
- **Suffisant au quotidien :** Idéal pour des tables de référence ou listes filtrées (< quelques milliers de lignes).

### ⚠️ Risques du tout-en-mémoire

- **Surcharge du Heap :** Cumul des entités JPA, du buffer `StringWriter` et de la `String` finale en RAM.
- **Risque d'OOM :** Crash rapide sur gros volumes ou requêtes concurrentes (notamment sous Kubernetes).

> **Vers le streaming :** Pour passer à l'échelle, on écrit directement dans le flux HTTP au fil de l'eau (section suivante).
