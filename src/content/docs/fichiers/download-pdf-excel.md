---
title: "Téléchargement PDF, Excel et Calc (Download)"
---

## Fichiers binaires : ce qui change par rapport au CSV

À la différence d'un [export CSV](/formation-api-rest/fichiers/download-csv/) qui renvoie du texte brut (`ResponseEntity<String>`), un `PDF`, un classeur Excel (`.xlsx`) ou LibreOffice Calc (`.ods`) est un **fichier binaire**.

Selon la provenance et la taille du fichier, Spring MVC propose trois types de retour :

- **`ResponseEntity<byte[]>` :** adapté aux petits fichiers générés à la volée en mémoire (facture PDF, export Excel de quelques centaines de lignes).
- **`ResponseEntity<Resource>` (`FileSystemResource`, `InputStreamResource`…) :** recommandé pour renvoyer un fichier déjà existant sur disque, dans le *classpath* ou depuis un [stockage objet (type S3)](/formation-api-rest/fichiers/stockage-s3/) sans tout charger dans un `byte[]`.
- **`ResponseEntity<StreamingResponseBody>` :** indispensable pour les fichiers très volumineux générés au fil de l'eau (voir [Streaming de gros volumes (SQL → JSON)](/formation-api-rest/acces-donnees/streaming/)).

| Format | Extension | `Content-Type` (`MediaType`) | `Content-Disposition` habituel |
| :--- | :--- | :--- | :--- |
| **PDF** | `.pdf` | `application/pdf` (`MediaType.APPLICATION_PDF`) | `inline` (aperçu navigateur) ou `attachment` (téléchargement) |
| **Excel** | `.xlsx` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | `attachment` |
| **LibreOffice Calc** | `.ods` | `application/vnd.oasis.opendocument.spreadsheet` | `attachment` |

## Exposition d'un fichier binaire dans le Controller

Le contrôleur reste identique dans son principe à celui du CSV : on construit l'en-tête `Content-Disposition` avec Spring (`StandardCharsets.UTF_8`), on précise le `Content-Type` et on retourne le tableau d'octets :

```java
@RestController
@RequestMapping("vin")
@RequiredArgsConstructor
public class VinController {

    private static final MediaType MEDIA_TYPE_XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final VinExportService vinExportService;

    @GetMapping(value = "export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf() throws IOException {
        byte[] pdf = vinExportService.genererCataloguePdf();
        ContentDisposition disposition = ContentDisposition.inline()
                .filename("catalogue-vins.pdf", StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("export/excel")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] xlsx = vinExportService.genererExcel();
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("vins.xlsx", StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MEDIA_TYPE_XLSX)
                .body(xlsx);
    }
}
```

- **`ContentDisposition.inline()` sur le PDF :** le navigateur affiche directement le PDF dans l'onglet tout en proposant son bouton de téléchargement sous le nom `catalogue-vins.pdf` *(passez à `.attachment()` si vous souhaitez forcer l'enregistrement sur disque)*.
- **`ContentDisposition.attachment()` sur l'Excel :** force le téléchargement de `vins.xlsx` pour ouverture dans Excel ou LibreOffice Calc.

## Générer un fichier Excel (`.xlsx`) ou Calc (`.ods`)

En pratique, le format **`.xlsx`** (Office Open XML) s'est imposé comme standard d'échange : il est lu et édité nativement aussi bien par **Microsoft Excel** que par **LibreOffice Calc**.

- **Besoin de données brutes sans mise en forme :** privilégiez le [CSV](/formation-api-rest/fichiers/download-csv/), beaucoup plus léger et rapide.
- **Besoin d'un classeur `.xlsx` (onglets, largeurs de colonnes, styles, formules) :**
  - **Apache POI (`org.apache.poi:poi-ooxml`, licence Apache 2.0) :** bibliothèque de référence la plus complète en Java pour lire, créer et modifier des fichiers Excel (`XSSFWorkbook` pour des classeurs petits à moyens ; `SXSSFWorkbook` en mode *streaming* pour éviter de saturer la mémoire sur gros volumes — pensez à appeler `dispose()` à la fin).
  - **FastExcel (`org.dhatim:fastexcel`, licence Apache 2.0) :** bibliothèque **indépendante d'Apache POI**, plus légère et rapide pour créer ou lire de gros exports `.xlsx` en flux *(elle ne permet en revanche pas d'ouvrir un fichier existant pour le modifier)*.
