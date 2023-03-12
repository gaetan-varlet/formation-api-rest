Feature: Obtenir les vins

    Background: création d'un jeu de données pour vérifier les données renvoyées
        Given des vins avec les attributs suivants
            | chateau   | appellation  | prix |
            | Château 1 | Saint-Julien | 10.5 |
            | Château 2 | Pomerol      | 25   |
            | Château 3 | Pomerol      | 30   |

    Scenario: obtenir tous les vins quand il y a un vin en base
        When je récupère tous les vins
        Then le nombre de vins est 3
        Then le vin du chateau "Château 1" de l'appellation "Saint-Julien" à 10.5€ est renvoyé
        Then le vin du chateau "Château 2" de l'appellation "Pomerol" à 25€ est renvoyé
        Then le vin du chateau "Château 3" de l'appellation "Pomerol" à 30€ est renvoyé

    Scenario Outline: obtenir tous les vins d'une appellation existante
        When je récupère tous les vins de l'appellation "<appellation>"
        Then le nombre de vins est <nombre_vins>
        Then le vin du chateau "<chateau>" de l'appellation "<appellation>" à <prix>€ est renvoyé
        Examples: appellation existante
            | appellation  | nombre_vins | chateau   | prix |
            | Saint-Julien | 1           | Château 1 | 10.5 |
            | Pomerol      | 2           | Château 2 | 25   |
            | Pomerol      | 2           | Château 3 | 30   |

    Scenario Outline: obtenir tous les vins d'une appellation qui n'existe pas
        When je récupère tous les vins de l'appellation "<appellation>"
        Then le nombre de vins est 0
        Examples: appellation inexistante
            | appellation |
            | toto        |
            | pomerol     |

    Scenario: obtenir les vins quand la base est vide
        Given il n'y a pas de données en base
        When je récupère tous les vins
        Then le nombre de vins est 0