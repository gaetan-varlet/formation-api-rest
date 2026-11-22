---
title: "Intégration Cucumber dans GitLab CI"
---

:::caution[Page à revoir]
Cette page est en cours de révision : les exemples `.gitlab-ci.yml` ci-dessous doivent être mis à jour (réduction des logs Maven/Spring Boot, transmission des artefacts entre jobs, noms des rapports Cucumber et règles GitLab Pages).
:::

## Réduire les logs dans le pipeline

```bash
variables:
  MAVEN_TEST_OPTS: >-
    -Dspring.datasource.driver-class-name=org.h2.Driver
    -Dspring.datasource.url=jdbc:h2:mem:testdb
    -Dlog.level=INFO

job_tests_build:
  script:
    - mvn clean verify $MAVEN_TEST_OPTS
```

## Avoir le rapport sur Gtilab Pages

```bash
pages:
  stage: deploy-reports
  needs:
    - job_tests_build
  script:
    - mkdir public
    - mv ./target/rapport-cucumber.html public/rapport-cucumber.html
  artifacts:
    paths:
      - public/
  allow_failure: true
  only:
    - main
```
