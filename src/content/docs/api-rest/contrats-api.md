---
title: "Contrats d’API : DTO et validation"
---

## Masquer un champ sur l'entité : `@JsonIgnore`

Une première approche consiste à annoter directement l'entité JPA pour exclure certains champs de la sérialisation :

- **`@JsonIgnore`** (Jackson) exclut un attribut de la sérialisation et de la désérialisation JSON (sur le champ ou le getter).
- **`@JsonIgnoreProperties`** (sur la classe) permet d'exclure plusieurs champs d'un coup, par exemple `@JsonIgnoreProperties({ "id", "prix" })`.
- **`@XmlTransient`** est l'équivalent Jakarta XML / JAXB (à placer sur le getter).

```java title="Vin.java"
public class Vin {
    private Long id;
    private String chateau;
    private String appellation;

    @JsonIgnore
    private Double prix;

    @XmlTransient
    public Double getPrix() {
        return prix;
    }
}
```

:::caution[Limite de cette approche]
Annoter l'entité JPA pollue le modèle de persistance avec des préoccupations HTTP et **fige une seule représentation** pour toute l'API (impossible d'avoir une vue simplifiée pour une liste et une vue détaillée ailleurs, ou de masquer un champ en écriture tout en l'exposant en lecture).
:::

## Le pattern DTO : découpler l'API de la base

*Objectif : séparer ce que l'API expose de ce que la base de données stocke.*

Un **DTO** (*Data Transfer Object*) est un objet simple dédié uniquement aux échanges entre le client et le contrôleur :

- **Contrat entrant (*Request*)** : données acceptées en écriture (création, mise à jour).
- **Contrat sortant (*Response*)** : données restituées en lecture.

### Pourquoi ne pas exposer directement les entités JPA ?

- **Sécurité (*Mass Assignment*)** : empêche un client de forcer ou d'écraser des champs internes (identifiant généré, statut métier, rôle, date d'audit…).
- **Évolutivité de l'API** : le schéma SQL et les entités JPA peuvent évoluer (renommage de colonne, refactorisation) sans casser le contrat des clients existants.
- **Fiabilité technique** : évite les boucles infinies de sérialisation Jackson sur les relations bidirectionnelles et élimine le risque de `LazyInitializationException` lors de la sérialisation JSON.

## Modéliser les contrats avec les `record` Java

En Java moderne, le **`record`** est idéal pour définir un DTO : immuable, concis, muni d'un constructeur canonique, d'accesseurs et supporté nativement par Jackson.

**Contrat entrant :** aucun identifiant technique n'est accepté à la création :

```java title="CreateVinRequest.java"
public record CreateVinRequest(
    String chateau,
    String appellation,
    Double prix
) {}
```

**Contrat sortant :** données garanties au client par l'API :

```java title="VinResponse.java"
public record VinResponse(
    Long id,
    String chateau,
    String appellation,
    Double prix
) {}
```

## Conventions de nommage : `Request` et `Response`

Plutôt que d'utiliser un suffixe générique `...Dto`, nommer les contrats selon **le sens du flux et l'intention métier** rend l'API immédiatement compréhensible :

- **Flux entrants (requêtes HTTP) :**
  - Création : `CreateVinRequest`
  - Mise à jour : `UpdateVinRequest`
- **Flux sortants (réponses HTTP) :**
  - Vue détaillée / unitaire : `VinResponse` ou `VinDetailResponse`
  - Vue de synthèse / liste : `VinSummaryResponse` ou `VinItemResponse`

## Valider les données entrantes

### Limites du contrôle manuel dans le service

Sans validation déclarative, on est tenté de multiplier les `if` dans le service (comme vu au début du chapitre [Gestion des erreurs](/formation-api-rest/api-rest/gestion-erreurs/)) :

```java
public VinResponse create(CreateVinRequest request) {
    if (request.prix() <= 0) {
        throw new IllegalArgumentException("Le prix doit être positif");
    }
    // ... autres contrôles, un par un
}
```

Cette approche présente plusieurs défauts :
- **Risque d'erreur 500** : un `NullPointerException` au lieu d'un `400 Bad Request`.
- **Erreur unique** : le traitement s'arrête au **premier** champ invalide ; le client corrige, renvoie la requête, puis découvre l'erreur suivante.
- **Duplication et couplage** : les règles de format sont enfouies dans le code métier et répétées entre création et mise à jour.
- **Rejet tardif** : la requête a déjà atteint la couche service (et parfois ouvert une transaction) avant d'être refusée.

### Validation déclarative avec `@Valid` (Bean Validation)

Ajoutez le starter Bean Validation dans `pom.xml` :

