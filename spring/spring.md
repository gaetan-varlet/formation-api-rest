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

Il est possible de se passer du fichier XML, mails il est également possible de l'utiliser de manière complémentaire en ajoutant une annotation `@ImportResource`

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

- un formulaire de saisie permet d'envoyer des informations depuis une page web vers un serveur
- possibilité de le faire avec *Thymeleaf*
- création d'une méthode Java qui va retourner vers la page html
- notion de **Backing Bean** : création d'une classe avec autant d'attributs que de champs dans le formulaire

```java
@GetMapping("/create-form")
public String formulaire() {
    return "toto"
}

@PostMapping("/create")
public String resultat(@ModelAttribute("form") User user) {
    userService.create(user);
    return "toto-created"
}
```

```html
<body>
<form th:action="@{/create}" method="POST"> <!-- renvoi à la méthode sur cette URL -->
    <p>Nom<input type="text" th:fiel="*{nom}"></p>
    <p>Prénom<input type="text" th:fiel="*{prenom}"></p>
    <input type="submit" value="OK">
</form>
</body>
```


### Validation des beans

- contrôle que l'objet envoyé par l'utilisateur est correct, par exemple le nombre de caractères d'une chaîne caractère
- Spring MVC est de vérifier les valeurs dans le *Backing Bean* après collecte de ces valeurs. Il faut donc soumettre le formulaire au serveur, qui va retourner les potentielles erreurs
- En Java, lorsqu'on souhaite valider les propriétés d'un *Bean*, il y a un standard : les JSR 303 et 380, appelés **Jakarka Bean Validation**
- **hibernate-validator** est l'implémentation de référence
- ajout su starter **spring-boot-starter-validation**, et ajout des critères de validité grâce à des annotations sur les propritétés des *Bean* à valider
- création d'un bean différent pour la validation du bean et celui qu'on enregistre en base, puis conversion de l'objet pour l'envoyer dans la couche inférieure
- ajout d'une annotation `@Valid` lors de la récupératio de l'objet dans le controller

```java
public class UserForm {
    @NotBlank(message = "Le nom est obligatoire") // non null et non vide
    private String nom;
    @Size(min=10,max=13)
    private String prenom;

    // Autre annotation
    @Pattern(regexp = ...)
    // D'autres annotation spécifiques à Hibernate Validator
    @CreditCardNumber
    private String creditCardNumber;
}

@PostMapping("/create")
public String resultat(@Valid @ModelAttribute("form") User user) { ... }
```

- collecte des erreurs dans un objet `BindingResult` et possibilité de retourner vers une autre page s'il y a des erreurs

```java
@PostMapping("/create")
public String resultat(@Valid @ModelAttribute("form") User user, BindingRestult) {
    if(results.hasErrors()){ ... }
    return "toto";

```

### Affichage des erreurs de saisie

- possibilité d'ajouter une `<div>` dans la vue en dessous de chaque champ de saisie, qui va s'affichier uniquement s'il y a une erreur

```html
<p>Nom<input type="text" th:fiel="*{nom}"></p>
<div th:if="${#fields.hasErrors('nom')}" th:errors="*{nom}"></div>
```

## Développer une API REST avec Spring

### Rappels : Web Services ReST

- on parle de service web REST lorsque l'URL utilisée pour contacter le serveur s'écrit sous une forme standard qui indique le changement d'état que va subir la ressource ciblée
- par exemple, `/user` en *GET* est censé renvoyer les informations relatives aux utilisateurs
- format de données utilisé : XML / JSON
- standard Java : JAX-RS avec Jersey comme implémentation de référence
- Spring propose une alternative avec Spring MVC, qui n'est pas une implémentation de JAX-RS

### ResponseBody, RequestBody et RestController

