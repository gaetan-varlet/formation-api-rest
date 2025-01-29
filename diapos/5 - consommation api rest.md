# Consommation d'une API REST

----

## Faire une requête HTTP

- avec le navigateur, via la barre d'adresse (uniquement requêtes GET)
- en utilisant des applications spécifiques telles que **Postman** ou **Advanced REST client**, qui sont liées au navigateur Chrome, ou **RESTClient** lié au navigateur Firefox
- avec la documentation interactive **Swagger** d'une API. Par exemple : `http://fakerestapi.azurewebsites.net/`
- en Java : par exemple, consommer une API dans son API ou dans son application web
- en JavaScript : consommer son API pour faire une IHM en JavaScript
- en bash : `curl https://jsonplaceholder.typicode.com/posts/1`
- en Libre Office
- en SAS
- etc...

----

## En JavaScript

- XMLHttpRequest
- Jquery
- Axios
- Fetch

----

## En Java

- sans bibliothèque en utilisant une **HttpURLConnection** et en lisant la réponse dans un **InputStream**, puis en utlisant **JAXB** pour lire les réponses XML et **Jackson** pour lire les réponses JSON
- avec HttpClient de Java 11
- avec des bibliothèques : OkHTTP, Jersey Client, Web Client...

----

### Mise en place

Lecture du service :

```http
http://fakerestapi.azurewebsites.net/api/v1/Users/1
```

réponse en JSON :

```json
{
    "id": 1,
    "userName": "User 1",
    "password": "Password1"
}
```

réponse en XML :

```xml
<User xmlns:i="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.datacontract.org/2004/07/FakeRestAPI.Web.Models">
    <id>1</id>
    <password>Password1</password>
    <userName>User 1</userName>
</User>
```

----

Création d'un projet Maven en Java 17 avec la dépendence **jackson-databind**, et création d'un Bean **User** :

```java
@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.FIELD)
public class User {

    @XmlElement(name = "id")
    @JsonProperty("id")
    private int id;
    @XmlElement(name = "userName")
    @JsonProperty("userName")
    private String userName;
    @XmlElement(name = "password")
    @JsonProperty("password")
    private String password;

    // getters, setters et méthode toString()
}
```

----

Création d'un fichier **package-info.java** pour lire la réponse XML :

```java
@XmlSchema(
    namespace="http://schemas.datacontract.org/2004/07/FakeRestAPI.Web.Models",
    elementFormDefault=XmlNsForm.QUALIFIED
)
package model;
 
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
```

----

### Requête HTTP en GET sans bibliothèque

```java
System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr");
System.setProperty("http.proxyPort", "8080");

// requête en GET avec réponse en XML
URL url = new URL("http://fakerestapi.azurewebsites.net/api/v1/Users/1");
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Accept", "application/xml");
InputStream response = connection.getInputStream();
JAXBContext jc = JAXBContext.newInstance(User.class);
User user = (User) jc.createUnmarshaller().unmarshal(response);
connection.disconnect();
System.out.println(user);
System.out.println(connection.getResponseCode()); // 200
System.out.println(connection.getContentType()); // application/xml; charset=utf-8

// requête en GET avec réponse en JSON
URL url = new URL("http://fakerestapi.azurewebsites.net/api/v1/Users/1");
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Accept", "application/json");
InputStream response = connection.getInputStream();
ObjectMapper mapper = new ObjectMapper();
User user = mapper.readValue(response, User.class);
connection.disconnect();
System.out.println(user);
```

----

## Requête HTTP en POST sans bibliothèque

```java
User user = new User(); user.setUserName("toto"); user.setPassword("azerty");
ObjectMapper mapper = new ObjectMapper();
URL url = new URL("http://fakerestapi.azurewebsites.net/api/v1/Users");
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("POST");
connection.setRequestProperty("Accept", "application/json");
connection.setRequestProperty("Content-type", "application/json");
connection.setDoOutput(true); //this is to enable writing
mapper.writeValue(connection.getOutputStream(), user);

InputStream response = connection.getInputStream();
BufferedReader reader = new BufferedReader(new InputStreamReader(response,Charset.defaultCharset()));
String line = null;
while ((line=reader.readLine()) != null) {
    System.out.println(line);
}
reader.close();
connection.disconnect();
```

----

### Requête GET synchrone avec HttpClient

```java
public static void requeteGetSynchroneXmlJson(String accept) throws Exception {
    // requête en GET avec réponse en XML ou JSON que l'on récupère dans un String
    HttpClient httpClient = HttpClient.newBuilder()
        .proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080)))
        .build();
    HttpRequest httpRequest = HttpRequest.newBuilder().header("Accept", accept)
        .uri(URI.create("http://fakerestapi.azurewebsites.net/api/v1/Users/1")).GET().build();
    HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
    System.out.println(response.statusCode()); // 200
    System.out.println(response.headers().allValues("content-type")); // [application/xml; charset=utf-8] ou [application/json; charset=utf-8]
    System.out.println(response.body()); // le User en XML ou JSON
    // même chose avec récupération du body dans un InputStream
    HttpResponse<InputStream> responseStream = httpClient.send(httpRequest, BodyHandlers.ofInputStream());
    String contentType = responseStream.headers().firstValue("Content-Type").get();
    if (contentType.contains("application/xml")) {
        JAXBContext jc = JAXBContext.newInstance(User.class);
        User user = (User) jc.createUnmarshaller().unmarshal(responseStream.body());
        System.out.println(user); // User [id=1, userName=User 1, password=Password1]
    } else if (contentType.contains("application/json")) {
        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.readValue(responseStream.body(), User.class);
        System.out.println(user); // User [id=1, userName=User 1, password=Password1]
    } else {
        throw new Exception("On veut du XML ou du JSON !");
    }
}
```

