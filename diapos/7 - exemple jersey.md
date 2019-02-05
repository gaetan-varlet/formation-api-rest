# Spring Boot et Jax-RS, les principales différences

----

## Les différences sur les principales annotations
- même principe que Spring Boot mais annotations différentes
    - Spring Boot -> annotations Spring
    - Jersey -> annotations `javax.ws.rs`
- @RequestMapping, @GetMapping -> @Path + @GET, @POST...
- @RequestParam -> @QueryParam
- @PathVariable -> @PathParam

----

## Ajout d'une dépendance pour monter la version de Jersey à partir de la 2.26

- [Release Notes Jersey 2.26](https://jersey.github.io/release-notes/2.26.html)
- le framework d'injection HK2 était inclus dans Jersey Core jusqu'à la version 2.25.1
- il est sorti du core à partir de la version 2.26 pour laisser le choix au client de son framework d'injection, il faut donc ajouter explicitement la dépendance HK2 pour passer au delà de la version 2.25.1
- on passe donc de deux dépendances à trois