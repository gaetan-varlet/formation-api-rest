# Consommation d'une API REST

----

## Faire une requête HTTP

- via la barre d'adresse du navigateur (uniquement requêtes GET)

- en utilisant des applications spécifiques telles que **Postman** ou **Advanced REST client**, qui sont liées au navigateur Chrome, ou **RESTClien** lié au navigateur Firefox

- avec la documentation interactive **Swagger** d'une API. Par exemple : `http://fakerestapi.azurewebsites.net/swagger/`

- en Java : par exemple, consommer une API dans son API, dans son application web

- en JavaScript : consommer son API pour faire une IHM en JavaScript

- en bash : `curl https://jsonplaceholder.typicode.com/posts/1`

- en Libre Office

- en SAS

- etc...

----

## En Java

- sans bibliothèque en utilisant une **HttpURLConnection** et en lisant la réponse dans un **InputStream**, puis en utlisant **JAXB** pour lire les réponses XML et **Jackson** pour lire les réponses JSON
- avec des bibliothèques :
    - OkHTTP, Jersey Client, Rest Template...

----

## En JavaScript

- XMLHttpRequest
- Jquery
- Axios
- Fetch