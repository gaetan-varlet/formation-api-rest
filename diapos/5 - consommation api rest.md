# Consommation d'une API REST

----

## Faire une requête HTTP
- avec le navigateur, via la barre d'adresse (uniquement requêtes GET)
- en utilisant des applications spécifiques telles que **Postman** ou **Advanced REST client**, qui sont liées au navigateur Chrome, ou **RESTClient** lié au navigateur Firefox
- avec la documentation interactive **Swagger** d'une API. Par exemple : `http://fakerestapi.azurewebsites.net/swagger/`
- en Java : par exemple, consommer une API dans son API ou dans son application web
- en JavaScript : consommer son API pour faire une IHM en JavaScript
- en bash : `curl https://jsonplaceholder.typicode.com/posts/1`
- en Libre Office
- en SAS
- etc...

----

## En Java

- sans bibliothèque en utilisant une **HttpURLConnection** et en lisant la réponse dans un **InputStream**, puis en utlisant **JAXB** pour lire les réponses XML et **Jackson** pour lire les réponses JSON
- avec des bibliothèques : OkHTTP, Jersey Client, Rest Template...

----

### Mise en place

Lecture du service :
```http
http://fakerestapi.azurewebsites.net/api/Users/1
```

réponse en JSON :
```json
{
    "ID": 1,
    "UserName": "User 1",
    "Password": "Password1"
}
```

réponse en XML :
```xml
<User xmlns:i="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.datacontract.org/2004/07/FakeRestAPI.Web.Models">
    <ID>1</ID>
    <Password>Password1</Password>
    <UserName>User 1</UserName>
</User>
```

----

Création d'un projet Maven en java 8 avec la dépendence **jackson-databind**, et création d'un Bean **User** :
```java
@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.FIELD)
public class User {

	@XmlElement(name = "ID")
	@JsonProperty("ID")
	private int id;
	@XmlElement(name = "UserName")
	@JsonProperty("UserName")
	private String userName;
	@XmlElement(name = "Password")
	@JsonProperty("Password")
	private String password;

    // getters, setters et méthode toString()
}
```

----

Création dun **package-info.java** pour lire la réponse XML :
```java
@XmlSchema(
    namespace="http://schemas.datacontract.org/2004/07/FakeRestAPI.Web.Models",
    elementFormDefault=XmlNsForm.QUALIFIED
)
package test;
 
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
```

----

### Requête HTTP sans bibliothèque

```java
// requête en GET avec réponse en XML
URL url = new URL("http://fakerestapi.azurewebsites.net/api/Users/1");
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Accept", "application/xml");
InputStream response = connection.getInputStream();
JAXBContext jc = JAXBContext.newInstance(User.class);
User user = (User) jc.createUnmarshaller().unmarshal(response);
connection.disconnect();
System.out.println(user);

// requête en GET avec réponse en JSON
URL url = new URL("http://fakerestapi.azurewebsites.net/api/Users/1");
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

### avec OkHTTP (TODO)

----

###  avec Jersey Client (à creuser, ne fonctionne pas)
```xml
<dependency>
	<groupId>org.glassfish.jersey.core</groupId>
	<artifactId>jersey-client</artifactId>
	<version>2.27</version>
</dependency>
```

```java
Client client = ClientBuilder.newClient();
WebTarget target = client.target("http://fakerestapi.azurewebsites.net/api/Users/1").path("");
Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
User user = builder.get(User.class);
System.out.println(user);
```

----

### avec RestTemplate (Spring)

```xml
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-web</artifactId>
	<version>5.1.3.RELEASE</version>
</dependency>
```

```java
String url = "http://fakerestapi.azurewebsites.net/api/Users/1";

System.out.println("Demande de la réponse sous forme de String");
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
System.out.println(response.getStatusCode()); // 200 OK
System.out.println(response.getBody()); // {"ID":1,"UserName":"User 1","Password":"Password1"}
System.out.println(response.getHeaders().getContentType()); // application/json;charset=utf-8

System.out.println("Demande de la réponse sous forme de String au format XML"); // NE RENVOIE PAS DU XML !!!
HttpHeaders headers = new HttpHeaders();
headers.setContentType(org.springframework.http.MediaType.APPLICATION_XML);
headers.set("my_other_key", "my_other_value");
HttpEntity<String> entity = new HttpEntity<String>(headers);
restTemplate = new RestTemplate();
response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
System.out.println(response.getStatusCode());
System.out.println(response.getBody());
System.out.println(response.getHeaders().getContentType());

System.out.println("Convertion du body en objet");
ObjectMapper mapper = new ObjectMapper();
User user = mapper.readValue(response.getBody(), User.class);
System.out.println(user);

System.out.println("Demande de la réponse sous forme d'objet User");
restTemplate = new RestTemplate();
user = restTemplate.getForObject(url, User.class);
System.out.println(user);
```

----

## En JavaScript

- XMLHttpRequest
- Jquery
- Axios
- Fetch