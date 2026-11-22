---
title: "Tâches planifiées (Scheduling)"
---

Spring permet d'exécuter automatiquement des méthodes en tâche de fond selon un intervalle régulier ou un calendrier (**`@Scheduled`**).

## Modes de planification

Trois attributs principaux permettent de définir la fréquence d'exécution :

- **`fixedRate`** : exécute la méthode à intervalle fixe (en millisecondes) mesuré depuis le **début** de l'exécution précédente (`@Scheduled(fixedRate = 5000)` → toutes les 5 secondes).
- **`fixedDelay`** : attend un délai fixe (en millisecondes) après la **fin** de l'exécution précédente avant de relancer la tâche (évite que deux exécutions ne se chevauchent si la tâche est lente).
- **`cron`** : planifie selon une expression Cron à **6 champs** en Spring (`seconde minute heure jour mois jour-de-semaine`), par exemple `@Scheduled(cron = "0 0 2 * * ?")` pour tous les jours à 2 h 00.

## Mise en place

1. **Activer le scheduling** sur la classe principale avec `@EnableScheduling` :

```java title="FormationApiRestApplication.java"
@SpringBootApplication
@EnableScheduling
public class FormationApiRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(FormationApiRestApplication.class, args);
    }
}
```

2. **Déclarer la tâche planifiée** dans un bean Spring (`@Component` ou `@Service`) :

```java title="BasicScheduledTask.java"
@Slf4j
@Component
public class BasicScheduledTask {

    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayTask() {
        log.info("Exécution planifiée à {}", LocalDateTime.now());
    }
}
```

:::note[Méthode sans paramètre et type de retour]
Une méthode annotée `@Scheduled` ne doit prendre **aucun argument** et retourne généralement **`void`** (toute valeur de retour est ignorée).
:::

## Externaliser la planification dans `application.properties`

Plutôt que d'écrire les délais ou expressions Cron en dur dans le code Java, utilisez des propriétés pour pouvoir ajuster la fréquence selon l'environnement sans recompiler :

```properties title="src/main/resources/application.properties"
app.task.fixed-delay-ms=5000
app.task.cron=0 0 2 * * ?
```

```java title="BasicScheduledTask.java"
@Scheduled(fixedDelayString = "${app.task.fixed-delay-ms}")
@Scheduled(fixedRateString = "${app.task.fixed-rate-ms}")
@Scheduled(cron = "${app.task.cron}")
```

*(Note : pour `fixedDelay` et `fixedRate`, il faut utiliser les variantes `*String` afin d'accepter un placeholder `${...}`.)*

:::caution[Attention en production multi-instances (Kubernetes)]
Par défaut, `@Scheduled` s'exécute en mémoire **dans chaque instance** de l'application. Si votre API tourne sur plusieurs réplicas (par exemple 2 ou 3 pods sous Kubernetes), la tâche sera déclenchée **plusieurs fois en parallèle**.

- **Privilégiez un `CronJob` Kubernetes :** en cluster, il est souvent préférable de déléguer la planification à un `CronJob` dédié afin qu'une seule exécution soit lancée à la fois.
- **Visez l'idempotence :** un traitement planifié doit idéalement être **idempotent**, c'est-à-dire que l'exécuter plusieurs fois de suite (ou en parallèle) produit exactement le même état final qu'une exécution unique, sans créer de doublons ni effets de bord indésirables.
:::

## Pour aller plus loin

- [Guide Spring `@Scheduled` — Baeldung](https://www.baeldung.com/spring-scheduled-tasks)