----

### Requête POST synchrone avec HttpClient

```java
ObjectMapper mapper = new ObjectMapper();
User user = new User(); user.setUserName("toto"); user.setPassword("azerty");
String userString = mapper.writeValueAsString(user);

HttpClient httpClient = HttpClient.newBuilder()
    .proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080)))
    .build();
HttpRequest httpRequest = HttpRequest.newBuilder()
    .header("Accept", "application/json")
    .header("Content-type", "application/json")
    .uri(URI.create("http://fakerestapi.azurewebsites.net/api/v1/Users"))
    .POST(BodyPublishers.ofString(userString)).build();
HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
System.out.println(response.body());
System.out.println("");

HttpResponse<InputStream> responseStream = httpClient.send(httpRequest, BodyHandlers.ofInputStream());
BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream.body(), Charset.defaultCharset()));
String line = null;
while ((line=reader.readLine()) != null) {
    System.out.println(line);
}
```

----

### Requête GET asynchrone avec HttpClient

```java
HttpClient httpClient = HttpClient.newBuilder()
    .proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080)))
    .build();
HttpRequest httpRequest = HttpRequest.newBuilder().header("Accept", "application/json")
    .uri(URI.create("http://fakerestapi.azurewebsites.net/api/v1/Users/1")).GET().build();
System.out.println(LocalDateTime.now()); // 15:44:19.690822
CompletableFuture<HttpResponse<String>> cfResponse = httpClient.sendAsync(httpRequest, BodyHandlers.ofString());
System.out.println(LocalDateTime.now()); // 15:44:19.717822
HttpResponse<String> response = cfResponse.join();
System.out.println(LocalDateTime.now()); // 15:44:19.876822
System.out.println(response.statusCode()); // 200
System.out.println(response.headers().allValues("content-type")); // [application/xml; charset=utf-8] ou [application/json; charset=utf-8]
System.out.println(response.body()); // le User en XML ou JSON
```

----

### Multiples requêtes GET asynchrones avec HttpClient

```java
HttpClient httpClient = HttpClient.newBuilder()
    .proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080)))
    .build();
List<String> urls = List.of(
    "http://fakerestapi.azurewebsites.net/api/v1/Users/1",
    "http://fakerestapi.azurewebsites.net/api/v1/Users/2",
    "http://fakerestapi.azurewebsites.net/api/v1/Users/3",
    "http://fakerestapi.azurewebsites.net/api/v1/Users/4",
    "http://fakerestapi.azurewebsites.net/api/v1/Users/5");
System.out.println(LocalDateTime.now()); // 16:03:28.104822
List<CompletableFuture<HttpResponse<String>>> cfResponses = urls.stream()
.map(url -> httpClient.sendAsync(
    HttpRequest.newBuilder().header("Accept", "application/json").uri(URI.create(url)).GET().build(),
    BodyHandlers.ofString())
).collect(Collectors.toList());
System.out.println(LocalDateTime.now()); // 16:03:28.137822
// on attend d'avoir toutes les réponses
CompletableFuture<Void> conbinedCF = CompletableFuture.allOf(cfResponses.toArray(new CompletableFuture[cfResponses.size()]));
conbinedCF.join();
System.out.println(LocalDateTime.now()); // 16:03:28.319822
for (CompletableFuture<HttpResponse<String>> completableFuture : cfResponses) {
    System.out.println(completableFuture.join().body());
}
System.out.println(LocalDateTime.now()); // 16:03:28.319822
```

----

### Requête vers API sécurisée en mode Basic

```java
String idMdp = "ID" + ":" + "MDP";
HttpClient httpClient = HttpClient.newBuilder()
    .proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080)))
    .build();
HttpRequest httpRequest = HttpRequest.newBuilder()
    .header("Accept", "application/json")
    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(idMdp.getBytes()))
    .uri(URI.create("URL_API")).GET().build();
HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
System.out.println(response.statusCode());
System.out.println(response.headers().allValues("content-type"));
System.out.println(response.body());
```

----

### Requête vers API sécurisée en mode Bearer

```java
HttpClient httpClient = HttpClient.newBuilder()
    .proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080)))
    .build();
HttpRequest httpRequest = HttpRequest.newBuilder()
    .header("Accept", "application/json")
    .header("Authorization", "Bearer " + "CONTENU_BEARER")
    .uri(URI.create("URL_API")).GET().build();
HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
System.out.println(response.statusCode());
System.out.println(response.headers().allValues("content-type"));
System.out.println(response.body());
```
