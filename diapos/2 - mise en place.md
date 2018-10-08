# Mise en place

----

## Création d'une application web classique avec Maven

- créez un nouveau projet maven en choisissant l'option "skip archetype" et packaging "war"
- créez un fichier **web.xml** dans le dossier *src/main/webapp/WEB-INF/* :
```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
         http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
	version="3.1">
</web-app>
```

----

- passez en Java 8 et ajoutez l'API Servlet dans le **pom.xml** :

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
		String nb1 = request.getParameter("nombre1");
		String nb2 = request.getParameter("nombre2");
		int nombre1 = Integer.parseInt(nb1);
		int nombre2 = Integer.parseInt(nb2);
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

- créez la servlet **CalculServlet.java** dans le dossier *src/main/java*, dans le package *servlet* :

```java
package servlet;

@WebServlet("/calcul")
public class CalculServlet extends HttpServlet {
	
	public void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException{
		String nb1 = request.getParameter("nombre1");
		String nb2 = request.getParameter("nombre2");
		int nombre1 = Integer.parseInt(nb1);
		int nombre2 = Integer.parseInt(nb2);
		int somme = nombre1+nombre2;
		int produit = nombre1*nombre2;

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
		out.println("<p>La somme de "+nombre1+" et "+nombre2+" est égale à "+somme+".</p>");
		out.println("<p>Le produit de "+nombre1+" et "+nombre2+" est égal à "+produit+".</p>");
		out.println("</body>");
		out.println("</html>");
	}
}
```