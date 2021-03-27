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

- lorsqu'on ne connaît pas Spring Boot, on a l'impression que c'est magique
- gestion du cycle de vie de l'application avec Maven
    - il n'est pas conseillé d'utiliser la dépendance `spring-boot-autoconfigure`
    - il est conseillé d'utiliser la balise `<parent>` `spring-boot-starter-parent`, pour bénéficier de tout ce qui est déclaré dans le parent, il y a dedans par exemple la bibliothèque `spring-boot-release`, `spring-boot-starter-data-jpa`...
    - toutes les dépendances gérées dans le POM parent ne sont pas directement téléchargées mais pour lesquels on donne des informations a priori, en donnant la version par défaut qu'il faudrait utiliser pour le cas où elles seraient nécessaire
    - si on utilise aucun starter, il est possible d'utiliser le starter `spring-boot-starter` pour bénéficier notamment de la dépendance `spring-boot-autoconfigure`
    - le plugin `spring-boot-maven-plugin` apporte également des choses intéressantes concernant le démarrage de l'application ou le build de l'application

### Fat Archive - Déployer une application en production

- pour lancer une application Java en ligne de commande, il faut exécuter la classe contenant la méthode *main* en ajoutant dans l'argument `-cp` toutes les bibliothèques auxquelles on fait référence : `java -cp "..." com.demo.myapp.App`
- avec le plugin `spring-boot-maven-plugin`, il suffit de lancer la commande `mvn spring-boot:run` pour démarrer une application Spring Boot
- pour créer un JAR de l'application, il faut utiliser la commande `mvn clean install`
    - sans le plugin de Spring Boot, on obtient un JAR avec les classes du projet, avec un fichier MANIFEST.MF, sans le classpath avec toutes les bibliothèques nécessaires, ni la classe à exécuter. Il n'est donc pas possible d'exécuter le JAR. Même en disant à Maven de renseigner comme il se doit le fichier MANIFEST, il resterait le problème que toutes les bibliothèques nécessaires à l'exécution du JAR ne sont pas embarquées dans le JAR
    - le plugin `spring-boot-maven-plugin` permet de faire tout cela automatiquement. Une étape supplémentaire de **repackage** a été effectué. Le JAR léger original a été suffixé avec `.original`, par exemple `toto-1.0.0.jar.original`. Un nouveau JAR, bien plus gros, appelé FAT JAR, a téé créé par le plugin, qui ne contient plus directement les classes du projet. Il y a un répertoire *BOOT-INF*, avec un répertoire *classes* où il y a les classes du projet et les fichiers de configuration, et également un répertoire *lib*, avec toutes les bibliothèques nécessaires au projet. Le fichier MANIFEST est également modifié pour fonctionner correctement
    - il suffit ensuite d'exécuter la commande `java -jar toto-1.0.0.jar`
- pour que tout cela fonctione, il faut utiliser le plugin `spring-boot-maven-plugin`, utiliser l'annotation `@SpringBootApplication`, et démarrer l'application avec `SpringApplication.run()`
- cela simplifie l'écriture, le build, le déploiemennt et l'exécution de l'application

## Les applications Web avec Spring Boot

### Spring Boot Web Starter

- une application web Java doit être servie par un serveur d'application web Java (conteneur de servlets), par exemple le serveur Apache Tomcat
- une application web est un ensemble de classes Java et de contenu web, zippé au format WAR (Web Archive)
- un serveur est démarré sur une machine, et il sert une ou plusieurs applications Java déployées sur le serveur
- avec Spring Boot, on démarre l'application Java qui embarque le serveur dans l'application web

Le starter `spring-boot-starter-web` permet de faire cela
- en démarrant l'application, on peut voir qu'un tomcat est démarré sur le port 8080
- création d'un répertoire `static` dans le dossier `resources` et création d'un fichier *index.html*, qui sera retourné en appelant l'URL racine `localhost:8080`

### Auto-configuration Spring

- la bibliothèque `spring-boot-starter-web` a une dépendance vers tomcat-embed
- `@SpringBootApplication` en plus de `@ComponentScan` et `@Configuration`, contient également `@EnableAutoConfiguration`
- avec `@EnableAutoConfiguration`, Spring va partir à la recherche dans les bibliothèques de notre projet de certaines classes annotées `@Configuration`, et cette classe de configuration va faire le travail
- il est possible de changer des réglages comme le port d'écoute du tomcat via les properties
- il est également possible de changer de serveur d'application, en utilisant par exemple *Undertow* à la place de *Tomcat*
    - il faut commencer par exclure la dépendance `spring-boot-starter-tomcat` de la dépendance `spring-boot-starter-web` afin qu'elle ne soit plus dans le classpath et que Spring n'essaie pas d'autoconfigurer l'application avec le serveur Tomcat
    - il faut ensuite ajouter une nouvelle dépendance `spring-boot-starter-undertow`

