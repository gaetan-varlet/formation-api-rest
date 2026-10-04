# Architecture

----

## L'architecture REST

2 grands types d'architectures pour les API :

- **SOAP** (Simple Object Access Protocol) dévelopé par Microsoft
- **REST** créé en 2000 par Roy Fielding dans sa thèse

API REST :

- doit être sans état, ou **stateless** en anglais : aucune donnée n'est conservée par le serveur entre 2 requêtes. Cela peut permettre de traiter les requêtes via des instances de multiples serveurs
- **modèle de maturité de Richardson** : quatre grands niveaux d’évaluation d’une API (dernier niveau => API RESTful)

----

![Le modèle de maturité de Richardson](diapos/images/modele-maturite-richardson.jpg "Le modèle de maturité de Richardson")

----

- **Niveau 0 — Le tunnel HTTP :**  
  HTTP sert uniquement de protocole de transport. L'API utilise un endpoint unique (ex. `/api`) et souvent une seule méthode (généralement `POST`) pour exécuter des fonctions distantes (style SOAP)

- **Niveau 1 — Les ressources (URI distinctes) :**  
  Découpage de l'application en entités individuelles adressables par une URI propre (ex. `/clients/42`, `/commandes/15`). En revanche, les verbes et codes HTTP ne sont pas encore normalisés (ex. utilisation de `POST /clients/42/supprimer`)

- **Niveau 2 — Les verbes et statuts HTTP (Standard actuel) :**  
  Utilisation standard de la sémantique HTTP :
  - Verbes adaptés aux opérations : `GET` (lecture), `POST` (création), `PUT` / `PATCH` (mise à jour), `DELETE` (suppression)
  - Codes retour explicites (`200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `404 Not Found`)

- **Niveau 3 — Les contrôles hypermédias (HATEOAS) :**  
  *Hypermedia As The Engine Of Application State*. La réponse contient les données brutes **et** des liens navigables indiquant les actions possibles depuis l'état courant

----

## Architecture monolithique VS architecture découplée

![Architecture monolithique VS architecture découplée](diapos/images/archi-legacy-vs-api.jpg "Architecture monolithique VS architecture découplée")

----

## Intérêts : rapidité, partage d'informations instantané entre applications

- application plus fluide, rafraichissement d'une partie de la page au lieu du chargement complet de la page
- partage d'informations : appel de l'API plutôt que des échanges de fichiers
  - pas d'attente de recevoir un fichier pour avoir une information à jour
  - pas besoin de gérer la lecture d'un fichier et l'import de données
  - pas de redondance de données : économie de stockage et pas d'écarts entre les différentes sources
