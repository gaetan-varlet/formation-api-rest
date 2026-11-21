---
title: "Le protocole HTTP"
---

## Définition

HTTP (Hypertext Transfer Protocol) est un protocole de communication client-serveur développé pour le World Wide Web. Il est utilisé pour échanger toute sorte de données entre client HTTP et serveur HTTP.

Il s'agit d'un protocole **sans état**, ce qui signifie que le serveur ne conserve aucune donnée entre deux requêtes.

![Schéma illustrant les échanges entre le client et le serveur](../../../assets/images/client-serveur.svg "Schéma illustrant les échanges entre le client et le serveur")

## Les méthodes de requêtes (ou verbes) HTTP

HTTP définit un [ensemble de méthodes](https://developer.mozilla.org/fr/docs/Web/HTTP/M%C3%A9thode) de requête qui indiquent l'action que l'on souhaite réaliser sur la ressource indiquée :

| Méthode | Rôle | Corps de requête |
| --- | --- | --- |
| **GET** | Récupère une représentation de la ressource (lecture seule) | Non |
| **HEAD** | Identique à `GET`, mais sans le corps de la réponse (en-têtes uniquement) | Non |
| **POST** | Envoie une entité vers la ressource (création ou effet de bord sur le serveur) | Oui |
| **PUT** | Remplace toutes les représentations actuelles de la ressource visée | Oui |
| **PATCH** | Applique des modifications partielles à une ressource | Oui |
| **DELETE** | Supprime la ressource indiquée | Non |
| **OPTIONS** | Décrit les options de communication avec la ressource visée (utilisé pour CORS) | Non |
| **CONNECT** | Établit un tunnel vers le serveur identifié par la ressource cible | Non |
| **TRACE** | Réalise un message de test aller/retour en suivant le chemin de la ressource | Non |

## La requête HTTP

```http
Méthode URL Version-du-protocole
En-têtes de requête

Corps de requête
```

- **Méthode** : précise le type de requête (`HEAD`, `GET`, `POST`…)
- **URL** : adresse de la ressource sur le serveur
- **Version du protocole HTTP** (ex. `HTTP/1.1`, `HTTP/2`)
- **En-têtes** sous la forme `clé: valeur` ([liste des headers sur MDN](https://developer.mozilla.org/fr/docs/Web/HTTP/Headers)) :
  - **Host** spécifie le nom de domaine du site
  - **Cookie** permet d'envoyer au serveur les cookies enregistrés sur le poste du client à la demande du serveur
  - **Content-Type** spécifie le type MIME du corps de la requête
  - **Content-Length** spécifie la longueur du corps de la requête
  - **Accept** spécifie le type de contenu accepté par le client (par exemple `text/html` ou `application/json`)
- **Corps de la requête** : optionnel, peut par exemple contenir les données d'un formulaire HTML passé en `POST` sous la forme `variable1=valeur1&variable2=valeur2` ou un document JSON

### Exemple de requête HTTP

```http
GET / HTTP/1.1
Host: developer.mozilla.org
Accept-Language: fr
```

```http
POST / HTTP/1.1
Host: foo.com
Content-Type: application/x-www-form-urlencoded
Content-Length: 13

say=Hi&to=Mom
```

Dans une requête `POST`, il faut spécifier le type de données. Pour un formulaire HTML, il y a 2 valeurs possibles :

- `application/x-www-form-urlencoded` (valeur par défaut)
- `multipart/form-data` (utilisé pour l'envoi de fichiers)

Il faut également spécifier la longueur du corps (`Content-Length`).

### Structure d'une URL d'API HTTP

```http
https://mon-api/{variable-de-chemin}?parametre-de-requete-1=valeur&parametre-de-requete-2=valeur
```

- **Variables de chemin** : souvent utilisées pour les identifiants obligatoires.
- **Paramètres de requête** :
  - `?` à la fin de l'URL suivi de `clé=valeur`, puis `&` pour séparer les autres paramètres ;
  - souvent utilisés pour les options non obligatoires, notamment le filtrage ou la pagination.

:::tip[Exemple concret : API Sirene (Insee)]
```http
# Sans paramètre : retourne l'historique complet du SIREN
GET https://api.insee.fr/entreprises/sirene/V3/siren/{siren}

# Avec paramètre de requête : filtre uniquement la période à cette date
GET https://api.insee.fr/entreprises/sirene/V3/siren/{siren}?date={date}
```
:::

## La réponse HTTP

```http
Version Code-réponse Texte-réponse
En-têtes de réponse

Corps de réponse
```

- **Version HTTP** du serveur
- **Code-réponse** : le code retourné, par exemple `200`, `404`, `500`
- **Texte-réponse** : le libellé associé, par exemple `OK`, `Not Found`, `Internal Server Error`
- **En-têtes** :
  - **Date** : date de génération de la réponse
  - **Server** : spécifie le serveur HTTP utilisé
  - **Set-Cookie** : demande au navigateur d'enregistrer un cookie
- **Corps de la réponse** : contient les données renvoyées (HTML, JSON, fichier…)

### Exemple de réponse HTTP

```http
HTTP/1.1 200 OK
Date: Sat, 09 Oct 2010 14:28:02 GMT
Server: Apache
Last-Modified: Tue, 01 Dec 2009 20:18:22 GMT
ETag: "51142bc1-7449-479b075b2891b"
Accept-Ranges: bytes
Content-Length: 29769
Content-Type: text/html

<!DOCTYPE html>... (suivi des 29769 octets de la page web demandée)
```

### Les codes de statut de réponse HTTP

Voir la [documentation Mozilla des statuts HTTP](https://developer.mozilla.org/fr/docs/Web/HTTP/Status) :

| Famille | Signification | Exemples courants |
| --- | --- | --- |
| **1xx** | Information | `100 Continue`, `101 Switching Protocols` |
| **2xx** | Succès | `200 OK`, `201 Created`, `204 No Content` |
| **3xx** | Redirection | `301 Moved Permanently`, `304 Not Modified` |
| **4xx** | Erreur du client | `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |
| **5xx** | Erreur du serveur | `500 Internal Server Error`, `502 Bad Gateway`, `503 Service Unavailable` |

## L'en-tête Content-Type

- L'en-tête HTTP **`Content-Type`** :
  - indique au client le format de ce qu'il reçoit : `text/html` (HTML), `text/plain` (texte brut), `application/pdf` (PDF), `application/json` (JSON), `application/xml` (XML). On parle de **type MIME** ([liste des types MIME](https://developer.mozilla.org/fr/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types)) ;
  - précise aussi le jeu de caractères (**charset**) que le navigateur doit utiliser pour afficher le contenu (historiquement *ISO-8859-1* / *Latin-1* en HTML ; en JSON, UTF-8 est toujours la norme).

:::tip[Bonne pratique : UTF-8 partout]
Utilisez systématiquement **UTF-8** pour l'encodage de vos fichiers sources et pour l'en-tête `Content-Type` des réponses HTTP.
:::

Exemple pour ajouter ces en-têtes en Java :

```java
response.setContentType("text/html");

response.setContentType("text/html;charset=UTF-8");

response.setContentType("text/html");
response.setCharacterEncoding("UTF-8");
```