```xml title="pom.xml"
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Déclarez les contraintes directement sur le contrat d'entrée :

```java title="CreateVinRequest.java"
public record CreateVinRequest(
    @NotBlank @Size(max = 100) String chateau,
    @NotBlank String appellation,
    @NotNull @Positive Double prix
) {}
```

Déclenchez la validation dans le contrôleur avec `@Valid` :

```java title="VinController.java"
@PostMapping
public ResponseEntity<VinResponse> create(@Valid @RequestBody CreateVinRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(vinService.create(request));
}
```

Si la requête est invalide, Spring MVC lève une `MethodArgumentNotValidException` et renvoie immédiatement une **erreur `400 Bad Request`**, avant même d'entrer dans le contrôleur ou le service.

### Avantages, limites et pièges

**Pourquoi `@Valid` sur le DTO est préférable :**
- **Toutes** les erreurs de validation sont collectées et remontées en une seule fois (ex. château vide *et* prix négatif).
- **Rejet précoce** : aucune transaction ouverte, aucun appel inutile au repository.
- **Contrat auto-documenté** : les contraintes sont lisibles directement sur le `record` et reprises automatiquement dans la documentation OpenAPI par `springdoc-openapi`.

:::caution[Pièges fréquents avec Bean Validation]
- **`@Positive` (comme `@Size`, `@Min`…) accepte `null` :** il faut toujours le coupler avec `@NotNull` si le champ est obligatoire.
- **Sans `spring-boot-starter-validation`, `@Valid` est ignoré silencieusement :** aucune erreur au démarrage, aucune validation à l'exécution.
- **Actif uniquement sur les requêtes HTTP :** un batch, un scheduler ou un listener qui appelle directement `VinService` ne passe pas par le contrôleur. Pour activer la validation aussi sur un service, annotez la classe avec `@Validated` et le paramètre avec `@Valid`.
- **Ne remplace pas les règles métier nécessitant la base :** `@Valid` détecte un prix négatif, pas un doublon en base. Vérifier « ce vin existe déjà » reste le rôle du service (erreur métier → **`409 Conflict`**).
:::

## Convertir entre DTO et entité : le Mapper

Le **Mapper** assure la conversion entre les contrats d'API (`Request` / `Response`) et les entités JPA.

### Mapper manuel

Simple, explicite et sans magie pour démarrer :

```java title="VinMapper.java"
@Component
public class VinMapper {

    public Vin toEntity(CreateVinRequest request) {
        Vin vin = new Vin();
        vin.setChateau(request.chateau());
        vin.setAppellation(request.appellation());
        vin.setPrix(request.prix());
        return vin;
    }

    public VinResponse toResponse(Vin vin) {
        return new VinResponse(
            vin.getId(),
            vin.getChateau(),
            vin.getAppellation(),
            vin.getPrix()
        );
    }
}
```

### Évolution : générer les mappers avec MapStruct

**MapStruct** génère l'implémentation du mapper **à la compilation** à partir d'une simple interface (zéro réflexion au runtime) :

- Attributs de **même nom** : associés automatiquement.
- Nom différent : on précise la correspondance avec `@Mapping` (par exemple ci-dessous si `VinResponse` exposait `nomChateau` au lieu de `chateau`).
- Attribut cible oublié : **warning** à la compilation (ou **erreur** avec `unmappedTargetPolicy = ReportingPolicy.ERROR`).

```java title="VinMapper.java"
@Mapper(componentModel = "spring")
public interface VinMapper {

    @Mapping(target = "id", ignore = true)
    Vin toEntity(CreateVinRequest request);

    @Mapping(source = "chateau", target = "nomChateau")
    VinResponse toResponse(Vin entity);
}
```

:::caution[MapStruct et Lombok]
MapStruct nécessite de déclarer son processeur d'annotations (`mapstruct-processor`) dans la configuration du `maven-compiler-plugin`. Si le projet utilise aussi **Lombok**, l'ordre des processeurs dans le `pom.xml` est important (avec `lombok-mapstruct-binding`), sans quoi MapStruct risque de ne pas détecter les getters et setters générés par Lombok.
:::

## Orchestrer les échanges dans le Service

La couche Service reçoit le contrat entrant, manipule l'entité pour la persistance, puis renvoie le contrat sortant. Le contrôleur ne manipule ainsi jamais d'entité JPA directement :

```java title="VinService.java"
@Service
@RequiredArgsConstructor
public class VinService {

    private final VinRepository vinRepository;
    private final VinMapper vinMapper;

    public VinResponse create(CreateVinRequest request) {
        // 1. Contrat entrant -> Entité
        Vin entity = vinMapper.toEntity(request);
        // 2. Persistance
        Vin saved = vinRepository.save(entity);
        // 3. Entité -> Contrat sortant
        return vinMapper.toResponse(saved);
    }
}
```
