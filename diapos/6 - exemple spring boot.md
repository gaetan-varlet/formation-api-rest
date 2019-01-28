# Création d'une API REST avec Spring Boot

----

## L'univers Spring

C'est un framework libre pour concevoir des applications Java. Spring s'occupe de l'exécution globale de l'application. Les développeurs peuvent se concentrer sur la configuration du projet et les traitements métiers.

- **Spring Core** : coeur de Spring qui gère la création et le cycle de vie des objets avec les annotations **@Autowired**, **@Controller**, **@Service**, **@Repository**, etc...
- **Spring MVC** : permet de construire des applications web basé sur l'API Servlet
- **Spring Data** : permet de gérer le mapping objet-relationnel (ORM)
- **Spring Batch** : permet de gérer les traitements par lots
- **Spring Securiy** : fournit des fonctionnalités d'authentification, d'autorisation et de sécurité
- **Spring Boot** : permet de créer facilement et rapidement des applications Spring avec un Tomcat embarqué

----

## Spring Boot : initialisation du projet

- le déploiement d'une application Web nécessite un livrable (war) et un serveur applicatif (Tomcat)
- Spring Boot offre un déploiement incluant le conteneur applicatif (jar)
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

# OPTIONNEL : permet de voir la requête exécutée par Hibernate, ainsi que la valeur des paramètres
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
# permet d'indenter la requête dans la log pour mieux la lire
spring.jpa.properties.hibernate.format_sql=true
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

## Raccourci pour le mapping des verbes HTTP dans les controllers

