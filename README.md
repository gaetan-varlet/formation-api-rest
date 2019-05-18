# Formation API REST

:arrow_forward: [Diaporama](https://gaetan-varlet.github.io/formation-api-rest/)

## TODO

PARTIE SPRING BOOT
- tuto microservices :
    - https://openclassrooms.com/fr/courses/4668056-construisez-des-microservices
    - https://openclassrooms.com/fr/courses/4668216-optimisez-votre-architecture-microservices
- désactiver keycloak avec le profil local : https://stackoverflow.com/questions/47861513/spring-boot-how-to-disable-keycloak
- actuator :
    - secure endpoints
    - jmx vs http
- sortir la partie queryDsl et EntityManager de la formation pour le mettre dans une formation Spring Data
- ordre du filtre spring security - keycloak ?
- vérifier si on a besoin de toutes les variables dans la déclaration des profils
- lire la doc de querydsl pour faire des requêtes de type group by

PARTIE CONSOMMATION API avec RestTemplate :
    - https://www.baeldung.com/rest-template
    - https://o7planning.org/fr/11647/exemple-spring-boot-restful-client-avec-resttemplate
    - https://howtodoinjava.com/spring-restful/spring-restful-client-resttemplate-example/


## annotations

- @Repository : cette annotation est appliquée à la classe afin d'indiquer à Spring qu'il s'agit d'une classe qui gère les données, ce qui nous permettra de profiter de certaines fonctionnalités comme les translations des erreurs. Nous y reviendrons.
- @Service
- @Component
