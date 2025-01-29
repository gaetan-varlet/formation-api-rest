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

- **niveau 0** : API qu'on ne peut pas vraiment qualifié de REST, davantage proche de SOAP. Utilisation d'"HTTP" comme protocole d'échange. Un seul point d'entrée, comme `/api`, et une seule méthode HTTP, `POST`, pour toutes les requêtes
- **niveau 1** : chaque ressource doit être distinguée par une URI distincte
- **niveau 2** : utilisation des verbes HTTP et codes retours HTTP adéquats
- **niveau 3** : notion d'HATEOAS (Hypertext As The Engine Of Application State) : ajout de liens dans les ressources retournées par l'API (par exemple pour mettre à jour la ressource, ou avoir l'URI d'un objet lié)

----

## L'architecture d'une application web classique à l'Insee

![Architecture legacy](diapos/images/archi-legacy.png "Architecture legacy")

----

## L'architecture d'une application basée sur une API

**JavaScript** pour le 'front-ent', **API Java** pour le 'back-end'

![Architecture API](diapos/images/archi-api.png "Architecture API")

----

## Intérêts : rapidité, partage d'informations instantané entre applications

- application plus fluide, rafraichissement d'une partie de la page au lieu du chargement complet de la page
- partage d'informations : appel de l'API plutôt que des échanges de fichiers
  - pas d'attente de recevoir un fichier pour avoir une information à jour
  - pas besoin de gérer la lecture d'un fichier et l'import de données
  - pas de redondance de données : économie de stockage et pas d'écarts entre les différentes sources