- transformation d'un controller en ressource *REST* en renvoyant un objet
- il faut ajouter l'annotation `@ResponseBody` pour le retourner sans passer dans une vue1
- l'objet va être converti en XML ou en JSON (par défaut), via spring-boot-starter-json (qui ramène Jackson) : configurable de manière fine via le concept de **Content Negociation**
- pour une méthode de création ou l'objet est envoyé par l'utilisateur, il faut ajouter l'annotation `@RequestBody` pour qu'il soit converti du JSON vers un objet Java
- pour éviter de dire que les sorties doivent être converties en JSON avec `@ResponseBody`, on peut remplacer `@Controller` par `@RestController`

```java
@Controller
@RequestMapping("/user")
public class UserController {
    @GetMapping
    @ResponseBody
    public List<User> getAll(){
        return userService.getAll();
    }

    @PostMapping
    @ResponseBody
    public User create(@RequestBody User user){
        return userService.save(user);
    }
}
```

### Expérimenter les services ReST

- pour utiliser l'API REST, il va falloir adapter les pages HTML avec un peu de javascript afin que l'application soit plus réactive via des requêtes AJAX en arrière-plan et mettre à jour le contenu du DOM sans rechargement de la page
- créer une API peut avoir d'autres objectifs comme d'offrir à d'autres applications la possibilité d'interagir avec nos services

### Exploiter les services ReST dans l’application

- l'idée est de ne pas attendre que toutes les données soient prêtes pour afficher la page (on parle d'asynchrone)
- cela va permettre que la page s'affiche très vite mais incomplète avec des indicateurs qui montrent que le chargement des éléments est en cours
- comportement courant sur la plupart des sites modernes

## Spring et les bases de données relationnelles

### Présentation

- étude de l'interaction avec les données, plus particulièrement sur l'exploitation de **base de données relationnelles** en utilisant **JDBC**, **Hibernate** et **JPA**, dans le cadre d'un sous-projet de Spring : **Spring Data**
- possibilité d'utiliser la base de données en mémoire **H2**, ou une base de données à installer comme **MySQL** ou **PostgreSQL**
- installation de MySQL Community Edition
    - en **standalone MySQL Server**
    - création d'un compte root et éventuellement de compte utilisateurs avec des droits d'accès restreint pour utiliser dans l'application
- installation d'un client de d'accès aux BDD : **DBeaver**
    - il est compatible avec tous les moteurs de BDD
    - pour se connecter à un serveur de BDD, DBeaver va utiliser un driver JDBC (une bibliothèque Java) qu'il va télécharger via Maven

### Introduction à Spring JDBC

- pour utiliser une base MySQL, il faut ajouter dans le projet java le driver MySQL, en scope *runtime* car les classes du driver ne sont pas utilisées directement dans le code de l'application
- il faut également une bibliothèque pour établir une connexion avec la base, par exemple **Spring JDBC** (`spring-boot-starter-jdbc`), à ne pas confondre avec **Spring Data JDBC**
- utilisation d'une **DataSource** pour obtenir une connexion à la base, instanciée par Spring
- configuration via un *Bean* de configuration

```java
@Bean
public DataSource getDataSource(){
    DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName("com.mysql.jdbc.Driver");
    dataSourceBuilder.url("");
    dataSourceBuilder.username("");
    dataSourceBuilder.password("");
    return dataSourceBuilder.build();
}
```

- pour éviter de passer par un Bean de configuration, il est possible de créer un Bean de configuration automatiquement en passant par des properties

