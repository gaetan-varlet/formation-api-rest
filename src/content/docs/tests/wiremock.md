---
title: "Tester les appels à des API externes"
---

Du principe de simulation (mock HTTP) à la mise en pratique avec WireMock

## Le problème : tester un client d'API externe

Une application consomme souvent des API tierces ou d'autres services via HTTP.

**Appeler le service réel dans des tests automatisés pose plusieurs problèmes :**

- **Instabilité** : dépendance au réseau, indisponibilités temporaires, latence variable.
- **Non-déterminisme** : données qui évoluent, effets de bord (création, modification, suppression).
- **Cas limites difficiles à provoquer** : erreurs `500`, `404`, timeouts, réponses malformées.
- **Contraintes d'environnement** : quotas d'appels, authentification, accès coupé en intégration continue (CI).

## Le principe du mock d'API externe

**Objectif :** valider *notre* code d'intégration HTTP (construction de l'URL, en-têtes, sérialisation, gestion des codes retour et des erreurs) en isolant le service distant.

- **Mock unitaire (ex. Mockito)** : remplace une classe Java (`TemperatureService`), mais **ne teste pas** la couche réseau ni le parsing HTTP.
- **Mock serveur HTTP** : démarre un **vrai serveur HTTP local** qui intercepte les requêtes et renvoie des réponses scénarisées (*stubs*).

```text
[TemperatureService] ── HTTP ──▶ [Serveur Mock Local (localhost:8082)]
                                        └─ Réponse configurée (200, 404, 500, délai...)
```

## Panorama des alternatives en Java / Spring

**Intercepteurs en mémoire (sans port réseau)**
- **MockRestServiceServer** *(Spring Test)* : natif Spring pour `RestClient` / `RestTemplate`, mais incompatible avec `HttpClient` du JDK.

**Serveurs HTTP locaux**
- **JDK `HttpServer`** : intégré au JDK (**zéro dépendance**), mais routage et assertions à coder à la main.
- **OkHttp `MockWebServer`** : léger et rapide, matching de requêtes limité.
- **WireMock** : matching riche, scénarios et simulation de latence/pannes, mais dépendances lourdes.
- **Testcontainers + MockServer** : isolé dans un conteneur (multi-langages), nécessite Docker.

## Pourquoi choisir WireMock ?

- **Agnostique du client HTTP** : fonctionne avec `HttpClient` JDK, `RestClient`, `WebClient`, `Feign`, etc.
- **Matching précis des requêtes** : URL, méthode HTTP, headers, paramètres de requête, corps JSON/XML.
- **Simulation de pannes réalistes** : latence réseau, coupure de connexion, réponses corrompues, enchaînement d'états (*scenarios*).
- **Intégration directe** avec JUnit 5, Spring Boot et Cucumber.

## Les limites de WireMock : ne pas l'utiliser par défaut

WireMock est puissant, mais **ce n'est pas toujours le bon choix** :

- **Arbre de dépendances transitives lourd** : embarque un serveur Jetty complet, Jackson, Guava, etc.
- **Surface d'exposition aux CVE** : plus de bibliothèques tierces = plus d'alertes de sécurité et de montées de version à maintenir.
- **Surdimensionné pour des besoins simples** : pour simuler 1 ou 2 endpoints basiques sans matching avancé, le `HttpServer` natif du JDK ou `MockRestServiceServer` évitent toute dépendance supplémentaire.

## Mise en place (1/6) — Dépendance et configuration

Ajout de la dépendance Maven (`pom.xml`) :

```xml
<dependency>
    <groupId>org.wiremock.integrations</groupId>
    <artifactId>wiremock-spring-boot</artifactId>
    <version>4.4.3</version>
    <scope>test</scope>
</dependency>
```

Externalisation de l'URL de l'API consommée (`application.properties`) :

```properties
temperature.base-url=[https://ma-cave.fr](https://ma-cave.fr)
```

## Mise en place (2/6) — Le service à tester

```java
@Slf4j
@Component
public class TemperatureService {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofMillis(5_000))
            .build();

    @Value("${temperature.base-url}")
    private String baseUrl;

    public Double getTemperature() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/temperature"))
                .GET()
                .build();
        log.debug("requête de récupération de la température : {}", httpRequest);
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        log.debug("code réponse HTTP : {}", response.statusCode());
        log.debug("body réponse HTTP : {}", response.body());
        return Double.valueOf(response.body());
    }
}
```

## Mise en place (3/6) — Configuration de WireMock

Démarrage et arrêt du serveur HTTP local autour de chaque scénario :

```java
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Slf4j
public class WireMockConf implements En {
    private WireMockServer wireMockServer;
    public WireMockConf() {
        Before(() -> {
            wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8082));
            wireMockServer.start();
            configureFor("localhost", 8082);
            log.debug("démarrage de wireMockServer");
        });

        After(() -> {
            if (wireMockServer != null) {
                log.debug("arrêt de wireMockServer");
                wireMockServer.stop();
            }
        });
    }
}
```

## Mise en place (4/6) — Surcharge de l'URL en test

Redirection de `TemperatureService` vers le serveur WireMock local :

```java
// ajout en plus des properties existantes
@TestPropertySource(properties = {
        "temperature.base-url=http://localhost:8082",
})
public class ContextConfigurationIT {
}
```

Le code exécuté est strictement identique à celui de production : seule l'URL cible est modifiée.

## Mise en place (5/6) — Écriture du scénario en Gherkin

```gherkin
Feature: Récupération de la température

    Scenario: récupération de la température
        Given La température dans la cave est 5
        When je récupère la température
        Then la température suivante est renvoyée : 5
```

## Mise en place (6/6) — Ecriture des Glue

```java
@Slf4j
public class TemperatureGlue implements En {
    private Double temperatureApi;
    private Double temperature;

    public TemperatureGlue(TemperatureService temperatureService) {
        Given("La température dans la cave est {double}", (Double temp) -> {
            temperatureApi = temp;
        });

        When("je récupère la température", () -> {
            try {
                stubFor(get("/temperature").willReturn(ok(temperatureApi.toString())));
                temperature = temperatureService.getTemperature();
            } catch (Exception e) {
                temperature = null;
                log.debug("ERREUR", e);
            }
        });

        Then("la température suivante est renvoyée : {double}", (Double expectedContent) -> {
            Assertions.assertThat(temperature).isEqualTo(expectedContent);
        });
    }
}
```

## Aller plus loin avec WireMock

- **Simuler des erreurs HTTP** : `stubFor(get("/temperature").willReturn(serverError()))` ou `status(404)`.
- **Tester les timeouts** : `.withFixedDelay(6_000)` pour vérifier le comportement du client en cas de lenteur.
- **Inspecter les requêtes envoyées (`verify`)** : utile surtout pour les `POST`/`PUT` sans corps de réponse, la vérification de headers ou le comptage d'appels (cache)
- **Éviter les conflits de ports en CI** : utiliser `options().dynamicPort()` (ou l'annotation `@EnableWireMock` de `wiremock-spring-boot`).
