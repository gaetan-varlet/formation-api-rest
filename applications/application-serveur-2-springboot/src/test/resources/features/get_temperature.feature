Feature: Récupération de la température

    Scenario: récupération de la température
        When je récupère la température
        Then la température suivante est renvoyée : 5