### Ressources Web statiques

- lors de la modification d'une ressource statique, elle n'est pas prise en compte sans redémarrer le serveur
- il est possible de configurer l'IDE pour rebuilder le projet automatiquement lorsqu'on fait une modification
- Spring DevTools permet de rafraîchir l'application de manière globale
- ces solutions fonctionnent en phase développement mais pas en production où un JAR est déployé
- il est possible de redéfinir la property `spring.resources.static-locations`, qui indique par défaut tous les emplacements qui sont considérés par Sring Boot pour servir du contenu statique (notamment /resources, /static). On va ajouter un emplacement qui se trouve en dehors du JAR, qui se trouve sur la machine. Exemple : `spring.resources.static-locations=file:C://temp/toto/,classpath:/static/`. Les modifications du fichier sont alors prises en compte

### Spring MVC : Le controleur frontal (Front Controller)

- Spring MVC est un framework web MVC, écrit en Java et utilise Spring. Il s'exécute côté serveur, et est dans le même esprit que Struts ou JSF
- il va faire appel à un contrôleur frontal pour attraper toutes les requêtes HTTP qui lui sont destinées. Il s'agit de la classe **DispatcherServlet**, fournie par `spring-webmvc`
- cette servlet dispatcher va attraper un ensemble de requêtes HTTP, par défaut toutes les requêtes. Il est possible de modifier cela pour ne capter qu'une certaine gamme de requêtes

### Spring MVC : Les controleurs Web

- la *DispatcherServlet* va s'adresser à un **Controller** en fonction de l'URL

```java
@Controller
public class MonController {
    @GetMapping("/hello")
    public String hello(){
        return "Hello";
    }
}
```

### Spring MVC : Les vues avec Thymeleaf

- une vue comporte des parties dynamiques
 - solution historique : génération du HTML côté serveur
 - solution AJAX : envoyer une page HTML incomplète et laisser Javascript récupérer les informations via des requêtes AJAX pour rafraîchir une partie de la page sans recharger la page complète

**Solution historique**
- pour générer sur le serveur une page HTML, il faut un **moteur de templating**. En Java EE, on utilise généralement les **JSP** (Java Server Pages). Cette technologie montre de plus en plus de limitations avec le temps. Spring recommande d'utiliser **Thymeleaf**
- il faut ajouter une dépendance pour pouvoir l'utiliser : `spring-boot-starter-thymeleaf`
- avec Thymeleaf, il est possible de conserver la page avec l'extension *html* alors qu'avec les JSP, il fallait utiliser l'extension *jsp*
- il faut adapter l'en-tête HTML en ajouter le namespace *thymeleaf* pour ajouter de nouvelles balises et attributs
```html
<!DOCTYPE html xmlns:th="http://www.thymeleaf.org">
<html lang="fr">
<head>...</head>
<body></body>
</html>
```
- ces fichiers ne sont plus statiques, il faut donc ne pas les mettre dans le dossier *static* mais dans un dossier **templates** dans le dossier *resources*
- pour utiliser un fichier `toto.html`, il faut le retourner dans une méthode d'un controller (fonctionnement par défaut pour faire le lien entre un controller et une vue)
```java
@GetMapping("/hello")
public String hello(){
    return "toto";
}
```

### Spring MVC : Affichage de données dans la vue - Le modèle

- création d'un jeu de données (le modèle) à transmettre à la vue pour l'afficher
- récupération via un service, qui lui même fait appel à un repository
- en Java EE classique, il est possible de mettre le jeu de données dans l'objet `HttpServletRequest` via sa méthode *setAttribute* et de le récupérer dans la vue, avec `th:each` de *Thymeleaf*
- s'il y a un seul attribut à retourner, possibilité que le controller le retourne et le place implicitement dans le scope *request* pour le récupérer dans la vue
    - par défault, Spring va alors nous rediriger vers la page HTML correspondant à l'URL dans *RequestMapping*
    - il faut utiliser l'annotation `@ModelAttribute` pour prévciser l'identifiant de l'objet passé dans la requête

```java
@Controller
public class MonController {
    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public String users(HttpServletRequest request){
        List<User> users = userService.findAll();
        request.setAttribute("users", users);
        return "toto";
    }

    @GetMapping("/user")
    public @ModelAttribute("users") List<User> users(){
        List<User> users = userService.findAll();
        return users;
    }
}
```

### Modularité

