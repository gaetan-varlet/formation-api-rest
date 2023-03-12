Feature: Créer des vins

    Scenario Outline: créer un vin avec des informations erronées (<testCase>)
        Given je veux créer un vin avec les attributs suivants
            | chateau   | appellation   | prix   |
            | <chateau> | <appellation> | <prix> |
        When je récupère tous les vins
        Then le nombre de vins est 0
        Then j'ai le message d'erreur suivant "<message>"
        Examples:
            | testCase                     | chateau                                             | appellation | prix | message                                                                                                                                    |
            | nom de château trop long     | 123456789012345678901234567890123456789012345678901 | toto        | 5    | le vin renseigné (Vin [id=null, chateau=123456789012345678901234567890123456789012345678901, appellation=toto, prix=5.0]) n'est pas valide |
            | nom de château non renseigné |                                                     | toto        | 5    | le vin renseigné (Vin [id=null, chateau=null, appellation=toto, prix=5.0]) n'est pas valide                                                |

    Scenario Outline: créer un vin avec des informations justes
        Given je veux créer un vin avec les attributs suivants
            | chateau   | appellation   | prix   |
            | <chateau> | <appellation> | <prix> |
        When je récupère tous les vins
        Then le nombre de vins est 1
        Then le vin du chateau "<chateau>" de l'appellation "<appellation>" à <prix>€ est renvoyé
        Examples:
            | chateau   | appellation | prix |
            | Chateau 1 | app1        | 5    |
            | Chateau 2 | toto        | 1.23 |
