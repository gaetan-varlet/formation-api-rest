# Spring et Spring Boot

## Introduction

Spring :
- l'idée est de généraliser à toute application un modèle de programmation particulier : l'inversion de contrôle
- configuration en XML
- Spring est souvent en avance par rapport aux solutions proposées par Jave EE
- Spring est aujourd'hui incontournable pour les applications Java EE
- Spring structure le code d'une application, le rend évolutif
- **Spring Initializr** est un site web qui permet d'initialiser une application prêt à l'emploi en fonction de nos besoins
- **Spring Boot** facilite le déploiement des applications Java avec un serveur d'application embarqué

## Rappel : Librairies Java et introduction à Maven

- bibliothèques : un fichier compressé au format **jar** (Java Archive) contenant un ensemble de classes Java compilées, organisées en package
- le site *Maven Repository* permet d'obtenir des bibliothèques Java
- lorsqu'on utilise une bibliothèque, il faut également ajouter les dépendances de cette bibliothèque (et éventullement les dépendances des dépendances)
- utilisation d'une bibliothèque Java sans Maven/Gradle : il faut télécharger le JAR. Et dans la plupart des IDE, il est possible d'ajouter un JAR externe au projet, ce qui ajoute automatiquement le JAR dans le *classpath* du projet, et il pourra alors être utilisé dans le projet
- le *classpath* est le chemin d'accès au répertoire où se trouvent les classes et les packages Java
- pour simplifier cela, il est possible d'utiliser *Maven*, qui va trouver une bibiothèque, la télécharger et la déposer dans un dossier centralisé, et ajouter la réfénrence de cette bibliothèque dans le projet
- pour utiliser Maven, il faut télécharger un client Maven (mvn en ligne de commande par exemple) ou utiliser le client Maven des IDE
- `groupId x artifactId x version` permet d'identifier de manière unique un JAR
- le fichier **pom.xml**, pour Project Object Model, décrit le projet, ses dépendances et l'ordre à suivre pour sa production
- il est possible de créer sa propre bibliothèque sous forme d'un JAR en exposant des services (balise `<packaging>` dans le pom)
- pour exécuter le JAR :
    - en exécutant la classe. Il faut ajouter dans le classpath le jar ainsi que ces éventuelles dépendances : `java -classpath monJar.jar com.example.test.Hello`
    - en exécutant le JAR : `java -jar monJar.jar`. Il faut renseigner dans le fichier *MANIFEST.MF* du jar l'attribut **Main-Class** pour lui dire la classe à exécuter, et l'attribut **Class-Path** pour lui préciser les dépendances du projet. (fichier est généré par Maven lors du build, qui se trouve dans le dossier *META-INF*)
    - le plugin Maven *maven-jar-plugin* peut renseigner le fichier *MANIFEST* automatiquement lors du build en précisant les options dans la balise `<manifest>` avec `<mainClass>` et `<addClasspath>`
- il peut y a des conflits de version entre les dépendances de notre projet, que l'on peut voir en regardant le graph des dépendances (2 versions différentes de la même bibliothèque). La meilleure solution est souvent d'utiliser la version la plus élevée (compatibilité descendante fonctionne souvent mieux). Il est possible dans une dépendance de dire de ne pas ramener une dépendance transitive
```xml
<dependency>
    <groupId>...</groupId>
    <artifactId>...</artifactId>
    <version>...</version>
    <exclusion>
        <groupId>...</groupId>
        <artifactId>...</artifactId>
    </exclusion>
</dependency>
```

## L'architectire Web MVC

- l'architecture Web MVC 2 est une évolution qui consiste à centraliser toutes les requêtes HTTP vers une seule ressource qui est une servlet, également appelé **Front Controller**, qui va déléguer le traitement à une classe différente pour chaque besoin appelé **Classes de commande**
- **Struts** est un framework qui a été le standard incontournable pour réaliser une application web pendant plusieurs années
- Java EE a par la suite a proposé un standard MVC2 : **JSF** pour JavaServer Faces
- Struts/Struts2, et JSF/JSF2 ne sont plus beaucoup utilisés car ils ont été remplacé par le framework Spring

