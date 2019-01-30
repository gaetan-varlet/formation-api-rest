# Spring Boot et Jax-RS, les principales différences

----

## Jersey, implémentation de référence de la spécification JAX-RS
- même principe que Spring Boot mais annotations différentes
    - Spring Boot -> annotations Spring
    - Jersey -> annotations `javax.ws.rs`
- @RequestMapping, @GetMapping -> @GET, @POST...
- @RequestParam -> @QueryParam
- @PathVariable -> @PathParam