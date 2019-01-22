# Création d'une API REST avec Spring Boot

----

## L'univers Spring

C'est un framework libre pour concevoir des applications Java. Spring s'occupe de l'exécution globale de l'application. Les développeurs peuvent se concentrer sur la configuration du projet et les traitements métiers.

- **Spring Core** : coeur de Spring qui gère la création et le cycle de vie des objets qu'il gère
- **Spring MVC** : permet de construire des applications web basé sur l'API Servlet
- **Spring Data** : permet de gérer le mapping objet-relationnel (ORM)
- **Spring Batch** : permet de gérer les traitements par lots
- **Spring Securiy** : fournit des fonctionnalités d'authentification, d'autorisation et de sécurité
- **Spring Boot** : permet de créer facilement et rapidement des applications Spring avec un Tomcat embarqué

----

## Spring Boot : initialisaiton du projet

- le déploiement d'une application Web nécessite un livrable (war) et un serveur applicatif (Tomcat)
- Spring Boot offre un déploiement incluant le conteneur applicatif
- on utilisera cette possiiblité en local mais on continuera de créer un WAR pour déployer au CEI
- [Spring Initializr](https://start.spring.io/) permet de générer le squelette d'une application Spring.
- **Création d'un projet Maven en java 8** :
  - sélectionner les dépendences _Web_, _JPA_ et _PostgreSQL_
  - sélectionner Packaging JAR

----

## Configuration du projet (1)

Ajout de properties dans le fichier **application.properties** dans **src/main/resources** pour la gestion de la log et de la base de données

```
logging.config=classpath:log4j2-local.xml

spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://dvtoucan01ldb01.ad.insee.intra:1983/di_pg_toucan01_dv01
spring.datasource.username=user_toucan01_loc
spring.datasource.password=***
```

----

## Configuration du projet (2)

Ajout dans le **pom.xml** d'une dépendence pour dire que l'on utilise Log4j2 plutôt que Logback (proposé par défaut)

```xml
		<!-- Indique à Spring Boot que l'on utilise log4j2 et pas logback qui est proposé par défaut -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
			<exclusions>
				<exclusion>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-logging</artifactId>
				</exclusion>
			</exclusions>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-log4j2</artifactId>
		</dependency>
```

----

## Configuration du projet (3)

Fichier **log4j2-local.xml** à ajouter dans **src/main/resources**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration monitorInterval="60">

	<!-- Propriétés surchargeables par le CEI indiquant le chemin vers le dossier contenant les logs et le nom du fichier de log -->
	<Properties>
		<property name="dossierLog">D:/logs/toucan</property>
		<property name="nomFichierLog">toucan</property>
	</Properties>

	<Appenders>
		<Console name="Console-Appender" target="SYSTEM_OUT">
			<PatternLayout>
				<pattern>
					[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %c{1} - %msg%n
				</pattern>

			</PatternLayout>
```

----

## Configuration du projet (4)

```xml
        </Console>
		<RollingFile name="File-Appender">
			<FileName>${dossierLog}/${nomFichierLog}.log</FileName>
			<FilePattern>${dossierLog}/%d{yyyy-MM-dd}-${nomFichierLog}.log</FilePattern>
			<PatternLayout>
				<Pattern>%d{yyyy-MMM-dd HH:mm:ss a} [%t] %-5level %logger{36} - %msg%n</Pattern>
			</PatternLayout>
			<Policies>
				<TimeBasedTriggeringPolicy interval="1" modulate="true" />
			</Policies>
			<DefaultRolloverStrategy max="90" />
		</RollingFile>
	</Appenders>
	<Loggers>
		<Logger name="fr.insee.toucan" level="info" additivity="false">
			<AppenderRef ref="File-Appender" level="info" />
			<AppenderRef ref="Console-Appender" level="info" />
		</Logger>
		<Root level="info">
			<AppenderRef ref="Console-Appender" />
		</Root>
	</Loggers>
</Configuration>
```

----

## Création d'un HelloWorld

- création d'un package **controller** : par exemple *fr.insee.formationapirest.controller* si le main de l'application est dans le package *fr.insee.formationapirest*
- création d'une classe **TestController** dans ce package

```java
package fr.insee.formationapirest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	@RequestMapping(value="hello", method = RequestMethod.GET)
	public String helloWorld() {
		return "Hello World !";
	}
}
```

- lancer l'application et accéder à l'URL `http://localhost:8080/hello`

----

## Zipper la réponse

----

## Création d'un WAR pour le déploiement au CEI

- nommer le livrable ROOT
- utiliser **maven-war-plugin** pour créer le war
- utiliser **maven-assembly-plugin** pour créer un zip contenant le war, les properties, le fichier de config de log4j2, le changelog...