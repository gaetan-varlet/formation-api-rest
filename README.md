# Formation API REST

:arrow_forward: [Diaporama](https://gaetan-varlet.github.io/formation-api-rest/)

## TODO

- réponse en Jersey en UTF-8 : tester sur un poste Windows du texte avec accent avec et sans UTF-8 dans le `@Produces` (`@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")`) et voir si c'est propre
- Jackson convertit que les objets Java dans l'exemple avec la servlet ?
- on ne peut pas convertir une liste d'objet Java en XML ? il faut le wrapper dans un objet conteneur
- blocage avec augmentation de la version de Jersey ?
- choix dans la requête via un en-tête de la préférence entre XML et JSON pour la réponse

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
    - Utilisation de [JSONPlaceholder](https://jsonplaceholder.typicode.com/)
    - Installation d'un plugin JsonViewer
    - Via le navigateur (démo)
    - En utilisant postman (démo)
    - En Java sans bibliothèque (TP)
    - En Java avec des bibliothèques (exemple avec Unirest et OkHTTP) (TP)
    - En JavaScript avec XMLHttpRequest, JQuery, Fetch, Axios (TP)

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