```java
@RequestMapping (method = RequestMethod.GET)
@GetMapping

@RequestMapping (method = RequestMethod.POST)
@PostMapping

// etc
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

[documentation sur les paramètres de requêtes Spring](https://www.baeldung.com/spring-request-param)

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

```bash
http://localhost:8080/vin # donne tous les vins
http://localhost:8080/vin?appellation=Margaux # ne donne que les Margaux
```

----

## Filtrage avancé avec Spring Data

possibilité de faire du filtrage avancé avec **Querydsl**

Par exemple, la requête suivante devrait renvoyer tous les vins à plus de 30€ et qui ont l'appellation Margaux

```http
http://localhost:8080/vin?search=appellation:Margaux,prix>30
```

----

## Paging et sorting

3 mots clés : **page**, **size** et **sort**

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

La requête suivante renvoie les données par page de 2 éléments triés de manière décroissante par appellation et prix
```http
http://localhost:8080/vin/pageable?page=0&size=2&sort=appellation,prix,DESC
```

----

## @Produces et @Consumes

- une requête peut fournir un élément dans son body à différents formats, par exemple XML ou JSON. Elle peut aussi spécifier quels formats elle accepte en retour via l'en tête **Accept**
- l'annotation `@RequestMapping` contient des attributs **consumes** et **produces** permettant de spécifier ce que le service accepte et ce qu'il produit. Si une restriction n'est pas respectée, une erreur HTTP 406 est renvoyée (Not Acceptable)
- sans précision, il n'y a pas de restriction. Exemple de restriction :
```java
@RequestMapping(method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
```
- pour transformer l'objet Java en XML, il faut ajouter l'annotation `@XmlRootElement` sur la classe de l'objet à transformer, dans notre cas, la classe Vin
- il n'est pas possible de renvoyer une liste d'objets au format XML, il faut créer un objet contenant la liste d'objets à retourner

----

## Réception d'un fichier dans un controller (1)

Création d'un controller en *POST* qui reçoit un fichier dans la requête grâce au paramètre **MultipartFile**
```java
// création d'un service qui attend un objet multipart/form-data avec comme nom de paramètre multipartfile
// le service retourne ici le contenu du fichier
@RequestMapping(value="/upload", method = RequestMethod.POST)
public String upload (@RequestParam MultipartFile multipartfile) throws IOException {
	return new String(multipartfile.getBytes());
}
```

----

## Réception d'un fichier dans un controller (2)

Requête HTTP avec l'en-tête **Content-Type = multipart/form-data**

![Requête multipart/form-data](diapos/images/requete-multipart.png "Requête multipart/form-data")

----

## Mise en place de Swagger

- Swagger permet de générer une documentation standardisée de votre API répondant aux spécifications **OpenAPI** au format JSON, accessible avec l'URL **http://localhost:8080/v2/api-docs**, et également une documentation au format HTML permettant de tester ses services à l'URL **http://localhost:8080/swagger-ui.html**.  
- il faut ajouter les 2 dépendances suivantes ainsi que l'annotation **@EnableSwagger2** sur la classe contenant la méthode main de l'application.

```xml
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>

<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.9.2</version>
</dependency>
```

----

## Configuration plus fine de Swagger

après la mise en place de Spring Security ?

----

## Injection de properties

possibilité d'injecter des properties dans des variables java

```bash
# dans les properties
propertyNonSurchargee=coucou
```

```java
// dans le controller Test
import org.springframework.beans.factory.annotation.Value;

@Value("${propertyNonSurchargee}")
	private String propertyCoucou;

@RequestMapping(value="propertyNonSurchargee", method = RequestMethod.GET)
public String propertyNonSurchargee() {
	return propertyCoucou;
}
```

lancer l'application et accéder à l'URL `http://localhost:8080/propertyNonSurchargee`

----

## Configuration de Spring Boot avec des profils

Spring Boot permet la gestion de différents environnements avec les profils :
- plusieurs fichiers de properties :
	- **application.proterties** pour les properties communes qui ne changent pas, ici **propertyNonSurchargee=coucou** qui sort donc des 2 fichiers suivants
	- **application-local.proterties** pour les properties spécifiques l'environnement local
	- **application-dev.proterties** pour les properties spécifiques l'environnement de dev
- création de profils dans le **pom.xml** (cf diapo suivante)
- démarrer l'application avec un profil en ajoutant dans *Program arguments* la ocmmande suivante
```bash
--spring.profiles.active=local # ou avec dev pour la lancer en dev
```

----

## Création des profils local et dev dans le POM

ajouter les profils dans la balide `<project>` du pom.xml

```xml
<profiles>
	<profile>
		<id>local</id>
		<properties>
			<env>local</env>
			<config.properties>local</config.properties>
		</properties>
	</profile>
	<profile>
		<id>dev</id>
		<properties>
			<env>dev</env>
			<config.properties>dev</config.properties>
		</properties>
	</profile>
</profiles>
```

----

## Création d'un fichier de conf de log pour les plateformes du CEI

Création d'un fichier **log4j2-cei.xml** pour préciser le chemin des logs sur les plateformes du CEI

```xml
<!-- Seule partie qui change par rapport au 1er fichier -->
	<Properties>
		<property name="dossierLog">/var/log/tomcat8</property>
		<property name="nomFichierLog">formationapirest</property>
	</Properties>
```

Dans le fichier de properties de dev *application-dev.properties*, il faut changer le chemin du fichier de log
```bash
logging.config=classpath:log4j2-cei.xml
```

----

## Création d'une property environnement

```bash
formationapirest.environnement=environnement local # dans le fichier application-local.properties
formationapirest.environnement=environnement de developpement # dans le fichier application-dev.properties
```

Création d'un controller qui va renvoyer l'environnement courant
```java
@Value("${formationapirest.environnement}")
private String environnement;

@RequestMapping(value="environnement", method = RequestMethod.GET)
public String environnement() {
	return environnement;
}
```


----

## Gestion des erreurs avec exceptions et codes HTTP (1)

- l'objectif est de renvoyer les codes HTTP adéquat pour prévenir l'utilisateur s'il a fait une erreur
- création d'un package **exception** et de 2 classes :

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class VinInconnuException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public VinInconnuException(String message) {
		super(message);
	}
}
```
```java
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VinInvalideException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public VinInvalideException(String message) {
		super(message);
	}
}
```

----

## Gestion des erreurs avec exceptions et codes HTTP (2)

Mise à jour des services avec ces exceptions
- exemple avec le service de suppression d'un vin
```java
public void deleteById(@PathVariable Integer id){
	if(vinRepository.existsById(id)) { // renvoie un boolean (true si l'objet existe, false sinon)
		vinRepository.deleteById(id);
	} else {
		throw new VinInconnuException("le vin avec l'id "+ id + " n'existe pas");
		}
}
```
- **TP** :
	- vérifier qu'un vin existe avant de le renvoyer sur le *getById(id)*, sinon dire que le vin est inconnu
	- vérifier sur cette même méthode que l'id est positif, sinon dire que l'id du vin n'est pas valide
	- lors de la création et de la mise à jour d'un vin, vérifier que le chateau et l'appellation font entre 1 et 50 caractères et que le prix n'est pas négatif

----

## Compresser la réponse (1)

Lorsque la réponse HTTP est grosse, il est possible de compresser pour alléger le transfert sur réseau. Charge au client de la décompresser (le navigateur sait le faire tout seul)
- pour compresser une réponse HTTP, il faut que le client soit d'accord et que le header **Accept-Encoding: gzip** soit présent dans la requête
- si la réponse est compressée, il y aura le header **Content-Encoding: gzip**

Utilisation d'une bibliothèque [Ziplet](https://github.com/ziplet/ziplet) qui créer un filtre pour zipper la réponse

----

## Compresser la réponse (2)

Ajouter la dépendance suivante, et créer un package **config** et créer la classe ZipletConfig

```xml
<dependency>
	<groupId>com.github.ziplet</groupId>
	<artifactId>ziplet</artifactId>
	<version>2.3.0</version>
