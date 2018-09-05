# Les streams et les lambdas

----

### Concepts
- on l'appelle généralement sur une collection
- 2 types d'opérations
  - opérations intermédiaires : succession de stream (stream pipelines)
    - `Stream.filter` permet de filtrer les éléments d'une collection
    - `Stream.map` permet de choisir quel élément on veut récupérer dans notre stream. On peut aussi directement modifier ce qu'on va récupérer
  - opérations terminales
    - `Stream.reduce`
      - réductions simples : `Stream.sum`, `Stream.max`, `Stream.count`
      - réductions mutables
    - `Stream.collect` permet de récupérer notre résultat dans une collection

----

### Filtrer, mapper, trier et afficher
```java
List<String> strings;
strings.stream()
      // filtrage
      .filter(x -> x.contains("cha"))
      // mapping : reformatage des chaînes de caractères
      .map(x -> x.substring(0, 1).toUpperCase() + x.substring(1))
      // tri par ordre alphabétique
      .sorted()
      // Outputs:
      .forEach( System.out::println );
```

### Mapper, supprimer les doublons, puis collecter dans une liste
```java
List<Commande> mesCommandes = … ;

List<Client> mesClients = mesCommandes.stream()
     .map( c -> c.getClient() )
     .distinct()
     .collect( Collectors.toList() );
```

----

### Trier sur un ordre non naturel
```java
List listeCommandeMai= listeCommandes.stream()
.filter(x -> x.numero.startsWith("201405"))
.sorted((x1, x2) -> (int)(x1.montant - x2.montant))
.collect(Collectors.toList());
```

### Limiter le nombre de résultat
```java
List lListeCommandeMai = listeCommandes.stream()
 .filter(x -> x.numero.startsWith("201405"))
 .sorted((x1, x2) -> (int)(x1.montant - x2.montant))
 .limit(2)
 .collect(Collectors.toList());
 ```

----

### Récupérer le max ou le min (utilisation de GET)
```java
Commande commande = listeCommandes.stream()
 .filter(x -> x.numero.startsWith("201405"))
 .max((x1, x2) -> (int) (x1.montant - x2.montant)).get();

Integer maxNumber =  Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
 .max(Comparator.comparing(Integer::valueOf)).get();

String maxChar = Stream.of("H", "T", "D", "I", "J")
 .max(Comparator.comparing(String::valueOf)).get();

// Création d'un comparateur pour avoir le plus jeune employé
Comparator<Employee> comparator = Comparator.comparing(Employee::getAge);
Employee minObject = listeEmployee.stream().min(comparator).get();
```

----

### Calculer des statistiques sur une list de nombres
```java
List<Integer> liste = Arrays.asList(1,2,3);
IntSummaryStatistics stats = liste.stream().mapToInt(i->i).summaryStatistics();
long nbElements = stats.getCount();
double moyenne = stats.getAverage();
int min = stats.getMin();
int max = stats.getMax();
long somme = stats.getSum();
```

### Compter le nombre d'éléments filtrés
```java
long nombreElement = listeCommandes.stream()
 .filter(x -> x.numero.startsWith("201405"))
 .count();
 ```
