# Formation API REST

:arrow_forward: [Diaporama](https://gaetan-varlet.github.io/formation-api-rest/)

## TODO

- blocage avec augmentation de la version de Jersey : cf changelog
- lecture pour conso API REST en Java :
    - Java et Jersey : http://blog.bdoughan.com/2010/08/creating-restful-web-service-part-55.html
    - RestTemplate :
        - https://www.baeldung.com/rest-template
        - https://o7planning.org/fr/11647/exemple-spring-boot-restful-client-avec-resttemplate
        - https://howtodoinjava.com/spring-restful/spring-restful-client-resttemplate-example/

## Plan

1. Introduction
	- Qu'est ce qu'un service web ? (définition et exemple)
    - Les formats de données XML et JSON

2. Le protocole HTTP
    - Définition
    - Les méthodes de requêtes (ou verbes) HTTP
    - La requête
    - La réponse
    - Les codes de statut de réponse HTTP
    - L'en-tête Content-Type
    - Structure d'une requête HTTP

3. Architecture 
    - L'architecture REST
    - L'architecture d'une application web classique à l'Insee
    - L'architecture d'une application basée sur une API
    - Intérêts : rapidité, partage d'informations instantané entre applications
    
4. Un premier exemple de mise en place d'une API en Java EE
    - Création d'une application web classique avec Maven
    - Création d'une première servlet avec JSP
    - Création d'une seconde servlet sans JSP
    - Création d'une troisième servlet sous forme d'API
    - Création d'une quatrième servlet sous forme d'API
    - Création d'une cinquième servlet avec conversion automatique au format JSON
    - Présentation de la spécification JAX-RS et exemple d'utilisation de l'implémentation Jersey

5. Consommation d'une API REST
    - Faire une requête HTTP
        - passage en revue des différentes manière de faire une requête HTTP
    - En Java
        - sans bibliothèque
        - avec des bibliothèques
    - En Javascript

6. Création d'une API REST avec Spring Boot
    - découverte rapide de l'univers Spring, de Spring Core, Spring Data, Spring Security, Spring MVC, Spring Boot
    - Spring Initializr
    - création d'un jar vs war
    - création d'un RestController HelloWorld
    - présentation du projet et des ressources à créer : objet Vin, GET (avec filtre sur un attribut via param de requête), POST, PUT, DELETE
    - produces / consumes
    - reception de fichier dans un service
    - mise en place de Spring Data JPA avec H2
    - paging et sorting
    - configuration de spring boot avec des profils
    - injection des propriétés
    - gestion des logs(à travailler)
    - bannière ASCII
    - gestion des erreurs avec exceptions et codes HTTP
    - mise en place de Swagger
    - les tests dans Spring Boot
    - Spring Security : Authentification basique, Keycloak
    - Cors
    - gestion du cache applicatif

7. Création d'une API REST avec Jersey
    - passage en revue rapide des différences avec Spring Boot

8. Documentation
    - Note de la cellule archi
        - Utilisation des verbes
        - Singulier / Pluriel
        - Numéro de version
