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

## Spring Boot : initialisation du projet

- le déploiement d'une application Web nécessite un livrable (war) et un serveur applicatif (Tomcat)
- Spring Boot offre un déploiement incluant le conteneur applicatif
- on utilisera cette possiiblité en local mais on continuera de créer un WAR pour déployer au CEI
- [Spring Initializr](https://start.spring.io/) permet de générer le squelette d'une application Spring.
- **Création d'un projet Maven en java 8** :
  - sélectionner les dépendences *Web*, *JPA* et *H2*
  - sélectionner Packaging JAR

----

## Configuration du projet (1)

ajout de properties dans le fichier **application.properties** dans **src/main/resources** pour la gestion de la log et de la base de données

```
logging.config=classpath:log4j2-local.xml

# active la console H2 à l'URL http://localhost:8080/h2-console/ et renseigner jdbc:h2:mem:testdb dabs JDBC URL
spring.h2.console.enabled=true
# désactiver la création automatique des tables par Hibernate et utiliser les requêtes de schema.sql
spring.jpa.hibernate.ddl-auto=none
```

----

## Configuration du projet (Alt PostGre)

 dans le **pom.xml**, remplacer la dépendence Maven *h2* par la dépendence *postgresql*
```xml
<dependency>
	<groupId>org.postgresql</groupId>
	<artifactId>postgresql</artifactId>
	<scope>runtime</scope>
</dependency>
```

remplacer le fichier de **properties** par celui-là:
```
logging.config=classpath:log4j2-local.xml

spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://dvtoucan01ldb01.ad.insee.intra:1983/di_pg_toucan01_dv01
spring.datasource.username=user_toucan01_loc
spring.datasource.password=***
```

----

## Configuration du projet (2)

ajout dans le **pom.xml** d'une dépendence pour dire que l'on utilise Log4j2 plutôt que Logback (proposé par défaut)

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

fichier **log4j2-local.xml** à ajouter dans **src/main/resources**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration monitorInterval="60">

	<!-- Propriétés surchargeables par le CEI indiquant le chemin vers le dossier contenant les logs et le nom du fichier de log -->
	<Properties>
		<property name="dossierLog">D:/logs/formation-api-rest</property>
		<property name="nomFichierLog">formation-api-rest</property>
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
            <AppenderRef ref="File-Appender" />
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

lancer l'application et accéder à l'URL `http://localhost:8080/hello`

----

## Ajouter de la log

```java
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// ajouter dans la classe TestController
private static final Logger log = LogManager.getLogger();
// ajouter dans la méthode helloWorld
log.info("passage dans le controller helloWorld");
```

----

## Création de données en base

créer un fichier **schema.sql** et un fichier  **data.sql** dans src/main/resources pour initialiser la base :

```sql
-- fichier schema.sql
CREATE SCHEMA formation;

CREATE TABLE formation.VIN (
	id serial PRIMARY KEY,
	chateau VARCHAR(100) NOT NULL,
	appellation VARCHAR(100),
	prix DECIMAL);

CREATE SEQUENCE formation.vin_id_seq start 5 increment 1;

-- fichier data.sql
INSERT INTO formation.vin (chateau, appellation, prix) VALUES ('Château Margaux', 'Margaux', 500);
INSERT INTO formation.vin (chateau, appellation, prix) VALUES ('Château Cantemerle', 'Haut-Médoc', 30.5);
INSERT INTO formation.vin (chateau, appellation, prix) VALUES ('Château Lascombes', 'Margaux', 80);
INSERT INTO formation.vin (chateau, appellation, prix) VALUES ('Domaine Lejeune', 'Pommard', 40);
```

----

## Création de l'objet Java correspondant

```java
package fr.insee.formationapirest.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "vin", schema = "formation")
public class Vin {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_vin")
	@SequenceGenerator(name = "seq_vin", sequenceName = "formation.vin_id_seq", allocationSize = 1)
	private Integer id;
	
	private String chateau;
	private String appellation;
	private Double prix;

    // ajouter les getters et setters	
}
```

----

## Création du DAO

- JpaRepository est une interface de Spring utilisant Hibernate qui donne accès à plein de méthodes nativement
- en implémentant l'interface, il faut préciser le type d'objet correspondant ainsi que le type de la clé primaire

```java
package fr.insee.formationapirest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.insee.formationapirest.model.Vin;

@Repository
public interface VinRepository extends JpaRepository<Vin, Integer> {
	
}
```

----

## Création du controller VinController

création d'une méthode permettant de récupérer tous les vins

```java
package fr.insee.formationapirest.controller;

@RestController
@RequestMapping("/vin")
public class VinController {
	
	@Autowired
	VinRepository vinRepository;

	@RequestMapping(method = RequestMethod.GET)
	public List<Vin> getAll(){
		return vinRepository.findAll();
	}
	
}
```
appeler l'URL `http://localhost:8080/vin` pour obtenir tous les vins

----

## Ajout d'une méthode pour obtenir un vin par son id

```java
@RequestMapping(value= "/{id}", method = RequestMethod.GET)
public Vin getById(@PathVariable Integer id){
	return vinRepository.findById(id).orElse(null);
}
```

appeler l'URL `http://localhost:8080/vin/1` pour obtenir le vin avec l'id 1

----

## Supprimer un vin par son id

```java
@RequestMapping(value= "/{id}", method = RequestMethod.DELETE)
public void deleteById(@PathVariable Integer id){
	vinRepository.deleteById(id);
}
```

- tester avec un id existant (code HTTP 200)
- tester avec un id qui n'existe pas (code HTTP 500)

----

## Faire un contrôle d'existence avant suppression

tester l'existence du vin avant de le supprimer

```java
@RequestMapping(value= "/{id}", method = RequestMethod.DELETE)
public void deleteById(@PathVariable Integer id){
	if(vinRepository.existsById(id)) { // renvoie un boolean (true si l'objet existe, false sinon)
		vinRepository.deleteById(id);
	}
}
```

----

## Création d'un nouveau vin

```java
@RequestMapping (method = RequestMethod.POST)
public Vin add(@RequestBody Vin vin){
    // ajouter un contrôle pour s'assurer que l'id n'est pas renseigné ou passer par un DTO
	return vinRepository.save(vin);
}
```

faire une requête en post avec un body au format JSON :
```json
{
"chateau":"Château Gloria",
"appellation":"Saint-Julien",
"prix":25
}
```

----

## Mise à jour d'un vin par son id

```java
@RequestMapping (method = RequestMethod.PUT)
public Vin update(@RequestBody Vin vin){
	if(vinRepository.existsById(vin.getId())) {
		return vinRepository.save(vin);
	}
	return null;
}
```

----

## Refactor : mise en place d'une couche de service

- objectif : plus de lien direct entre la couche controller et la couche repository. Tout doit passer par les services
- exemple avec la méthode `getAll()` :

```java
@Service
public class VinService {	
	@Autowired
	VinRepository vinRepository;
	
	public List<Vin> getAll(){
		return vinRepository.findAll();
	}
}
```
```java
// dans VinController :
@Autowired
VinService vinService;

@RequestMapping(method = RequestMethod.GET)
public List<Vin> getAll(){
    return vinService.getAll();
}
```

----

## Filtrage sur un attribut via paramètre de requête

doc : `https://www.baeldung.com/spring-request-param`

exemple en filtrant sur l'appellation : la requête `http://localhost:8080/vin` donne tous les vins alors que la requête `http://localhost:8080/vin?appellation=Margaux` ne donne que les Margaux

```java
// Création dans le Repository d'une méthode filtrant sur l'appellation
List<Vin> findByAppellation(String appellation);

// Création d'un service
public List<Vin> findByAppellation(String appellation){
	return vinRepository.findByAppellation(appellation);
}

// Mise à jour du controller
@RequestMapping(method = RequestMethod.GET)
public List<Vin> getAll(@RequestParam(required=false) String appellation){
	if(appellation != null) {
		return vinService.findByAppellation(appellation);
	}
	return vinService.getAll();
}
```

----

## Filtrage avancé avec Spring Data

possibilité de faire du filtrage avancé avec **Querydsl**

Par exemple, la requête `http://localhost:8080/vin?search=appellation:Margaux,prix>30` devrait renvoyer tous les vins à plus de 30€ et qui ont l'appellation Margaux

----

## Paging et sorting

page, size et sort

```java
// service
public Page<Vin> pageable(Pageable p) {
	return vinRepository.findAll(p);
}
// controller
@RequestMapping(value="/pageable", method = RequestMethod.GET)
public Page<Vin> getAllPageable(Pageable p){
	return vinService.pageable(p);
}
```

`http://localhost:8080/vin/pageable?page=0&size=2&sort=appellation,prix,DESC` renvoie les données par page de 2 éléments triés de manière décroissante par appellation et prix

----

## @Produces et @Consumes


----

## Gestion des erreurs avec exceptions et codes HTTP

----

## Réception d'un fichier dans un controller

----

## Mise en place de Swagger

----


## Configuration de Spring Boot avec des profils

----

## Injection de properties

----

## Spring Security

----

## Les tests dans Spring Boot

----

## CORS

----

## Zipper la réponse

----

## Création d'un WAR pour le déploiement au CEI

- nommer le livrable ROOT
- utiliser **maven-war-plugin** pour créer le war
- utiliser **maven-assembly-plugin** pour créer un zip contenant le war, les properties, le fichier de config de log4j2, le changelog...

----

## Bonus : Bannière ASCII

----

## Bonus : Gestion du cache applicatif

- définition

- limites