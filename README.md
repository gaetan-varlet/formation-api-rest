# Formation API REST

:arrow_forward: [Diaporama](https://gaetan-varlet.github.io/formation-api-rest/)

TODO :

- ajouter le plugin pour le formattage de code
- déploiement sur Kubernetes
- diapo `/3/5` : ajouter sur le schéma un serveur Apache pour modéliser une appli JS
- diapo `/4/1` sur la mise en place d'une appli Java EE
  - passer à Java 21 et Tomcat 10
  - vérfier les versions de bibilothèques utilisées dans ce projet
  - tester si l'artifact maven fonctionne toujours, voir s'il n'y en a pas un plus récent pour faire du java 17-21 ?
  - passer à Spring Boot ?
- diapos `/11/...` sur les filtres
  - supprimer filtre dynamique
  - utiliser DTO à la place
- diapos `/12/...` sur les requêtes SQL spéciales
  - ajouter un premier exemple avec @Query en JPQL, puis en nativeQuery, et un exemple avec `@Modifying`
- diapo `/14/5` sur la création de fichier CSV
  - mettre en avant la méthode fourni par spring avec ResponseEntity au lieu d'utiliser HttpServletResponse

A creuser :

- utilisation d'**OpenFeign** pour faire des requêtes HTTP
- base postgre en mémoire (chercher si c'est possible)
- nouveautés Spring Boot 3 à étudier :
  - Spring Boot Observation
  - @HttpExchange : @GetExchange...
- vérifier le fonctionnement de Spring Actuator avec Spring Boot 3
- queryDSL
- utilisation de Spring Data Specifications
  - <https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications>
  - <https://reflectoring.io/spring-data-specifications/>
  - <https://www.baeldung.com/spring-data-criteria-queries>
