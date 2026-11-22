---
title: "Surveiller votre application avec Spring Boot Actuator"
---

:::note[Contenu basé sur Spring Boot 2 — à actualiser]
**Spring Boot Actuator** reste l'outil de référence pour superviser une API en production, mais les exemples de cette page datent de **Spring Boot 2**. Certaines parties ont évolué dans les versions récentes (Spring Boot 3 / 4) :
- `/httptrace` et `HttpTraceRepository` ont été remplacés par `/httpexchanges` et `HttpExchangeRepository` ;
- les propriétés `info.*` nécessitent d'activer `management.info.env.enabled=true` pour apparaître dans `/info` (qui n'est plus exposé par défaut sans configuration) ;
- le plugin Git s'appelle désormais `io.github.git-commit-id:git-commit-id-maven-plugin`.

En cas de doute sur votre version, appuyez-vous sur la [documentation officielle Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html).
:::

**Spring Boot Actuator** est un module de Spring Boot qui permet de superviser l'application en production et d'exposer des métriques et indicateurs personnalisables.

```xml title="pom.xml"
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Une fois démarrée, la liste des endpoints exposés est accessible sur `http://localhost:8080/actuator`.

```properties title="application.properties"
# Affichage dans Swagger UI des endpoints d'Actuator
springdoc.show-actuator=true
```

## Le service `/health`

- L'URL `/health` indique l'état de santé de l'application (`UP` avec un code HTTP **`200 OK`**, ou `DOWN` avec un code **`503 Service Unavailable`**).
- Actuator agrège le résultat de tous les beans implémentant **`HealthIndicator`**. Pour ajouter votre propre vérification (par exemple vérifier que la base de données contient bien des données), implémentez la méthode `health()` :

```java title="DatabaseHealthIndicator.java"
@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.down().withDetail("problème de BDD", "pas de données").build();
    }
}
```

```properties title="application.properties"
# Permet de renommer l'URL /health en /healthcheck
management.endpoints.web.path-mapping.health=healthcheck
# Affiche les détails de santé dans la réponse JSON
management.endpoint.health.show-details=always
```

## Le service `/info`

### Enrichissement via `application.properties`

Vide par défaut, `/info` permet d'exposer des informations sur l'application (version, contexte…) via des clés commençant par `info.` :

```properties title="application.properties"
maproperty-toto=dada

info.toto=Gaetan
# Récupérer une propriété système ou issue de /env
info.java-version=${java.version}
info.app.version=2.0
# Référencer une autre propriété applicative
info.app.toto=${maproperty-toto}
# Injecter une propriété Maven
info.app.version-spring=@parent.version@
```

### Enrichissement programmatique (`InfoContributor`)

Il est également possible d'enrichir `/info` dynamiquement (par exemple à partir d'un appel à un service ou à la base de données) en implémentant **`InfoContributor`** et en surchargeant `contribute(Info.Builder builder)` :

```java title="ExampleInfoContributor.java"
@Component
public class ExampleInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("nbUsers", 4);
        builder.withDetail("example", Collections.singletonMap("key", "value"));
    }
}
```

### Informations du dernier commit Git

Ajoutez le plugin Git commit dans le `pom.xml` pour exposer les informations du dernier commit dans `/info` :

```xml title="pom.xml"
<!-- Récupère les informations Git pour l'endpoint /info -->
<plugin>
    <groupId>pl.project13.maven</groupId>
    <artifactId>git-commit-id-plugin</artifactId>
</plugin>
```

```properties title="application.properties"
# Affiche l'ensemble des informations du commit dans /info
management.info.git.mode=full
```

## Activer et exposer les autres endpoints

Par défaut, seuls les endpoints autorisés sont exposés sur le web ; les autres doivent être explicitement ouverts dans `application.properties` :

```properties title="application.properties"
# Expose tous les endpoints
management.endpoints.web.exposure.include=*

# Ou expose uniquement une liste explicite
management.endpoints.web.exposure.include=health,info,metrics

# Expose tous les endpoints sauf loggers
management.endpoints.web.exposure.include=*
management.endpoints.web.exposure.exclude=loggers

# Active explicitement un endpoint désactivé par défaut
management.endpoint.shutdown.enabled=true
```

## Les autres endpoints disponibles

- `/metrics` : liste des métriques système et applicatives.
- `/beans` : liste de tous les beans gérés par le contexte Spring.
- `/env` : propriétés de configuration actives et variables d'environnement.
- `/loggers` : niveaux de logs configurés (et modifiables à chaud).
- `/httptrace` *(remplacé par `/httpexchanges` depuis Spring Boot 3)* : historique des 100 dernières requêtes-réponses HTTP.
- `/shutdown` : permet d'arrêter proprement l'application (requête `POST`, à activer explicitement avec `management.endpoint.shutdown.enabled=true`).

## Personnaliser les traces HTTP (`HttpTraceRepository`)

Sous Spring Boot 2, il est possible d'implémenter `HttpTraceRepository` pour intercepter et personnaliser le suivi des requêtes HTTP :

```java title="LogConf.java"
@Component
public class LogConf implements HttpTraceRepository {

    @Override
    public List<HttpTrace> findAll() {
        // Endpoint de consultation : ici on ne stocke rien en mémoire
        return List.of();
    }

    @Override
    public void add(HttpTrace trace) {
        // Appelé à chaque requête HTTP : affichage des métadonnées (sans les corps de requête/réponse)
        System.out.println("request (headers) : " + trace.getRequest().getHeaders());
        System.out.println("request (method) : " + trace.getRequest().getMethod());
        System.out.println("request (endpoint) : " + trace.getRequest().getUri().getPath());
        System.out.println("request (query) : " + trace.getRequest().getUri().getQuery());
        System.out.println("response (status) : " + trace.getResponse().getStatus());
        System.out.println("response (headers) : " + trace.getResponse().getHeaders());
        System.out.println("heure GMT : " + trace.getTimestamp());
        System.out.println("temps de réponse (en ms) : " + trace.getTimeTaken());
        // Si vous avez besoin des corps HTTP, utilisez plutôt une bibliothèque dédiée comme Logbook
    }
}
```

## Créer des endpoints personnalisés

Exemple d'un endpoint personnalisé exposé sur `/actuator/test` :

```java title="TestEndpoint.java"
@Component
@Endpoint(id = "test")
public class TestEndpoint {

    private final List<String> liste = new ArrayList<>();

    @ReadOperation
    public List<String> getValeurs() {
        liste.add("toto");
        return liste;
    }
}
```

## Pour aller plus loin

- [Documentation officielle Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)
- [Guide Spring Boot Actuator — Baeldung](https://www.baeldung.com/spring-boot-actuators)
