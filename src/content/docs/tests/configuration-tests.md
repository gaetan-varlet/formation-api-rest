---
title: "Configuration pour les tests"
---

## Les bibliothèques de test

Ajoutez la dépendance de test Spring Boot (en portée `test`, pour ne pas l'embarquer dans le livrable final) — **elle est déjà fournie par défaut par Spring Initializr** lors de la création d'un projet Spring Web :

```xml title="pom.xml"
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
```

Ce starter embarque automatiquement l'écosystème standard de test Java / Spring :
- **JUnit 5** (moteur d'exécution et assertions de base) ;
- **Mockito** (création de doublures / *mocks*) ;
- **AssertJ** (assertions fluides `assertThat(...)`) ;
- **Hamcrest / JsonPath** (assertions sur les structures JSON) ;
- **Spring Test** (`@SpringBootTest`, `MockMvc`, `RestTestClient`…).

## Exécution avec Maven : Surefire vs Failsafe

Maven distingue l'exécution des tests unitaires rapides de celle des tests d'intégration plus longs grâce à deux plugins :

| Plugin | Convention de nommage | Phase Maven | Usage typique |
| --- | --- | --- | --- |
| **Maven Surefire** | `*Test.java` | `mvn test` | Tests unitaires rapides (sans contexte Spring) |
| **Maven Failsafe** | `*IT.java` | `mvn verify` | Tests d'intégration / Cucumber |

**Maven Surefire** est déjà actif par défaut avec `spring-boot-starter-parent`. Pour activer **Maven Failsafe** et lancer les tests d'intégration lors de `mvn verify`, ajoutez simplement le plugin dans `pom.xml` :

```xml title="pom.xml"
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

## Couverture de code avec JaCoCo

**JaCoCo** (*Java Code Coverage*) mesure les zones du code source exécutées pendant les tests :

- Génère un rapport HTML consultable localement dans `target/site/jacoco/index.html`.
- Génère un rapport exploitable par **SonarQube** (`target/jacoco.exec` / XML).
- Peut faire échouer le build si un seuil minimal de couverture n'est pas atteint (via le goal `check`).

### Métriques principales

- **Lines coverage :** pourcentage de lignes de code dans lesquelles les tests sont passés.
- **Branches coverage :** pourcentage de branches conditionnelles (liées aux `if`/`else`, `switch`…) effectivement traversées.
- **Cyclomatic complexity :** complexité cyclomatique — nombre minimal de chemins indépendants pour parcourir tout un bloc de code (par exemple `1` sans `if`), donnant un indicateur du nombre de cas de test à mettre en œuvre.

:::caution[Limite de la couverture de code]
Un taux de couverture élevé (même 100 %) prouve uniquement que les lignes ont été **traversées**, pas que les résultats ont été correctement vérifiés par des assertions pertinentes.
:::

### Configuration Maven

```xml title="pom.xml"
<!-- Génère les rapports de couverture de tests JaCoCo -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.14</version>
    <executions>
        <execution>
            <id>default-prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>default-report</id>
            <phase>verify</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Tests de mutation avec PiTest

> *« Si vous voulez savoir si vos tests sont bons, introduisez un bug et regardez si vos tests échouent. »*

Pour dépasser les limites d'un simple pourcentage de couverture, le **mutation testing** vérifie si vos assertions détectent réellement les régressions :

1. **PiTest** injecte automatiquement de petits défauts (*mutations*) dans le code compilé (inverser un `>`, supprimer un appel de méthode, changer une constante…).
2. La suite de tests est relancée sur chaque mutant :
   - Si au moins un test échoue → la mutation est **tuée** (bon signe).
   - Si tous les tests continuent de passer → la mutation **survit** (assertion manquante ou code mort).
3. Le rapport (`target/pit-reports/index.html`) donne le pourcentage de mutations tuées.

### Configuration Maven et exécution

```xml title="pom.xml"
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.12.0</version>
    <!-- Plugin requis pour le support de JUnit 5 -->
    <dependencies>
        <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.1.2</version>
        </dependency>
    </dependencies>
</plugin>
```

Lancement manuel à la demande (recommandé, car l'analyse de mutation peut être longue) :

```bash
mvn clean test org.pitest:pitest-maven:mutationCoverage
```

*(Si vous souhaitez l'exécuter automatiquement dans votre cycle Maven, par exemple en CI lors de `verify`, ajoutez un bloc `<executions>` dans la déclaration du plugin :)*

```xml title="pom.xml (optionnel : exécution automatique)"
<executions>
    <execution>
        <id>pit-report</id>
        <phase>verify</phase>
        <goals>
            <goal>mutationCoverage</goal>
        </goals>
    </execution>
</executions>
```