```properties
spring.datasource.url=jdbc:mysql:...
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

- s'il y a plusieurs BDD, il faudra utiliser des Bean de configuration
- le pool de connexion créé par défaut est de type **HikariCP**

### Utilisation de JdbcTemplate

- le fait d'avoir une classe annotée `@Repository` donne accès à un objet `JdbcTemplate`, qui permet d'effectuer des requêtes, en exploitant la *Datasource*, en récupérant une connexion, et gérer les potentielles erreurs
- exemple de récupération d'une liste d'objets avec la méthode `query(String requete, RowMapper<User> rowMapper)`, avec un rowMapper qui itère sur le ResultSet et crée des objets en conséquence

```java
@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM USER", (rs, rowNum) -> new User(rs.getInt("ID"), rs.getString("NOM"),
                rs.getString("PRENOM"), rs.getString("EMAIL"), rs.getInt("AGE")));
    }
}
```

- exemple de récupération d'un seul objet en fonction de son id avec la méthode `queryForObject(String requete, Object[] args, RowMapper<User> rowMapper)`

```java
public User findById(Integer id) {
    return jdbcTemplate.queryForObject("SELECT * FROM USER WHERE id= ?", new Object[] { id },
        (rs, rowNum) -> new User(rs.getInt("ID"), rs.getString("NOM"), rs.getString("PRENOM"), rs.getString("EMAIL"), rs.getInt("AGE")));
}
```

- exemple de requête générique pour effectuer une mise à jour avec `update()` (insert, update, delete), proche de `executeUpdate()` en JDBC natif
- pour récupérer l'identifiant auto-généré, il faut passer par une instance de `PreparedStatement`

```java
public User create(User user) {
    // objet qui permet de récupérer la clé autogénéré
    KeyHolder kh = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
        // RETURN_GENERATED_KEYS indique que le PS doit être utilisé pour récupérer les
        // clés autogénérés
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO USER(id, nom, prenom, email, age) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, user.getId());
        ps.setString(2, user.getNom());
        ps.setString(3, user.getPrenom());
        ps.setString(4, user.getEmail());
        ps.setInt(5, user.getAge());
        return ps;
    }, kh);
    // récupération de la clé autogénéré et affectation à l'id de l'objet
    user.setId(kh.getKey().intValue());
    return user;
}
```

### Introduction à Spring Data et Spring Data JDBC

- Spring JDBC est assez flexible, mais la contrepartie est que c'est assez verbeux
- on aimerait écrire moins de code sans forcément utiliser un ORM (Hibernate) car utiliser Hibernate est identifié comme la principale cause d'échec de projets Java
- il existe une solution qui permet de se limiter à JDBC tout en écrivant moins de code : **Spring Data JDBC**, qui est un sous-ensemble de **Spring Data**
- **Spring Data** propose une manière d'aborder le concept de *Repository* commune à toute solution technologique de persistance (JDBC, JPA, NoSQL...)
- cette couche d'abstraction exploite intensivement le principe de **Convention Over Configuration**, ce qui veut dire que plutôt d'ajouter des fichiers XML ou des annotations pour expliquer à Spring comment l'application fonctionne, on va écrire le code de manière standardisé, notamment avec une orthographe précise, ce qui va alléger le code
- ces principes sont très intéressants au niveau des *Repository* car ces derniers contiennent toujours la même chose : des méthodes de CRUD
- pour **Spring Data JDBC**, il s'agit d'appliquer les principes de *Spring Data* à *JDBC*
    - on peut le voir comme un *ORM* beaucoup plus léger, sans notion de cache, de lazy-loading, ou de flush...
    - c'est un projet assez récent, plus récent que Spring Data JPA
- il faut bien maîtriser le *Domaine Driver Design* (**DDD**), car il est imposé par *Spring Data JDBC*
    - lE DDD est inévitable pour modulariser une application, ou encore pour les microservices
    - impact sur la façon d'écrire les entités (liens entre les entités seraient matérialisés par des identifiants et non des objets)

### Spring Data JDBC : Model et Repository

- il faut utiliser le starter `spring-boot-starter-data-jdbc`
- il faut annoter l'identifiant de notre entité avec `@Id` de Spring Data, psa besoin de `@Entity` comme en JPA
- par défaut, le camel case des propriétés Java est converti en snake case (monNom -> mon_nom). On parle de **NamimgStrategy**, qu'on peut changer
- s'il y a des exceptions, on peut utiliser `@Column`
- création d'une interface pour le repository qui étend `PagingAndSortingRepository` ou `CrudRepository`
- grâce à `@EnableJdbcRepositories` dont on bénéficie automatiquement grâce au starter, Spring va retrouver toutes les interfaces qui étendent notre interface et une nouvelle classe qui implémente l'interface va être écrite à la volée et injectée dans les services

```java
public class User {
    @Id // import org.springframework.data.annotation.Id;
    private Integer id;
    @Column("name")
    private String nom;
    private String prenom;
}

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    // PagingAndSortingRepository ou CrudRepository ou Repository
}

