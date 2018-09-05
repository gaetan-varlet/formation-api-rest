# Les expressions régulières

----

## Introduction

Les expressions régulières, ou regex, permettent de rechercher des motifs décrits par la combinaison d'opérateurs et de valeurs. Une utilisation classique consiste à la recherche de mots-clés ou encore de vérificr des données saisies par l'utilisateur pour s'assurer qu'elles respectent un format prédéfini.

Java fourni dans son JDK depsuis la version 1.4 une API standard permettant la manipulation d'expressions régulières. L'API doit être importée comme suit : `import java.util.regex.*;`

Il existe deux classes:
- `Pattern` : cette classe a por onbjet de compiler l'expression régulière fournie
- `Matcher` : cette classe permet de comparer une expression régulière à un texte, et de faire différentes opérations dessus

----

## Premier exemple
- la façon la plus simple d'utiliser une expression régulière est d'utiliser la méthode statique `matches()` de la classer `Pattern`
- une façon plus complexe de l'écrire, mais équivalente, est de passer par un objet `Matcher`
- la première méthode est adaptée aux cas les plus simples, la seconde permet de réutiliser l'objet matcher, et de faire des opérations plus complexes qu'une simple recherche.

```java
String texte =  "Quand le ciel bas et lourd" ;  // texte à tester    
boolean b = Pattern.matches("a.*", texte) ;
```
```java
String texte =  "Quand le ciel bas et lourd" ;  // texte à tester    
Pattern p = Pattern.compile("a.*") ;      
Matcher m = p.matcher(texte) ;    
boolean b = m.matches() ;
```
le booléen *b* sera vrai si *texte* contient une chaîne de caractères commençant par la lettre *a*

----

## Classe Pattern

- la méthode statique `compile()` permet de créer un *Pattern*. Elle prend une expression régulière en paramètre, et un flag optionnel
- `flags()` retourne les éventuels flags déclarés sur le pattern, `pattern()` et `toString()` retourne le pattern proprement dit
- `matcher(String)` permet de comparer les textes passés en paramètre avec cette expression régulière
- `split(String)` et `split(String, int)` permettent de découper un texte en fonction de l'expression régulière en paramètre. Elles retournent un tableau de String, limité au nombre de cases éventuellement passé en paramètres

----

## Classe Matcher

La classe `Matcher` permettent de faire des opérations plus sophistiquées
- `matches()` permet de texter si le texte correspond à l'intégralité de l'expression régulière
- `lookingAt()` cherche si le texte commence par le pattern fourni
- `find()` examine le texte et recherche les occurences du pattern dedans. Des appels successifs à cette méthode permettent de balayer l'ensemble du texte recheché
- `replaceAll()` et `replaceFirst()` permettent de remplacer un morceau de texte par un autre
```java
String texte =  "un - deux - trois - quatre" ;  
String texteRemplace = Pattern.compile("-").matcher(texte).replaceAll(";"); // un ; deux ; trois ; quatre
```

----

##  Notion de classe

- une expression régulière est une chaîne de caractères. Le premier principe est qu'un caractère se représente lui-même, ainsi le **pattern** *bonjour* représente simplement le mot *bonjour*
- il est possible d'ajouter des **caractères spéciaux** à un pattern pour enrichir ce qu'il représente
  - le pattern `a*` représente toutes les chaînes de caractères constitués d'un nombre quelconque de *a*, y compris la chaîne vide. Ajouter `*` à un pattern signifie qu'il peut se répéter
  - `.*` représente toutes les chaînes de caractères, y compris la chaîne vide. `.` représente n'importe quel caractère
- il est possible de définir des **classes de caractères**, définie par une chaîne de caractères écrite entre crochets
  - `.` représente un caractère quelconque
  - `[abc]` représente un unique caractère qui peut être a, b ou c
  - `[^abc]` représente un unique caractère qui peut prendre toutes les valeurs sauf a, b et c
  - `[a-z]` ou `\p{Lower} ` représente une minuscule
  - `[a-zA-Z]` ou `\p{Alpha}` représente un unique caractère alphabétique, minuscule ou majuscule
  - `[a-gmn]` représente un unique caractère alphabétique compris entre a et g, de m et de n
  - `[0-9]` ou `\d` représente n'importe quel chiffre
  - `\s` représente un caractère blanc (espace, tabulation, retour-chariot...), `\S` un caractère non blanc

----

## Les quantifieurs

Das ces exemples, X représente une classe quelconque
- `X?` où X apparaît 0 ou 1 fois
- `X*` où X apparaît un nombre de fois quelconque, 0 fois et plus
- `X+` où X apparaît 1 fois et plus
- `X{n}` où X apparaît exactement n fois
- `X{n,}` où X apparaît au moins n fois
- `X{n, m}` où X apparaît au moins n fois, et au plus m fois (inclus)

## Les opérateurs logiques

- `XY` signifie X suivi de Y
- `X|Y` signifie doit appartenir à la classe X ou Y

----

## Exemples

- `ab` représente la chaîne ab
- `[ab]` représente soit le caractère a, soit le caractère b
- `[0-9][a-z]` chaîne constituée d'un chiffre puis d'une lettre
- `[P|0-9][0-9]{8}` chaîne constituée d'un P ou d'un chiffre, suivi de 8 chiffres
- `\b[0-9]{5}\b` pour un code postal, plus exactement tout nombre à 5 chiffres entouré de ruptures de mot
- `(NYSE|AMEX|NASDAQ):[A-Z]{1,4}` pour l'une des 3 valeurs, suivi de deux points, suivis d'un symbole en majuscules. Exemple : NYSE:IBM
- `[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}` pour une adresse IP (1 à 3 chiffres, suivi d'un point, suivi de 1 à 3 chiffres...). Le point s’écrit \. car « . » est un caractère qui signifie « un caractère quelconque ». Le \ précédent le . sert à indiquer qu’il ne faut pas interpréter le point comme une expression régulière
- `[0-9]{1,2} [a-z]+ [0-9]{4}` ou `\d{1,2} \p{Lower}+ \d{4}` pour une date au format 28 mars 2018
