# Spring et Spring Boot

## Introduction

Spring :
- l'idée est de généraliser à toute application un modèle de programmation particulier : l'inversion de contrôle
- configuration en XML
- Spring est souvent en avance par rapport aux solutions proposées par Jave EE
- Spring est aujourd'hui incontournable pour les applications Java EE
- Spring structure le code d'une application, le rend évolutif
- Spring Boot facilite le déploiement des applications Java


## Rappel : Librairies Java et introduction à Maven

- bibliothèques : un fichier compressé au format **jar** (Java Archive) contenant un ensemble de classes Java compilées, organisées en package
- le site *Maven Repository* permet d'obtenir des bibliothèques Java
- lorsqu'on utilise une bibliothèque, il faut également ajouter les dépendances de cette bibliothèque (et éventullement les dépendances des dépendances)
- utilisation d'une bibliothèque Java sans Maven/Gradle : il faut télécharger le JAR. Et dans la plupart des IDE, il est possible d'ajouter un JAR externe au projet, ce qui ajoute automatiquement le JAR dans le *classpath* du projet, et il pourra alors être utilisé dans le projet
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

## Spring Initializr
