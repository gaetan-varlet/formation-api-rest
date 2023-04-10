Feature: Obtenir les vins

    Scenario: obtenir tous les vins quand il y en a en base
        Given des vins avec les attributs suivants
            | chateau   | appellation  | prix |
            | Château 1 | Saint-Julien | 10.5 |
            | Château 2 | Pomerol      | 25   |
            | Château 3 | Pomerol      | 30   |
        When je récupère tous les vins
        Then le nombre de vins est 3

    Scenario: obtenir tous les vins quand il y en 2 en base
        Given des vins avec les attributs suivants
            | chateau   | appellation  | prix |
            | Château A | Saint-Julien | 10.5 |
            | Château B | Pomerol      | 25   |
        When je récupère tous les vins
        Then le nombre de vins est 2
