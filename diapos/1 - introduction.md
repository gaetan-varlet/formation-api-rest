# Introduction

----

## Qu'est ce qu'un service web ?

Un service web est une technologie permettant à des applications de dialoguer à distance via Internet. On parle d'interface de programmation applicative, souvent désignée par le terme **API** pour *application programming interface*. L'application **offre des services** à d'autres logiciels.

Une **API REST**, pour *REpresentational State Transfer*,  est une API basée sur le protocole **HTTP**, où chaque ressource est accessible via un identifiant unique (URI).

Exemples :
- [JSONPlaceholder](https://jsonplaceholder.typicode.com/)
     - obtenir la liste des utilisateurs : [https://jsonplaceholder.typicode.com/users](https://jsonplaceholder.typicode.com/users)
- [World Population API](http://api.population.io/)
    - obtenir la liste des pays : [http://api.population.io:80/1.0/countries](http://api.population.io:80/1.0/countries)

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

```line-numbers language-json
{
  "prenom": "Gaëtan",
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
    "prenom": "Gaëtan",
    "age": 30,
    "adresse": {
      "nomCommune": "Montrouge",
      "codePostal": "92120"
    }
  },
  {
    "prenom": "Thibaut",
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
    <prenom>Gaëtan</prenom>
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
        <prenom>Gaëtan</prenom>
    </personne>
    <personne>
        <adresse>
            <codePostal>02100</codePostal>
            <nomCommune>Saint-Quentin</nomCommune>
        </adresse>
        <age>23</age>
        <prenom>Thibaut</prenom>
    </personne>
</personnes>
```