- sans le fichier de configuration XML, il est nécessaire de n'avoir qu'une implémentation annoté par Spring pour chaque interface pour éviter les conflits lors de l'injection de dépendance
- l'idée de Spring est de flexibiliser l'architecture, de pouvoir basculer d'une implémentation à une autre, en rendant l'application plus modulaire
- la première étape est de séparer le projet en 2 projets : un projet Core et un projet Web, qui contiendra le controller
- le projet Core n'a plus vocation a être exécuté, c'est une simple librairie. Il n'a plus vocation à être un projet SpringBoot mais un simple projet Spring avec `spring-context`
- déplacement des properties vers le projet Web qui doivent être définies par l'utilisateur de la bibliothèque, celles qui doivent être définies dans la librairie peuvent rester dans le Core. Possibilité de les laisser aux 2 endroits, le Core fourni alors des valeurs par défaut qui peuvnt être surchargées pour l'utilisateur de la bibliothèque
- les 2 projets ont le même *package racine*, par exemple `com.example.test`. En général, on ajoute un niveau de package supplémentaire au projet Core, par exemple `com.example.test.core`
- après avoir buildé le projet core et ajouté la dépendance dans le POM du projet Web, il faut que le chemin des packages du Core soit dans le chemin de l'annotation `@ComponentScan` du projet Web

### Paramètre de requête et préparation à ReST

- utilisation de `@PathVariable` pour récupérer une variable de chemin
- utilisation de l'objet **ModelAndView** qui permet de porter à la fois la vue vers laquelle se rediriger et un objet à utiliser dans la vue
- utilisation de l'objet **Model** qui ne porte que l'objet, et la méthode retourne alors le nom de la page html vers laquelle se rediriger

```java
@Controller
public class MonController {
    @Autowired
    private UserService userService;

    @GetMapping("/user/{id}")
    public ModelAndView user(@PathVariable Integer id){
        User user = userService.findById(id);
        ModelAndView mv = new ModelAndView("toto");
        mv.addObject("user", user);
        return mv;

    @GetMapping("/user/{id}")
    public String user(@PathVariable Integer id, Model model){ // Model doit être le dernier argument de la méthode
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "toto";
    }
}
```

### Spring MVC : Gestion de formulaire

### Validation des beans

### Affichage des erreurs de saisie


## Développer une API REST avec Spring

### Rappels : Web Services ReST

### ResponseBody, RequestBody et RestController

### Expérimenter les services ReST

### Exploiter les services ReST dans l’application


## Spring et les bases de données relationnelles

## Architectures "Cloud native" et microservices

### Introduction

- les architectures "Cloud native" sous-entendent la conception d'une application en microservices
- historiquement, conception d'**applications monoblocs**, qui contient tout ce qu'il faut pour remplir sa mission
- pour ce qui concerne les considérations transversales à différentes applications, il fallait souvent passer par des bases de données pour synchroniser les données entre les applications
- avec le temps, les applications ont commencé à exploiter des modules/logiciels qui ont vocation à assurer des fonctions génériques de l'entreprise (serveur LDAP, serveur d'authentification), ce qui permet de déléguer une partie des fonctionnalités communes à une application tierce. On parle alors d'**application d'entreprise**
- les applications migrent vers le navigateur web pour offrir des interfaces utilisateurs. L'ère du **SAS** pour **Software As A Service** a commencée
- des applications commencent à offrir leurs propres services via des API sur des protocoles standardisés notamment HTTP comme SOAP ou plus tard REST. C'est l'avénement des architectures orientées service, **SOA** pour **Service Oriented Architecture**
- en parallèle de ces évolutions logicielles, on voit émerger les environnements virtualisés comme AWS qui permettent d'héberger les applications offrant des services web. C'est le début de l'ère du **PAS** pour **Plateform As A Service**
- les applications exposent de plus en plus de service, certains sont plus importants que d'autres, méritent d'être plus sécurisés, redondant ou d'avoir une capacité de montée en charge variable
- arrive également la problématique des montées de version, monter de version toute une application pour un petit changement est contraignant
- l'idée des microservices est de répartir les fonctionnalités de l'application monolithique, dans de plus petites applications autonomes, qui vont interagir les uns avec les autres via leur web-services
- avec les microservices, les API Rest sont dédiées à d'autres applications internes, et non pas à des applications ou utilisateurs externes. Ces services sont souvent inaccessible de l'extérieur
- on va utiliser les capacités de la virtualisation pour répartir et orchestrer ces différentes unités autonomes. Le terme **cloud** apparaît en 2009 pour désigner ces applications distribuées sur plusieurs machines virtuelles
- en 2020, les architectures en microservices s'imposent. Ces services sont écris dans plusieurs langages, et répartis sur plusieurs serveurs ou même plusieurs infrastructures (Google Cloud, Azure, AWS...). On parle désormais de **IAAS** pour **Infrastructure As A Service**
- d'un point de vue applicatif, il va falloir concevoir les applications différemments : on va voir comment concevoir les microservices et comment les faire interagir dans un environnement cloud compatible

### Découpage en microservices
