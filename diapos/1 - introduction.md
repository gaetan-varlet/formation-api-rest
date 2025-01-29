# Introduction

----

## Qu'est ce qu'un service web ?

Un service web est une technologie permettant à des applications de dialoguer à distance via Internet. On parle d'interface de programmation applicative, souvent désignée par le terme **API** pour *application programming interface*. L'application **offre des services** à d'autres logiciels.

Une **API REST**, pour *REpresentational State Transfer*,  est une API basée sur le protocole **HTTP**, où chaque ressource est accessible via un identifiant unique (URI).

Exemple : [JSONPlaceholder](https://jsonplaceholder.typicode.com/)

- obtenir la liste des utilisateurs : [https://jsonplaceholder.typicode.com/users](https://jsonplaceholder.typicode.com/users)

----

## Les formats de données XML et JSON

- les formats JSON et XML  permettent de stocker des données textuelles de manière structurée.
- le JSON est plus léger

----

### Objet Java

```java
@XmlRootElement
public class Personne {
  private String prenom;
  private Integer age;
  private Adresse adresse;
}

@XmlRootElement
public class Adresse {
  private String nomCommune;
  private String codePostal;
}
```

----

#### Rendu en JSON

```json
{
  "prenom": "Bob",
  "age": 30,
  "adresse": {
    "nomCommune": "Montrouge",
    "codePostal": "92120"
  }
}
```

```json
[
  {
    "prenom": "Bob",
    "age": 30,
    "adresse": {
      "nomCommune": "Montrouge",
      "codePostal": "92120"
    }
  },
  {
    "prenom": "Albert",
    "age": 23,
    "adresse": {
      "nomCommune": "Saint-Quentin",
      "codePostal": "02100"
    }
  }
]
```

----

### Rendu en XML

```xml
<personne>
    <adresse>
        <codePostal>92120</codePostal>
        <nomCommune>Montrouge</nomCommune>
    </adresse>
    <age>30</age>
    <prenom>Bob</prenom>
</personne>
```

```xml
<personnes>
    <personne>
        <adresse>
            <codePostal>92120</codePostal>
            <nomCommune>Montrouge</nomCommune>
        </adresse>
        <age>30</age>
        <prenom>Bob</prenom>
    </personne>
    <personne>
        <adresse>
            <codePostal>02100</codePostal>
            <nomCommune>Saint-Quentin</nomCommune>
        </adresse>
        <age>23</age>
        <prenom>Albert</prenom>
    </personne>
</personnes>
```
