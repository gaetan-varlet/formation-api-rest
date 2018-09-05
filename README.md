# Formation Webservices

:arrow_forward: [Diaporama](https://gaetan-varlet.github.io/formation-webservices/)

## Plan

1. Qu'est ce que c'est ?
	- exemple
    - le protocole HTTP (requête/réponse, code retour HTTP)
    - les formats de données XML et JSON
    - différence avec une application web classique
    - l'architecture REST
    - l'architecture classique d'une application web à l'Insee
    - la nouvelle architecture Insee avec une application JavaScript et une API en Java
    - les verbes HTTP [descriptif MDN](https://developer.mozilla.org/fr/docs/Web/HTTP/M%C3%A9thode)

2. Un premier exemple de mise en place d'une API
    - un premier exemple en Java EE (TP)
    - présentation de la norme JAX-RS
    - utilisation de frameworks :
        - mise en place d'un projet avec Jersey 2 (TP)
        - mise en place d'un projet avec Spring Boot (TP)

3. Consommation de webservices
    - utilisation de [JSONPlaceholder](https://jsonplaceholder.typicode.com/)
    - installation d'un plugin JsonViewer
    - via le navigateur (démo)
    - en utilisant postman (démo)
    - en Java sans bibliothèque (TP)
    - en Java avec des bibliothèques (exemple avec Unirest et OkHTTP) (TP)
    - en JavaScript avec XMLHttpRequest, JQuery, Fetch, Axios (TP)

4. Aller plus loin dans la mise en place de webservice
    - structure d'une requête HTTP (pathparam, queryparam, avec les ? et &)
    - les paramètres de requête, le corps de la requête (TP)
    - personnalisation de la réponse (TP)

5. Sécurité
    - https
    - authentification basique
    - keyclock
    - cors

6. Intégration au SI
    - les tests (TP)
    - documenter l'API avec Swagger (TP)
    - note de la cellule archi
        - utilisation des verbes
        - singulier / pluriel
        - numéro de version