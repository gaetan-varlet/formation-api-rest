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
### Configuration par annotation
### Valorisation des propriétés par annotation : @Value et fichier de propriétés
### Détection automatique des beans
### Gérer les conflits de dépendances
### Se passer du fichier XML
### Les classes de configuration plus en détail


## Mise en place de Spring Boot

## Les applications Web avec Spring Boot

## Développer une API REST avec Spring

## Spring et les bases de données relationnelles

## Architectures "Cloud native" et microservices