## Pourquoi a t-on besoin de Spring ?

### La programmation par contrat
- créer des classes pour nos services et nos repository n'est pas flexible/évolutif, car nos composants font appel explicitement à d'autres composants en instanciant un service dans un controller par exemple. On crée donc un **couplage fort**
- pour flexibiliser ses dépendances, on va faire appel au design pattern **programmation par contrat** qui permet de relâcher les contraintes qui existent entre les différents composants grâce aux interfaces. On passe d'un couplage fort à un **couplage faible**
- ces interfaces vont définir comment le composent appelant communique avec le composant appelé. On parle aussi de *contrat*
- à la place d'instancier une classe, on fait référence à l'interface sans l'instancier. Le code compile mais une exception va être levée à l'utilisation car nos interfaces ne pointent sur aucune implémentation

### Injection de dépendance
- il faut ajouter des *setters* dans les classes ou une référence aux interfaces est faite
- dans le composant appelant de haut niveau, il faut instancier une implémentation de chaque interface des différents composants que l'on souhaite utiliser et les injecter en utilisant les *setters*
- cette solution reste cependant compliquée

### Inversion de contrôle
- il est possible d'utiliser la refléxivité pour dire quelle implémentation de l'interface utiliser, ce qui permet en cas de création d'un nouvelle implémentation de ne pas avoir à faire évoluer son code
- il est possible de mettre le nom des classes dans un fichier de configuration pour choisir quoi instancier et injecter au démarrage de l'application : solution de Spring
- le fait d'avoir un endroit centralisé du code qui se charge d'instancier les composants et de les mettre en relation grâce à l'injection de dépendances s'appelle l'inversion de contrôle
```java
UserService userService = (UserService) Class.forName(userServiceClass).getDeclaredConstructor().newInstance();
```
```properties
userServiceClass=com.demo.myapp.service.UserServiceImpl
```

## Les fondamentaux du framework Spring

### Conteneur léger Spring
le conteneur léger de Spring permet de :
- lire le fichier de configuration
- instancier les classes
- mettre en relation les composants en invoquant les setters

Ce conteneur léger va effectuer l'inversion de contrôle et garder en mémoire tous les composants instanciés durant toute la vie de l'application.  
Le fichier de configuration au format XML est le fonctionnement originel de Spring : **applicationContext.xml** dans le dossier `src/main/resources`
- il ya une balise racine `<beans>` qui contient des `<bean>` pour chaque classe à instancier
```xml
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean class="com.demo.myapp.service.UserServiceImpl">
        <property name="userRepository" ref="userRepo">
        <!-- permet d'injecter une dépendance dans l'attribut userRepository  -->
    </bean>
    <bean id="userRepo" class="com.demo.myapp.repository.UserRepositoryImpl"/>
</beans>
```
- il faut utiliser la bibliothèque **spring-context** pour que ça fonctionne et utiliser le code suivant. L'implémentation définie dans le fichier de configuration sera automatiquement utilisée.
```java
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
UserService userService = context.getBean(UserService.class);
userService.findAll();
```

### Affectation d'une valeur à une propriété
- les classes instanciées par Spring ne le sont qu'une seule fois. Il n'est donc pas nécessaire d'avoir des attributs statiques
- il est possible d'affecter une valeur à une propriété en le renseignant dans le fichier de configuration XML avec une balise `<property>` dans le `<bean>` en question
- il faut que la propiété ait un setter
```xml
 <property name="age" value="2">
 <property name="prenom" value="Louis">
```

### Autowiring byName et byType

