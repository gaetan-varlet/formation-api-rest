# Formation API REST

:arrow_forward: [Diaporama](https://gaetan-varlet.github.io/formation-api-rest/)

## TODO

- réponse en Jersey en UTF-8 : tester sur un poste Windows du texte avec accent avec et sans UTF-8 dans le `@Produces` (`@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")`) et voir si c'est propre

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

3. Architecture 
    - L'architecture REST (et différences avec une application web classique)
    - L'architecture classique d'une application web à l'Insee
    - La nouvelle architecture avec un 'front-ent' JavaScript et une API Java pour le 'back-end'
    - Intérêts : rapidité (démo avec mode ralenti dans Chrome), partage d'informations instantané entre applications
    
2. Un premier exemple de mise en place d'une API
    - Création d'une application web classique avec Maven (TP)
    - Présentation de la norme JAX-RS
    - Transformation de l'applicaation web en API (TP)
    - Utilisation de frameworks :
        - Mise en place d'un projet avec Jersey 2 (TP)
        - Mise en place d'un projet avec Spring Boot (TP)

3. Consommation de webservices
    - Utilisation de [JSONPlaceholder](https://jsonplaceholder.typicode.com/)
    - Installation d'un plugin JsonViewer
    - Via le navigateur (démo)
    - En utilisant postman (démo)
    - En Java sans bibliothèque (TP)
    - En Java avec des bibliothèques (exemple avec Unirest et OkHTTP) (TP)
    - En JavaScript avec XMLHttpRequest, JQuery, Fetch, Axios (TP)

4. Aller plus loin dans la mise en place de webservice
    - Structure d'une requête HTTP (pathparam, queryparam, avec les ? et &)
    - Les paramètres de requête, le corps de la requête (TP)
    - Personnalisation de la réponse (TP)

5. Sécurité
    - Https
    - Authentification basique
    - Keyclock
    - Cors

6. Intégration au SI
    - Les tests (TP)
    - Documenter l'API avec Swagger (TP)
    - Note de la cellule archi
        - Utilisation des verbes
        - Singulier / Pluriel
        - Numéro de version