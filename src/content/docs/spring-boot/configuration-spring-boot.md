---
title: "Configuration de Spring Boot"
---

## Lancer l'application en local

Démarrez l'application directement depuis Maven (avec rechargement à chaud si *Spring Boot DevTools* est présent) :

```bash
mvn spring-boot:run
```

Pour surcharger ponctuellement des propriétés au démarrage :

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--formationapirest.environnement=Toto --monNom=Varlet"
```

## Injection de propriétés (`@Value`)

Vous pouvez injecter une valeur issue de `application.properties` (ou d'une variable d'environnement / ligne de commande) dans un composant Spring avec `@Value` :

```properties title="src/main/resources/application.properties"
monNom=Gaetan
```

```java title="TestController.java"
import org.springframework.beans.factory.annotation.Value;

@Value("${monNom:Inconnu}")
private String nom;

@GetMapping("/mon-nom")
public String getNom() {
    return nom;
}
```

:::tip[Valeur par défaut avec `:`]
La syntaxe `${cle:valeurParDefaut}` (ici `:Inconnu`) définit une valeur de repli si la clé est absente — sans elle, Spring refuse de démarrer si la propriété manque.
:::

Démarrez l'application et accédez à [http://localhost:8080/mon-nom](http://localhost:8080/mon-nom).

:::note[Alternative pour plusieurs propriétés : `@ConfigurationProperties`]
Si vous avez plusieurs propriétés liées (ex. `app.api.url`, `app.api.timeout`), il existe une alternative à `@Value` : regrouper ces champs dans une classe (ou un `record`) annotée avec `@ConfigurationProperties(prefix = "app.api")`.
:::

## Activer une classe selon une propriété

Vous pouvez conditionner l'enregistrement d'un bean Spring (`@Service`, `@Configuration`, `@Bean`…) à la valeur d'une propriété :

- **`@ConditionalOnProperty`** : active la classe uniquement si la propriété correspond à `havingValue` (`matchIfMissing = false` par défaut : si la propriété est absente, le bean n'est pas créé).

```java
@Service
@ConditionalOnProperty(name = "environnement", havingValue = "local")
public class MonServiceLocal {
    // Bean actif uniquement quand environnement=local
}
```

- **`@ConditionalOnExpression`** : utile lorsque vous avez besoin d'une condition composée (`&&`, `||`) ou d'une valeur booléenne avec valeur par défaut directement dans l'expression (ici `false` si `ma-property` est absente).

```java
@Service
@ConditionalOnExpression("${ma-property:false}")
public class MonServiceOptionnel {
    // Bean actif uniquement quand ma-property=true
}
```

## Bonus : personnaliser la bannière ASCII

Pour personnaliser la bannière affichée dans la console au démarrage de Spring Boot :

- Créez un fichier **`banner.txt`** dans `src/main/resources/`.
- Vous pouvez générer le texte ASCII sur [Text to ASCII Art Generator (TAAG)](http://patorjk.com/software/taag/#p=display&f=Graceful&t=Youpi).

*(Le packaging en JAR exécutable, la gestion des environnements et profils ainsi que l'inspection des propriétés au démarrage sont détaillés dans [Packaging et configuration par environnement](/formation-api-rest/deploiement/packaging-environnements/).)*