- pour éviter d'avoir à préciser des `<property>` à injecter via l'attribut **ref** et l'attribut **id**, il est possbile d'utiliser l'attribut **autowire="byName"** dans les balises `<bean>` ou directement dans la balise `<beans default-autowire="byName">`, en se basant syr la valeur de l'attribut **id**
- il est également possible d'utiliser l'autowiring **byType** qui se base sur le type des propriétés au lieu de leur id. Spring va rechercher s'il existe un composant de type compatible, une implémentation de l'interface dans notre cas

```xml
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd" default-autowire="byType">
    <bean class="com.demo.myapp.service.UserServiceImpl">
    <bean class="com.demo.myapp.repository.UserRepositoryImpl"/>
</beans>
```

### Configuration par annotation

- depuis Spring 2.5, il est possible d'exploiter les annotations Java pour configurer les applications, ce qui permet de palier à la verbosité des fichiers XML. ON parle de **XML Hell**
- il existe plusieurs annotations qui permettent de faire de l'autowiring : `@Autowired` spéficique à Spring, ou `@Inject` annotation Java
- avant cela, il faut :
    - supprimer `default-autowire="byType"`
    - ajouter un **xmlns** dans la balise `<beans>` pour pouvoir utiliser une balise XML de Spring Context **annotation-config**

```xml
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:context="http://www.springframework.org/schema/context" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
    <context:annotation-config/>
    <bean class="com.demo.myapp.service.UserServiceImpl">
    <bean class="com.demo.myapp.repository.UserRepositoryImpl"/>
</beans>
```

```java
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
}
```

### Valorisation des propriétés par annotation : @Value et fichier de propriétés

- `@Value` permet de se passer des balises *property* pour valoriser une propriété
- il est possible d'injecter ce qui est marqué dans un fichier de properties, en ajoutant dans le fichier de configuration XML `<context:property-placeholder location="classpath:application.properties"/>`

```java
@Value("124")
private int number; // inutile car équivalent à number = 124
@Value("${myNumber}")
private int number2;
```
```properties
myNumber=12
```

### Détection automatique des beans

- il est également possible de se passer dans balises `<bean>` dans le fichier de configuration XML en utilisant des annotations
- l'annotation la plus générique que l'on peut utiliser est `@Component`. Elle indique que la classe est un composant de notre architecture
- l'annotation fille `@Controller` permet d'indiquer plus précisément de quel type de composant il s'agit, même si Spring va les traiter de la même manière
- l'annotation `@Service` suit la même logique
- l'annotation `@Repository` suit également la même logique avec quand même quelques subtilités négligeables par rapports aux autres annotations
- il est possible que Spring interprète différemment ces annotations dans le futur

```java
@Service
public class UserServiceImpl implements UserService {}
```

- il faut préciser à Spring dans quels packages il doit détecter les annotations dans ces classes avec une balise dans le fichier de configuration XML `<component-scan>`

```xml
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:context="http://www.springframework.org/schema/context" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
    <context:annotation-config/>
    <context:property-placeholder location="classpath:application.properties"/>
    <context:component-scan base-package="com.demo.myapp"/>
</beans>
```

- ce n'est pas forcément une bonne idée car pour changer d'implémentation d'une interface, il faut ajouter une annotation sur une implémentation et enlever l'annotation sur l'autre implémentation, alors qu'avant, il suffisait de modifier le fichier de configuration XML

### Gérer les conflits de dépendances

- il existe une meilleure solution où il est possible de laisser annoté plusieurs implémentation d'une même interface sans avoir de conflit (erreur **Expecting single matching bean but found 2**)
- il n'y a pas une seule solutions, mais plusieurs :
    - répartition par packages
    - répartition par librairie
    - annotation `@Primary`
    - autowiring byName
    - `@Profile` ou `@Conditional`

- il est possible de dupliquer la balise `<context:component-scan/>` en scannant non pas le package racine mais les différents sous-packages contenant les stéréotypes, ce qui peut éviter des problèmes difficiles à débugguer

