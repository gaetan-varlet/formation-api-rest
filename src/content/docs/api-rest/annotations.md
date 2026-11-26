---
title: "Annotations Spring Web & formats"
---

## `@RequestMapping` : les bases

- **`value`** (ou **`path`**) permet de spécifier le ou les chemin(s) de l'URL, et **`method`** la ou les méthode(s) HTTP autorisées. Si `method` n'est pas précisé, **toutes les méthodes HTTP** (`GET`, `POST`, `PUT`, `DELETE`…) sont acceptées.

```java
@RequestMapping(value = "/vins", method = RequestMethod.GET)
@RequestMapping(value = { "/vins", "/catalogue" }, method = { RequestMethod.PUT, RequestMethod.POST })
```

- Le mapping peut encore être restreint en imposant un ou plusieurs en-têtes dans la requête avec **`headers`** :

```java
@RequestMapping(value = "/vins", headers = "key=val", method = RequestMethod.GET)
@RequestMapping(value = "/vins", headers = { "key1=val1", "key2=val2" }, method = RequestMethod.GET)
```

- Il existe un raccourci dédié pour chaque méthode HTTP (à privilégier au quotidien) :

```java
@GetMapping("/vins")     // équivalent à @RequestMapping(value = "/vins", method = RequestMethod.GET)
@PostMapping("/vins")    // équivalent à @RequestMapping(value = "/vins", method = RequestMethod.POST)
@PutMapping("/vins")     // équivalent à @RequestMapping(value = "/vins", method = RequestMethod.PUT)
@PatchMapping("/vins")   // équivalent à @RequestMapping(value = "/vins", method = RequestMethod.PATCH)
@DeleteMapping("/vins")  // équivalent à @RequestMapping(value = "/vins", method = RequestMethod.DELETE)
```

:::caution[Conflit de routes = échec au démarrage]
Il est impossible de mapper la même URL avec la même méthode HTTP sur deux méthodes Java différentes. L'application **refusera de démarrer** avec une erreur de type :  
`Ambiguous mapping. Cannot map 'vinController' method ... There is already 'vinController' bean method`
:::

## Négociation de format : `consumes` et `produces`

Une requête HTTP peut envoyer des données dans son corps (format précisé par l'en-tête **`Content-Type`**) et indiquer les formats qu'elle accepte en réponse (via l'en-tête **`Accept`**).

Les annotations `@RequestMapping` / `@PostMapping` / `@GetMapping` disposent des attributs **`consumes`** et **`produces`** pour restreindre ces formats :

```java
@PostMapping(
    consumes = "application/json",
    produces = { "application/json", "application/xml" }
)
```

- Si le `Content-Type` envoyé ne respecte pas `consumes` → erreur HTTP **`415 Unsupported Media Type`**.
- Si l'en-tête `Accept` demandé ne correspond pas à `produces` → erreur HTTP **`406 Not Acceptable`**.
- Sans précision, aucune restriction de format n'est appliquée.

## Retourner un objet au format XML

Par défaut, Spring Boot embarque Jackson uniquement pour le **JSON**. Pour exposer aussi du **XML** :

- **Avec JAXB (approche standard Java/Jakarta) :**
  - Annotez la classe de l'objet à sérialiser avec `@XmlRootElement` (par exemple sur `Vin`).
  - Il n'est pas possible de renvoyer directement une `List<Vin>` en XML avec JAXB seul : il faut encapsuler la liste dans un objet conteneur annoté `@XmlRootElement`.
  - Depuis Java 9+, JAXB n'est plus inclus dans le JDK ; il faut ajouter son implémentation dans `pom.xml` :

```xml title="pom.xml"
<!-- Dépendance JAXB (retirée du JDK depuis Java 9+) -->
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>3.0.2</version>
</dependency>
```

:::tip[Alternative Spring Boot : `jackson-dataformat-xml`]
En ajoutant la dépendance `com.fasterxml.jackson.dataformat:jackson-dataformat-xml`, Jackson sérialise automatiquement vos DTOs/Records **et vos listes** en XML sans avoir besoin d'ajouter `@XmlRootElement`.
:::

## Paramètres de requête (`@RequestParam`)

### Nom, caractère obligatoire et valeur par défaut

- Par défaut, le paramètre HTTP porte le même nom que la variable Java. Pour utiliser un nom différent dans l'URL, précisez **`value`** (ou `name`) :

```java
@RequestParam(value = "id") String vinId // l'attribut name fonctionne également
@RequestParam("id") String vinId         // raccourci quand seul value est précisé
```

- Par défaut, un `@RequestParam` est **obligatoire** : s'il est absent, Spring renvoie une erreur **`400 Bad Request`**.
- Pour le rendre optionnel, utilisez **`required = false`** (il vaudra `null` s'il n'est pas fourni) :

```java
@RequestParam(required = false) String id
```

- Vous pouvez aussi définir une **`defaultValue`** (ce qui rend automatiquement le paramètre optionnel) :

```java
@RequestParam(defaultValue = "test") String id
```

### Paramètres multiples et différence avec `@PathVariable`

- Récupérer tous les paramètres de requête dans une `Map` :

```java
@RequestParam Map<String, String> allParams
```

- Récupérer un paramètre passé plusieurs fois dans une `List` :

```java
@RequestParam List<String> id
```

```http
GET http://localhost:8080/foo?id=1&id=2
GET http://localhost:8080/foo?id=1,2,3
```

- Les paramètres de requête (`@RequestParam`) sont décodés selon les règles des formulaires URL (où `+` devient un espace), contrairement aux variables de chemin (`@PathVariable`) où `+` est conservé tel quel :

```http
GET http://localhost:8080/foos?id=ab+c  # @RequestParam -> "ab c"
GET http://localhost:8080/foos/ab+c     # @PathVariable -> "ab+c"
```

:::note[Variables de chemin optionnelles]
Il est techniquement possible de rendre une `@PathVariable` optionnelle (`required = false`), mais c'est déconseillé car cela provoque facilement des ambiguïtés et conflits entre routes.
:::
