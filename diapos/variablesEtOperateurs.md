# Les variables et les opérateurs

----

## Les variables
* numériques :
	* byte : 1 octet, de -128 à 127
	* short : 2 octets, de -32768 à 32767
	* int : 4 octets, de -2x10^9 à 2x10^9
	* long : 8 octets, de -9x10^18 à 9x10^18, il faut mettre un “L” à la fin du nombre
	* float : 4 octets, pour les nombres à virgule, mettre “f” à la fin
	* double : 8 octets, même chose avec un “d” à la fin
* char : permet de stocker un caractère entre guillemets simples
* boolean : permet de stocker true ou false
* String : permet de stocker des chaînes de caractères entre guillemets doubles
```java
byte temperature = 64;
float pi = 3.14159f;
char caractere='A';
boolean question = true;
String phrase = "Salut !";
String phrase = new String("Salut !");
```

----

## Les règles de nommage

- les noms de classe doivent commencer par une majuscule
- les noms de variables doivent commencer par une minuscule
- si le nom d’une variable est composé de plusieurs mots, faire commencer le premier par une minuscule et les suivants par des majuscules, le tout sans espaces
- pas d’accents

----

## Les opérateurs arithmétiques

- \+ pour l’addition, - pour la soustraction, * pour la multiplication, / pour la division, % pour le modulo (renvoie le reste de la division entière de 2 nombres)
- incrémentation :
```java
nbre1 = nbre1 + 1;
nbre1 += 1;
nbre1++;
++nbr1;
```
- décrémentation : même chose que l'incrémentation, avec des - à la place des +
- multiplication :
```java
nbre1 = nbre1 * 2;
nbre1 *= 2;
```
- division : même chose que la multiplication avec des / à la place des *

----

## Les conversions

Exemple :
```java
int i = 100;
float j = (float)i;
System.out.println(j); // affiche 100.0
```
Attention aux priorités lors des conversions, sinon le résultat peut être faux. Dans le deuxième exemple, la JVM fait le calcul, puis la conversion, puis l'affectation à la variable.
```java
int nbre1 = 3, nbre2 = 2;
double resultat = nbre1 / nbre2;
System.out.println(resultat); // affiche 1.0

nbre1 = 3; nbre2 = 2;
resultat = (double)(nbre1 / nbre2);
System.out.println(resultat); // affiche 1.0

nbre1 = 3; nbre2 = 2;
resultat = (double)(nbre1) / (double)(nbre2);
System.out.println(resultat);  // affiche 1.5
```

----

## Conversion d'un String en nombre

### Conversion d'un String en int et Integer

```java
String str = "123";
int x = Integer.parseInt(str);
Integer y = Integer.valueOf(str);
```
une NumberFormatException est levée si :
* si la chaîne de caractère est `null`
* si elle ne contient pas de chiffres
* si elle contient autre chose qu'un chiffre (lettre, espace...)

----

### Exemple de méthode pour convertir un String en Integer
 * accepte `null` en argument

```java
public static Integer convertStringToInteger(String nbAConvertir){
	Integer integer = null;
	if(nbAConvertir!=null){
		integer = Integer.valueOf(nbAConvertir);			
	}
	return integer;
}
```

----

### Exemple de méthode pour convertir un String en Double
 * accepte `null` en argument
 * convertit les virgules dans les nombres en points

```java
public static Double convertStringToDouble(String nbAConvertir){
	Double nombre = null;
	if(nbAConvertir!=null){
		nombre = Double.valueOf(nbAConvertir.replace(",", "."));
	}
	return nombre;
}
```

----

## Conversion d'un nombre en String

### Conversion d'un int et d'un Integer en String

Exemple de conversion de String en int et Integer :
```java
int x = 12;
String str = String.valueOf(x);
Integer y = 15;
String str2 = String.valueOf(y);
```
Autre possibilité :
```java
String str3 = Integer.toString(145);
String str4 = y.toString;
```
Ces deux dernières possibilités n’acceptent pas un Integer null.