// Exemple d'utilisation
userRepository.findAll();
userRepository.findById(id);
userRepository.save(user);
```

### Spring Data JPA avec Hibernate

- **Spring Data JPA** est l'équivalent de *Spring Data JDBC* mais pour **JPA**
- il s'agit d'exploiter via Spring Data l'API de persistance de Java EE JPA
- utilisation du starter `spring-boot-starter-data-jpa`, qui embarque implicitement l'annotation `@EnableJpaRepositories`
- utilisation des annotations de JPA
- utilisation de la même interface que Spring Data JDBC : `CrudRepository`, ou `JpaRepository` qui propose des méthodes en plus
- au démarrage de l'application, grâce à SpringBoot qui a fait des choix, on peut voir que :
    - JPA a été utilisé avec l'instanciation de l'EntityManagerFactory avec un *persistence unit* qui a été appelé **default**
    - l'implémentation de JPA qui a été utilisé est Hibernate
    - un dialect a été choisi pour nous
- tous ces choix peuvent être changés
- ajout de properties pour voir les requêtes SQL dans la log : `spring.data.show-sql=true` et `spring.jpa.properties.hibernate.format_sql=true`
- les ORM ont la capacité de générer les tables du modèle de données au démarrage de l'application sur la base des annotations placées sur les *Entity*. Avec Hibernate, c'est le mode **ddl-auto**
- Avec Spring Data, c'est le cas par défaut seulement si c'est une base de données mémoire comme H2 ou HSQL
- il est possible d'utiliser des fichiers **schema.sql** et **data.sql** pour générer la base de données. Il faut alors également utiliser la property `spring.jpa.hibernate.ddl-auto=none` pour ne pas qu'Hibernate génère lui-même la base et ignore nos fichiers

```java
@Entity
public class User {
    @Id // import javax.persistence.Id;
    private Integer id;
    private String nom;
    private String prenom;
}
```

### Spring Data JPA : Modèle de données plus complexe

![Modèle de données](/spring/modele-donnees.png "Modèle de données")

Script de BDD correspondant :

```sql
CREATE TABLE IF NOT EXISTS ADDRESS  (
  ID BIGINT NOT NULL AUTO_INCREMENT,
  STREET VARCHAR(50) NOT NULL,
  STREET_NUMBER VARCHAR(10),
  CITY VARCHAR(20) NOT NULL,
  ZIP_CODE VARCHAR(10) NOT NULL,
  COUNTRY VARCHAR(20) NOT NULL,
  PRIMARY KEY(ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS CUSTOMER  (
  ID BIGINT NOT NULL AUTO_INCREMENT,
  NAME VARCHAR(50) NOT NULL,
  ID_ADDRESS BIGINT NOT NULL,
  PRIMARY KEY(ID),
  FOREIGN KEY(ID_ADDRESS) REFERENCES ADDRESS(ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS PRODUCT  (
  ID BIGINT NOT NULL AUTO_INCREMENT,
  NAME VARCHAR(50) NOT NULL,
  PRIMARY KEY(ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS CATEGORY  (
  ID BIGINT NOT NULL AUTO_INCREMENT,
  NAME VARCHAR(50) NOT NULL,
  PRIMARY KEY(ID)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS PRODUCT_CATEGORIES  (
ID_PRODUCT BIGINT NOT NULL,
ID_CATEGORY BIGINT NOT NULL,
PRIMARY KEY(ID_PRODUCT, ID_CATEGORY),
FOREIGN KEY(ID_PRODUCT) REFERENCES PRODUCT(ID),
FOREIGN KEY(ID_CATEGORY) REFERENCES CATEGORY(ID)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS INVOICE  (
  INVOICE_NUMBER BIGINT NOT NULL AUTO_INCREMENT,
  /*CUSTOMER_NAME VARCHAR(50) NOT NULL,*/
  ORDER_NUMBER VARCHAR(13),
  ID_CUSTOMER BIGINT NOT NULL,
  PRIMARY KEY(INVOICE_NUMBER),
  FOREIGN KEY(ID_CUSTOMER) REFERENCES CUSTOMER(ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS INVOICE_LINE  (
  ID BIGINT NOT NULL AUTO_INCREMENT,
  QUANTITY SMALLINT NOT NULL,
  ID_PRODUCT BIGINT NOT NULL,
  INVOICE_NUMBER BIGINT NOT NULL,
  PRIMARY KEY(ID),
  FOREIGN KEY(INVOICE_NUMBER) REFERENCES INVOICE(INVOICE_NUMBER),
  FOREIGN KEY(ID_PRODUCT) REFERENCES PRODUCT(ID)
) ENGINE=InnoDB;
```

### Spring Data JPA : Jackson et l'anti-pattern Open Session In View (OSIV)

- lorsqu'une entité contient elle-même une autre entité qui est chargé en *Lazy Loading*, cette dernière n'est pas récupéré par l'ORM, elle va être sous forme de **HibernateProxy**, qui ne contient que l'identifiant de l'entité
- Jackson veut convertir cette sous-entité en JSON. Lorsqu'on accède aux propriétés d'un proxy Hibernate en lecture, l'ORM va vouloir relancerde nouvelles requêtes vers la base pour obtenir de nouvelles informations
    - si la session (EntityManager) qui a servi a faire la lecture est fermée, il va y avoir une `LazyInitializationException` qui dit qu'il n'est pas possible d'effectuer ces requêtes hors session
    - si la session est ouverte (ce qui est le cas avec Spring Boot, on parle de **OpenSessionInView** ou **OpenEntityManagerInView**), des requêtes complémentaires sont effectuées et le proxy est complété des nouvelles informations obtenues
    - dans le cas où la session est ouverte, il y a quand même une erreur car Jackson essaie de convertir également les propriétés des proxy, comme **HibernateLazyInitializer**, alors que cette propriété n'intéresse pas le client. Il faut donc dire à Jackson de ne pas transformer cette propriété en ajoutant `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` sur tous les objets *Entity* qui posent problème (une autre propriété `handler` pose le même problème)
    - en faisant cela, Jackson va déproxyfier tous les sous-objets et faire énormément de requêtes, ce qui va ramener probablement plus d'informations qu'on a besoin, ce qui va probablement dégrader les performances
- une solution est d'utiliser le **paradigme DTO** (Data Transfer Object) qui va consister dans la couche de service à convertir les *Entity* en d'autres objets taillé sur mesure pour les interfaces utilisateurs avec que les propriétés uniquement nécessaires. Cette solution est très populaire mais assez verbeuse

### Nullifier les proxy avec Jackson Hibernate5Module

- une autre solution, est de continuer à renvoyer des *Entity*, mais que leurs propriétés soient mises à null avant que ces entités soient transformées en JSON
- pour cela, il faut ajouter la dépendance `jackson-datatype-hibernate5`, et ajouter un *Bean* de configuration `Hibernate5Module`
- on peut maintenant enlever les `JsonIgnoreProperties`, car les proxys seront mis à null
- avec cette solution, on ne récupère plus du tout les sous-entités. Pour choisir, ce qu'on veut récupérer, il y a 2 solutions :
    - **initialisation des proxy à postériori** : déproxifier les proxys qui nous intéresse avant transformation en json. C'est la solution plus flexible mais moins performante
    - **fetch à priori** : faire des requêtes plus adaptées, pour lire les informations qui nous intéresse lors de la requête original. C'est la solution la plus performante mais pas la plus flexible

```java
@Bean
public Hibernate5Module hibernateModule(){
    return new Hibernate5Module();
}
```

### Solution 1 : La déproxification à postériori (N+1 Select)

- la bonne pratique est de déproxifier dans la couche de service, en appelant les sous-objects, ce qui va forcer l'ORM à initialiser le proxy
- cette solution est flexible, car on choisit dans la couche de service quelles sont les propriétés qu'on va initialiser
- cependant, cela va faire beaucoup de requêtes secondaires, ce qui est dangereux pour les performances de l'application

```java
List<Invoice> invoices = invoiceRepository.findAll();
// ceci permet d'initialiser le client de chaque facture
invoices.forEach(invoice -> invoice.geCustomer().getName());
```

### Solution 2 : Les fetch à priori dans les repository (@Query ou @EntityGraph)

- solution qui consiste à ne pas initialiser les proxys dans la couche de service, mais réaliser des requêtes adaptés au niveau des repository
- exemple de redéfinition de la méthode `findAll()` de `InvoiceRepository` qui va aller chercher les clients en faisant une jointure

```java
@Query("SELECT invoice from Invoice invoice inner join fetch invoice.customer")
Iterable<Invoice> findAll();
```

- cette méthode fonctionne bien mais c'est dommage de devoir écrire un `@Query` pour forcer la requête à faire la jointure
- il existe une deuxième méthode sans écrire la requête en utilisant **EntityGraph**

```java
@Entity
 // nommage pour l'utiliser ailleurs
 // déclaration qu'il est possible de faire une requête sur invoice avec un fetch sur customer si cela est explicitement demandé
@NamedEntityGraph(name = "invoice.customer", attributeNodes = @NamedAttributeNode("customer"))
public class Invoice{
}

// dans le repo, utilisation de @EntityGraph avec le nom de l'entityGraph qu'on veut suivre
// le type de requête qu'on veut effectuer : FETCH ou LOAD
@EntityGraph(value = "invoice.customer", type = EntityGraph.EntityGraphType.FETCH)
Iterable<Invoice> findAll();
```

### Open Session / EntityManager in View, est-ce une bonne idée ?

- chaque solution proposée au problème de déproxyfication à pour effet de ne plus déclencher aucune requête SQL lors de la transformation des entités en JSON
- il n'est donc plus nécessaire d'avoir une session ou un EntityManager ouvert dans le controller
- il est donc possible de passer la property `spring.jpa.open-in-view` à false, ce qui est contraire au comportement par défaut de Spring Boot
- dans la log, il est indiqué sous forme de warning, que pour que Spring Boot soit simple à utiliser, le paradigme **open-session-in-view** est activé, au risque de déproxyfier les données et de faire chuter les performances
- la bonne pratique est plutôt de mettre la property à false

### Spring Data JPA : Ecriture en base de données et introduction à la gestion transactionnelle

- lorsqu'on enregistre une entité, si elle contient une autre entité, il faut alors enregistrer au préalable cette entité
- il est possible de le faire automatiquement en mettant dans l'annotation `@ManyToOne(cascade = CascadeType.PERSIST)`, mais il vaut mieux ne pas le faire entre 2 aggrégats
- si la première requête a fonctionné et que la deuxième échoue, il ne faut pas que la première requête soit persistée : on parle de **gestion transactionnelle**
- pour rendre une méthode transactionnelle, il faut l'annoter `@Transactional`. Une transaction est ouverte avant l'exécution de la méthode, et un commit sera effectué à la fin de la méthode si tout c'est bien passé. Si une exception survient, un rollback est effectuée
- lors d'une erreur dans une transaction, JDBC reçoit une `SQLException` que Spring encapsule dans une `DataAccessException`
- pour comprendre correctement le fonctionnement des transactions avec Spring, il faut étudier le développement orienté aspect (AOP with Spring)


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

- monolithe : un seule livrable à déployer
- découper l'applicaiton en plusieurs petites applications autonomes capables d'interagir avec les autres
- logique à suivre pour faire ce découpage :
    - la notion d'**Agrégat** va être importante, on peut éventuellement faire un microservice par Agrégat
    - on peut aussi s'intéresser à la notion de montée en charge d'une partie de l'application
    - également à la notion de montée de version de chaque partie
    - plus il va y avoir de microservices, plus il va y avoir d'échanges en web services pour effectuer une opération, plus les performances s'en ressentent
- il ne faut pas forcément faire un module maven par microservice
    - l'organisation du code source est un problème distinct
    - il est par exemple possible de converser un module core pour partager du code, et une application Spring Boot par module à déployer
- chaque application va avoir son serveur d'application : il faut penser, en local, à es démarrer sur des ports différents
- Martin Fowler, qui a popularisé le concept d'inversion de contrôle, conseille d'écrire dans un premier l'application sous forme de monolithe, avant de la découper en microservices

### Les entites métier et les données

- les classes `Entity` sont utilisées par plusieurs microservices : ceux qui fournissent les sercives, et ceux qui les consomment. Il vaut mieux laisser les classes dans un module `core` pour éviter les dépendances entre microservices
- précision à Spring Boot quels packages scannés avec l'annotation `@EntityScan("")` pour ne scanner que les packages contenant les entités nécessaires à ce microservice. En général, lorsqu'on travaille en **DDD**, les entités sont répartis dans des packages relatif à l'agrégat
- il faut se servir des principes du **DDD**, et casser les relations entre objets qui ne font pas parti du même agrégat, car on ne peut plus mappper une propriété qui n'est pas géré par le microservice. Il est en revanche possible d'ajouter une propriété qui va référencer l'identifiant de cet objet
- chaque microservice va être responsable de ses données et aura sa propre base

### Premier endpoint (micro)service

- création d'un endpoint qui permet de récupérer les informations d'un client pour son identifiant dans le microservice *Client*
- ce service sera appelé par le microservice *Facture* pour récupérer les informations du client associé à la facture

### Préparation du microservice consommateur

- récupération du client correspondant à l'idClient de la facture
- ajout d'une option à notre bean `Hibernate5Module` pour que la transformation en JSON par Jackson se fasse sur les propriétés annotées `@Transient`, car elle ne se fait pas par défaut avec *Hibernate5Module*

```java
@Bean
public Hibernate5Module hibernateModule(){
    Hibernate5Module module =  new Hibernate5Module();
    module.disable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION);
    return module;
}
```

### Exploiter le service avec RestTemplate

- récupération des informations sur le *Client* dans le microservice *Facture* en faisant une requête HTTP sur le microservice *Client*

### Associations entre entites et API ReST

- Le problème des associations entre entités avec les API ReST
    - association inter services (par exemple facture et client)
        - s'il y a plusieurs clients, possibilité de faire une seule requête HTTP pour récupérer les infos de tous les client en une seule requête pour gagner du temps
        - généralement, les microservices sont déployés au même endroit, ce qui fait que les temps de communication (serveur à serveur), est réduit
    - association intra services (par exemple client et adresse)
        - possibilité d'ajouter un `@EntityGraph` pour récupérer le sous-objet lors de la requête SQL
        - cependant, cela récupérera le sous-objet à chaque fois qu'on appelle le microservice, ce qui n'est pas forcément nécessaire
        - autre possibilité : créer un autre endpoint pour récupérer l'adresse du client
        - si on veut récupérer l'id de l'adresse dans l'objet *Adresse* de l'objet *Client*, il faut configurer `Hibernate5Module` pour qu'il intialise l'objet *Adresse* (nul par défaut) en renseignant l'id récupéré dans dans la table *Client*

```java
module.enale(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
```

### Spring cloud et Client Side Service Discovery avec Netflix Eureka

- lors de  l'appel à des microservices, il ne faut pas que les URL soient marqués en dur, surtout dans un environnement cloud, où les emplacements serveurs sont inconnus et peuvent changer régulièrement
- de plus, acec les microservices, avec la répartition de la charge (**Load Balancing**), un microservice va exister en plusieurs exemplaires sur plusieurs serveurs, donc derrière plusieurs URL
- les URL changent aussi selon l'environnement (développement, qualification, production...)
- pour résoudre ces problèmes, on va faire appel à la technique de **Service Discovery**, mis en oeuvre par un **discovery server**
- **Spring Cloud** met en oeuvre une technique appelée **Client Side Service Discovery**, qui permet de rechercher et découvrir des URL dynamiquement, en fournissant un nom symbolique associé à ces URL (comme les serveurs DNS)
- il existe de multiples solutions, Spring permet d'interagir avec la technologie de notre choix grâce à une couche d'abstraction
- utilisation de la solution la plus populaire : **Eureka**, solution opensource de Netflix
    - **Eureka Server** va gérer la partue *discovery server*
    - **Eureka Client** va gérer la partie *microservice*
- seul les microservices fournisseurs de services doivent s'enregistrer auprès du *discovery server*
- le *Eureka Server* va être une application autonome qui ne sert qu'à faire office de *Service Discovery*
    - création d'une application Spring Boot qui exploite la bibliothèque *Eureka Server*
    - ajout de l'annotation `@EnableEurekaServer`, sur la classe contenant le main de l'application par exemple
    - avant de démarrer l'application, il faut désactiver le fait que l'application, en plus d'être un serveur Eureka, est aussi un client Eureka, et va donc essayer de s'enregistrer auprès du serveur. Ajout de properties :
    ```properties
    eureka.client.register-with-eureka=false
    eureka.client.fetch-registry=false
    ```
    - serveur d'application web, fonctionnant avec Tomcat : possibilité de changer le port et d'utiliser le port standard pour les *discovery server* : `server.port=8761`
    - une interface utilisateur est également fournie à la racine du serveur, et donne des informations notamment la liste des clients du serveur actuellement enregistrés

### Enregistrement des clients du discovery server

- seul les microservices fournisseurs de services doivent s'enregistrer auprès du *discovery server* (par exemple, Client et Produit)
- le microservice *Facture*, qui consomme ses services, n'a pas besoin de s'enregistrer auprès du *discovery server*
- toutefois, ce microservice peut s'enregistrer si on estime que ces services pourraient être exploités par une autre application
- ajout d'une dépendance dans chaque microservice pour interagir avec le *discovery server* : **Eureka Discovery Client**
- il n'est plus nécessaire d'ajouter l'annotation `@EnableEurekaClient` grâce à l'autoconfiguration
- l'application va automatiquement essayer de s'enregistrer auprès du *discovery server*. Il faut juste préciser l'emplacement de ce serveur via la property : `eureka.client.service-url.default-zone=http://localhost:8761/eureka/`
- ajout d'une property pour nommer l'application `spring.application.name=toto`, pour qu'on la reconnaisse sur le *discovery server*

### Obtenir l'emplacement d'un microservice avec @LoadBalanced

- maintenant que les services sont connus du *discovery server*, il va aider les clients à les contacter via leur emplacement réel
- dans les clients appelant ses microservices, au lieu de marquer l'hôte et le port du microservice, par exemple `http://localhost:8080/customer`, il faut marquer le nom de l'hôte en question à la place : `http://customer-service/customer`
- il faut également ajouter l'annotation `@LoadBalanced` pour que ça fonctionne sur le `WebClient` utilisé pour faire les requêtes HTTP
- le *discovery server* va alors être contacté pour obtenir l'adresse réelle sans rien avoir à faire

### Expérimenter le load balancing côté client

- ce mécanisme permet également de mettre en place un *load balancing* de microservices
- de base, une seule instance de chaque microservice est démarré
- il est possible de démarrer via un IDE ou en ligne de commande une deuxième instance d'un microservice en changeant le port
- notre microservice client va alors appeler l'une des instances, on parle de mécanisme de **répartition de charge**, qui est ici à l'initiative du client

### Programmation réactive et WebClient

- la classe **WebClient**, qui est une alternative à **RestTemplate**, car ce dernier est voué à disparaître
- **WebClient** exploite la programmation réactive
- **Spring Reactive Web** apporte la bibliothèque **spring-boot-starter-webflux**
- au lieu de renvoyer une liste d'objets, un service va envoyer un flux `Flux<T>` qui va être complété de manière asynchrone
- possibilité de faire des requêtes HTTP en parallèle, grâce au multithreading
