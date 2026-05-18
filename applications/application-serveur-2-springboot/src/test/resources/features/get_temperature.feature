Feature: Récupération de la température

    Scenario: récupération de la température
        Given La température dans la cave est 5
        When je récupère la température
        Then la température suivante est renvoyée : 5
