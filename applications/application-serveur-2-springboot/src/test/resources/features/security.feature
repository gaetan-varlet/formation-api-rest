Feature: Tester les droits d'accès

    Scenario: impossibilité d'accéder au endpoint /hello-secured sans jeton
        When je fais une requête HTTP en GET sur l'url "/hello-secured"
        Then j'obtiens un code retour HTTP 401

    @WithRoleToto
    Scenario: impossibilité d'accéder au endpoint /hello-secured avec un mauvais rôle
        When je fais une requête HTTP en GET sur l'url "/hello-secured"
        Then j'obtiens un code retour HTTP 403

    @WithRoleAdmin
    Scenario: impossibilité d'accéder au endpoint /hello-secured avec le rôle admin
        When je fais une requête HTTP en GET sur l'url "/hello-secured"
        Then j'obtiens un code retour HTTP 200