```xml
<context:component-scan base-package="com.demo.myapp.controller"/>
<context:component-scan base-package="com.demo.myapp.service"/>
<context:component-scan base-package="com.demo.myapp.repository"/>
```

Cela ne règle pas le problème des conflits de dépendances, mais cela ouvre la voie à la **création de sous-packages** pour grouper les implémentations (un sous-package par implémentation), et choisir le sous-package que l'on souhaite dans la balise `<context:component-scan/>`, par exemeple : `<context:component-scan base-package="com.demo.myapp.controller.default"/>`

### Se passer du fichier XML

- le fichier XML est presque vide, il va être possible de s'en passer
- il est déjà possible de supprimer `<context:annotation-config/>` avec l'introduction de `<context:component-scan/>` car elle active implicitivement la première
- il est possible de créer une classe Java pour la configuration qui doit être annotée `@Configuration`

```java
@Configuration
@ComponentScan(basePackages = {"com.demo.myapp.controller", "com.demo.myapp.service", "com.demo.myapp.repository"})
@PropertySource("classpath:application.properties")
public class AppConfig {}
```

Dans la classe avec le main de l'application, il faut utiliser une autre implémentation de l'interface **ApplicationContext** en précisant non plus le chemin du fichier XML mais la classe Java contenant la configuration

```java
// AVANT
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
// APRES
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
```

Il est possible de se passer du fichier XML, mails il est également possible de l'utiliser de manière complétaire en ajoutant une annotation `@ImportResource`

```java
@Configuration
@ComponentScan(basePackages = {"com.demo.myapp.controller", "com.demo.myapp.service", "com.demo.myapp.repository"})
@PropertySource("classpath:application.properties")
@ImportResource("classpath:applicationContext.xml")
public class AppConfig {}
```


### Les classes de configuration plus en détail

- il est possible d'avoir plusieurs classes de configuration (toutes annotées `@Configuration`), il faut alors les préciser dans la création de l'`ApplicationContext` qui peut prendre plusieurs classes en paramètre
- il est également possible de na pas avoir de classe de configuration en faisant la configuration directement sur la classe contenant le main, en se référençant dans l'`ApplicationContext`

```java
@Configuration
@ComponentScan(basePackages = {"com.demo.myapp.controller", "com.demo.myapp.service", "com.demo.myapp.repository"})
@PropertySource("classpath:application.properties")
public class App {
    public static void main(String[] args){
        ApplicationContext context = new AnnotationConfigApplicationContext(App.class);
        ...
    }
}
```

- il est possible dans les classes de configuration d'ajouter des méthodes, annotées`@Bean`, qui vont fournir des objets supplémentaires au conteneur léger Spring
- il est par exemple possible qu'un service soit instancié via une méthode au lieu du componentScan de l'annotation `@Service`

```java
@Bean
public UserService configUserService(){
    return new UserServiceImpl();
}
```

## Mise en place de Spring Boot

### SpringBootApplication

- `@ComponentScan` peut ne pas avoir de *basePackages*, Spring va alors autodétecter tous les composants dans le package et les sous-packages de la classe annotée
- `@SpringBootApplication`, qui se trouve dans la bibliothèque *spring-boot-autoconfigure*, inclut `@ComponentScan` dans sa version par défaut, ainsi que `@Configuration`
- l'instanciation de l'`ApplicationContext` change également
- concernant `@PropertySource("classpath:application.properties")`, par défault, SpringBoot cherche un fichier de properties *application.properties*. L'annotation peut donc être supprimée

```java
@SpringBootApplication
public class App {
    public static void main(String[] args){
        ApplicationContext context = SpringApplication.run(App.class);
        ...
    }
}
```

### Spring Boot et Maven

### Fat Archive - Déployer une application en production

## Les applications Web avec Spring Boot

## Développer une API REST avec Spring

## Spring et les bases de données relationnelles

## Architectures "Cloud native" et microservices
