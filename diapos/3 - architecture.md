# Architecture

----

## L'architecture REST

- 2 grands types d'architectures pour les API :
    - **SOAP** (Simple Object Access Protocol) dévelopé par Microsoft 
    - **REST** créé en 2000 par Roy Fielding dans sa thèse

REST :
- doit être sans état, ou **stateless** en anglais : aucune donnée n'est conservée par le serveur entre 2 requêtes. Cela peut permettre de traiter les requêtes via des instances de multiples serveurs

![Le modèle de maturité de Richardson](diapos/images/modele-maturite-richardson.jpg "Le modèle de maturité de Richardson")

----

## L'architecture d'une application web classique à l'Insee

![Architecture legacy](diapos/images/archi-legacy.png "Architecture legacy")

----

## L'architecture d'une application basée sur une API

![Architecture API](diapos/images/archi-api.png "Architecture API")

- JavaScript pour le 'front-ent'
- API Java pour le 'back-end' 

----

## Intérêts : rapidité, partage d'informations instantané entre applications

- démo avec mode ralenti dans Chrome