</dependency>
```
```java
@Configuration
public class ZipletConfig {
	
	@Bean
	public Filter compressingFilter() {
		return new CompressingFilter();
	}
	
}
```

----

## Keycloak avec Spring Security (1)

- sécuriser son API avec une couche d'authentification
- fonctionne avec un jeton mis un header de la requête
```bash
Authorization: Bearer <token>
```
- [tutoriel en français](https://blog.ineat-conseil.fr/2017/12/securisez-vos-apis-spring-avec-keycloak-3-utilisation-des-connecteurs-spring-de-keycloak/)
- ajout de 2 dépendances Maven
```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
	<groupId>org.keycloak</groupId>
	<artifactId>keycloak-spring-boot-starter</artifactId>
	<version>4.7.0.Final</version>
</dependency>
```
- création d'une classe **SpringKeycloakSecurityConfiguration** dans le package *config*
- un peu de confi dans les properties

----

## Keycloak avec Spring Security (2)


----

## CORS : Cross-origin resource sharing

----

## Création d'un WAR pour le déploiement au CEI (1)

- nommer le livrable ROOT
- utiliser **maven-war-plugin** pour créer le war
- utiliser **maven-assembly-plugin** pour créer un zip contenant le war, les properties, le fichier de config de log4j2, le changelog...
- properties pour la prod
- exécuter le goal maven **clean install application-serveur-2-springboot** avec le profil **dev**

----

## Création d'un WAR pour le déploiement au CEI (2)

ajouter dans le pom.xml les éléments suivants :
```xml
<packaging>war</packaging>

<build>
	<!-- permet le filtrage des properties afin de sélectionner le bon suivant le profil -->
	<resources>
		<resource>
			<directory>src/main/resources</directory>
			<filtering>true</filtering>
		</resource>
	</resources>

	<!-->définit le nom du WAR< -->
	<finalName>ROOT</finalName>

	<plugins>
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-war-plugin</artifactId>
			<configuration>
				<warName>ROOT</warName>
				<failOnMissingWebXml>false</failOnMissingWebXml>
			</configuration>
		</plugin>
	</plugins>
</build>
```

----

## Les tests dans Spring Boot

----

## Bonus : Bannière ASCII

changer la bannière de démarrage de l'application :
- créer un fichier **banner.txt** dans *src/main/resources*
- [http://patorjk.com/software/taag/#p=display&f=Graceful&t=Youpi](http://patorjk.com/software/taag/#p=display&f=Graceful&t=Youpi) permet de générer un texte avec des polices fantaisistes

----

## Bonus : Gestion du cache applicatif

- permet à une application d'éviter de répéter des appels de méthodes coûteux en stockant le résultat d'un appel en mémoire
- limites / risques : les données en cache ne sont plus valides à cause d'une mise à jour d'un autre module applicatif (par exemple un module batch) ou par le passage d'un script en base de données