- **Format `.ods` (LibreOffice Calc) :** en pratique, on génère presque toujours du `.xlsx` (que LibreOffice Calc ouvre et édite nativement) ou du `.csv`. Si du `.ods` natif est strictement imposé, on passe plutôt par une conversion (`.xlsx` → `.ods`) via **LibreOffice** (en ligne de commande, sans interface graphique).

## Générer un fichier PDF (`.pdf`)

Deux approches répondent à des besoins différents :

### 1. Génération programmatique (`OpenPDF` / `Apache PDFBox`)

Adaptée aux documents simples (attestations, listes, bordereaux) ou à la manipulation de fichiers PDF existants en code Java :

- **OpenPDF (`com.github.librepdf:openpdf`) :** fork open source d'iText sous licence permissive **LGPL / MPL**. Son API de plus haut niveau (paragraphes, tableaux, sauts de page automatiques) la rend **plus simple pour créer un PDF simple de zéro**.
- **Apache PDFBox (`org.apache.pdfbox:pdfbox`) :** bibliothèque de la fondation Apache (**Apache 2.0**). Plus bas niveau pour dessiner une mise en page (placement manuel en coordonnées `x, y`), mais c'est la référence pour **manipuler des PDF existants** (fusionner, découper, extraire du texte, remplir un formulaire ou apposer un filigrane).

*(Vigilance sur les licences : évitez les versions modernes d'*iText*, soumises à une licence **AGPL** ou commerciale.)*

### 2. HTML + CSS vers PDF (`OpenHTMLToPDF` ou `Gotenberg`)

Dès que la mise en page devient riche (facture, rapport multi-pages, en-têtes/pieds de page, charte graphique), écrire des coordonnées et des paragraphes en Java devient vite pénible. Il est alors préférable de partir d'un **template HTML + CSS** (par exemple avec **Thymeleaf**) puis de le convertir en PDF :

- **`OpenHTMLToPDF` :** bibliothèque Java embarquée dans votre application (**zéro infrastructure supplémentaire** à déployer ni superviser), idéale pour des PDF propres ; en contrepartie, elle impose du XHTML strict et un CSS ancien (pas de Flexbox/Grid ni de JavaScript).
- **`Gotenberg` :** service externe open source (conteneur Docker embarquant **Chromium** et **LibreOffice**) que votre API Spring appelle en HTTP (`RestClient`) ; il offre un rendu CSS moderne complet (Flexbox, Grid, graphiques JS) et convertit aussi des documents Office (`.docx`, `.odt`, `.xlsx`, `.ods`) en PDF, au prix d'un **service supplémentaire à déployer et maintenir dans le SI**.

:::caution[Bonnes pratiques : échappement et polices en production]
- **Échappement des données :** si vous générez un PDF depuis un template HTML, veillez à échapper les saisies utilisateur (Thymeleaf le fait par défaut avec `th:text`, mais jamais avec `th:utext`). Voir [Neutralisation des entrées utilisateur](/formation-api-rest/securite/neutralisation-entrees/).
- **Polices de caractères :** les conteneurs Linux minimaux (Docker / Kubernetes) n'embarquent pas les polices bureautiques habituelles (Arial, Calibri…). Pensez à inclure vos polices (`.ttf` / `.otf`) dans le projet pour garantir un rendu identique entre votre poste et la production.
:::

## Synthèse : quel format et quel outil choisir ?

| Besoin | Format recommandé | Outil adapté |
| :--- | :--- | :--- |
| Export de données brutes vers tableur | **CSV** (`.csv`) | Voir [Téléchargement et export CSV](/formation-api-rest/fichiers/download-csv/) |
| Tableur avec mise en forme, onglets ou types | **Excel** (`.xlsx`) | **Apache POI** (`XSSFWorkbook` / `SXSSFWorkbook`) ou **FastExcel** *(Apache 2.0)* |
| Format OpenDocument strictement imposé | **Calc** (`.ods`) | Conversion depuis `.xlsx` via **LibreOffice** |
| PDF simple ou manipulation de PDF en Java pur | **PDF** (`.pdf`) | **OpenPDF** *(création)* ou **Apache PDFBox** *(manipulation)* |
| PDF complexe à mise en page riche | **PDF** (`.pdf`) via HTML/CSS | **Thymeleaf + OpenHTMLToPDF** *(embarqué)* ou **Gotenberg** *(service HTTP)* |
