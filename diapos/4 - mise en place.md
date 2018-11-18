# Mise en place

----

## Création d'une application web classique avec Maven

- créez un nouveau projet maven en choisissant l'option "skip archetype" et packaging "war"
- créez un fichier **web.xml** dans le dossier *src/main/webapp/WEB-INF/*
```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
         http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
	version="3.1">
</web-app>
```

----

- passez en Java 8 et ajoutez l'API Servlet dans le **pom.xml**

```xml
	<properties>
		<maven.compiler.source>1.8</maven.compiler.source>
		<maven.compiler.target>1.8</maven.compiler.target>
	</properties>

	<dependencies>
		<dependency>
			<groupId>javax.servlet</groupId>
			<artifactId>javax.servlet-api</artifactId>
			<version>3.1.0</version>
			<scope>provided</scope>
		</dependency>
	</dependencies>
```

----

### Création d'une première servlet avec JSP

- créez la servlet **CalculAvecJspServlet.java** dans le dossier *src/main/java*, dans le package *servlet* par exemple :

```java
package servlet;

@WebServlet("/calcul-avec-jsp")
public class CalculAvecJspServlet extends HttpServlet {
	
	public void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException{
		int nombre1 = Integer.parseInt(request.getParameter("nombre1"));
		int nombre2 = Integer.parseInt(request.getParameter("nombre2"));
		int somme = nombre1+nombre2;
		int produit = nombre1*nombre2;
		request.setAttribute( "somme", somme );
		request.setAttribute( "produit", produit );

		response.setContentType("text/html");
		response.setCharacterEncoding( "UTF-8" );
		this.getServletContext().getRequestDispatcher( "/WEB-INF/jsp/reponse-calcul.jsp" )
		.forward( request, response );
	}
}
```

----

- créez la JSP **reponse-calcul.jsp** dans le dossier */WEB-INF/jsp/*

```jsp
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Calcul avec JSP</title>
    </head>
    <body>
    	<p>Calcul avec affichage de la réponse dans la JSP :</p>
    	<p>La somme de ${param.nombre1} et ${param.nombre2} est égale à ${requestScope.somme}.</p>
    	<p>Le produit de ${param.nombre1} et ${param.nombre2} est égal à ${requestScope.produit}.</p>
    </body>
</html>
```

- déployez le projet à la racine de votre Tomcat 8 et appellez l'URL [http://localhost:8080/calcul-avec-jsp?nombre1=3&nombre2=4](http://localhost:8080/calcul-avec-jsp?nombre1=3&nombre2=4)

----

### Création d'une seconde servlet sans JSP

- créez la servlet **CalculServlet.java** dans le dossier *src/main/java*, dans le package *servlet*

```java
package servlet;

@WebServlet("/calcul")
public class CalculServlet extends HttpServlet {
	
	public void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException{
		int nombre1 = Integer.parseInt(request.getParameter("nombre1"));
		int nombre2 = Integer.parseInt(request.getParameter("nombre2"));
		int somme = nombre1+nombre2;
		int produit = nombre1*nombre2;
        ...
```

----

- suite de la servlet **CalculServlet.java**

```java
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("<meta charset=\"utf-8\" />");
		out.println("<title>Calcul</title>");
		out.println("</head>");
		out.println("<body>");
        out.println("<p>Calcul avec affichage de la réponse dans la servlet :</p>");
		out.println("<p>La somme de "+nombre1+" et "+nombre2+" est égale à "+somme+".</p>");
		out.println("<p>Le produit de "+nombre1+" et "+nombre2+" est égal à "+produit+".</p>");
		out.println("</body>");
		out.println("</html>");
	}
}
```

- relancez le Tomcat et appellez l'URL [http://localhost:8080/calcul?nombre1=3&nombre2=4](http://localhost:8080/calcul?nombre1=3&nombre2=4)

----

### Création d'une troisième servlet sous forme d'API

- créez la servlet **CalculTextPlainServlet.java** dans le dossier *src/main/java*, dans le package *servlet* :

```java
package servlet;

@WebServlet("/calcul-text-plain")
public class CalculTextPlainServlet extends HttpServlet {
	
	public void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException{
		int nombre1 = Integer.parseInt(request.getParameter("nombre1"));
		int nombre2 = Integer.parseInt(request.getParameter("nombre2"));
		int somme = nombre1+nombre2;
		int produit = nombre1*nombre2;

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println("somme:"+somme+", produit:"+produit);
	}
}
```

----

- relancez le Tomcat et appellez l'URL [http://localhost:8080/calcul-text-plain?nombre1=3&nombre2=4](http://localhost:8080/calcul-text-plain?nombre1=3&nombre2=4)

- compliqué à lire de manière automatique, on va plutôt utiliser un format tel que le XML ou le JSON

----

### Création d'une quatrième servlet sous forme d'API

- créez la servlet **CalculJsonServlet.java** dans le dossier *src/main/java*, dans le package *servlet* :

```java
@WebServlet("/calcul-json")
public class CalculJsonServlet extends HttpServlet {
	
	public void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException{
		int nombre1 = Integer.parseInt(request.getParameter("nombre1"));
		int nombre2 = Integer.parseInt(request.getParameter("nombre2"));
		int somme = nombre1+nombre2;
		int produit = nombre1*nombre2;

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println("{\"somme\":"+somme+", \"produit\":"+produit+"}");
	}
}
```

- relancez le Tomcat et appellez l'URL [http://localhost:8080/calcul-json?nombre1=3&nombre2=4](http://localhost:8080/calcul-json?nombre1=3&nombre2=4)

----

- créer un fichier **index.html** sur votre bureau

```html
<!DOCTYPE html>
<html>
<head>
    <title>Exercice JS</title>
    <meta charset="UTF-8">
</head>

<body>
    <div id="myDiv"></div>
    <script src="script.js"></script>
</body>
</html>
```

----

- créer un fichier **script.js** également sur votre bureau

```js
const req = new XMLHttpRequest()
const method = 'GET'
const url = 'http://localhost:8080/calcul-json?nombre1=3&nombre2=4'

req.onreadystatechange = function(event){
    if(this.readyState === XMLHttpRequest.DONE){// on aurait pu écrire 4 à la place, c'est pareil
        if(this.status === 200){
        document.getElementById('myDiv').innerHTML =
        '<p>La somme vaut '+JSON.parse(this.responseText).somme+'</p>'
        +'<p>Le produit vaut '+JSON.parse(this.responseText).produit+'</p>';
            console.log(
                JSON.parse(this.responseText)
            )
        } else {
            console.log("Statut Erreur : "+this.status)
        }
    }
}

req.open(method, url)
req.send()
```

- lancez dans votre navigateur le fichier *index.html*

----

### Création d'une cinquième servlet avec conversion automatique au format JSON

- transformer les objets Java en chaîne de caractères au format Json est fastidieux et source d'erreur. On peut utiliser une bibliothèque qui le fait pour nous : Jackson

```xml
<dependency>
	<groupId>com.fasterxml.jackson.core</groupId>
	<artifactId>jackson-databind</artifactId>
	<version>2.9.7</version>
</dependency>
```

- Jackson va transformer un objet au format JavaBean en un objet au format JSON. Il peut aussi transformer les collections :
```json
{"somme":15,"produit":56} // objet Java
[{"somme":15,"produit":56},{"somme":15,"produit":56}] // liste de 2 objets Java
```

----

- créez la classe **Calcul.java** pour définir un objet	avec 2 attributs *somme* et *produit* de type *Integer*
- créez la servlet **CalculJacksonServlet.java** dans le dossier *src/main/java*, dans le package *servlet* :

```java
package servlet;

@WebServlet("/calcul-jackson")
public class CalculJacksonServlet extends HttpServlet {
	
    public void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException{
		int nombre1 = Integer.parseInt(request.getParameter("nombre1"));
		int nombre2 = Integer.parseInt(request.getParameter("nombre2"));
		Calcul calcul = new Calcul();
		calcul.setSomme(nombre1+nombre2);
		calcul.setProduit(nombre1*nombre2);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");	
		PrintWriter out = response.getWriter();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(out, calcul);
	}
}
```

- relancez le Tomcat et appellez l'URL [http://localhost:8080/calcul-jackson?nombre1=3&nombre2=4](http://localhost:8080/calcul-jackson?nombre1=3&nombre2=4)

----

## Présentation de la spécification JAX-RS et exemple d'utilisation de l'implémentation Jersey

- **JAX-RS**, pour *Java API for RESTful Web Services* est une spécification de Java EE.
- il faut utiliser une implémentation : **Jersey** est l'implémentation de référence fournie par Oracle 
    - la première dépendance est la bibliothèque Jersey
    - la deuxième dépendance est Jackon pour Jersey, pour convertir les objets en Json

```xml
<dependency>
    <groupId>org.glassfish.jersey.containers</groupId>
    <artifactId>jersey-container-servlet</artifactId>
    <version>2.25.1</version>
</dependency>
<dependency>
	<groupId>org.glassfish.jersey.media</groupId>
	<artifactId>jersey-media-json-jackson</artifactId>
	<version>2.25.1</version>
</dependency>
```

----

- il faut ajouter dans le *web.xml* la servlet qui va jouer le rôle de contrôleur frontal

```xml
<servlet>
	<servlet-name>javax.ws.rs.core.Application</servlet-name>
</servlet>
<servlet-mapping>
        <servlet-name>javax.ws.rs.core.Application</servlet-name>
        <url-pattern>/rest/*</url-pattern>
</servlet-mapping>
```

- pas besoin d'écrire la réponse avec `PrintWriter`, le contrôleur s'en charge
- récupération des paramètres de requêtes avec `@QueryParam`
- `@Produces(MediaType.APPLICATION_JSON)` permet de transformer l'objet Java en JSON et de d'écrire dans la réponse que l'on produit du JSON
- `@GET` permet de dire que la ressource est accessible en GET

----

- créez la ressource **CalculResource.java** dans le dossier *src/main/java*, dans le package *resource* :

```java
package resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import model.Calcul;

@Path("/calcul-jersey")
public class CalculResource {
	
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@GET
	public Calcul calcul(@QueryParam("nombre1") Integer nombre1, @QueryParam("nombre2") Integer nombre2){
		Calcul calcul = new Calcul();
		calcul.setSomme(nombre1+nombre2);
		calcul.setProduit(nombre1*nombre2);
		return calcul;
	}
}
```

- relancez le Tomcat et appellez l'URL [http://localhost:8080/rest/calcul-jersey?nombre1=3&nombre2=4](http://localhost:8080/rest/calcul-jersey/calcul-jackson?nombre1=3&nombre2=4)