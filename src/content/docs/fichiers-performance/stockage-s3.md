---
title: "Stockage objet (S3)"
---

## Principe : base de données + stockage objet S3

Lorsque les fichiers reçus ou servis par l'API sont volumineux ou nombreux, les stocker directement en base de données alourdit les sauvegardes et dégrade les performances. On privilégie alors un **stockage objet compatible S3** (*Simple Storage Service*).

Contrairement à une idée reçue, S3 n'est pas propre au cloud Amazon : c'est un **protocole standard** utilisé par la grande majorité des solutions de stockage objet (MinIO, Ceph, OVH, Scaleway, AWS S3…).

Surtout, un serveur S3 **n'est pas un système de fichiers classique** : on n'y accède pas avec `java.io.File`, `Files` ou `FileReader`. Votre application dialogue avec lui **à travers une API HTTP(S)** via un client dédié (le SDK S3), ce qui permet aussi à plusieurs instances de l'API (par exemple plusieurs pods Kubernetes) de partager facilement le même stockage sans disque réseau partagé.

La séparation des responsabilités est simple :

- **Sur le serveur S3 :** le contenu binaire du fichier, rangé dans un **bucket** (équivalent d'un conteneur logique) sous une **clé unique** (idéalement générée avec un `UUID` côté serveur pour éviter les collisions et les noms forgés).
- **En base de données (ex. PostgreSQL) :** uniquement les **métadonnées métier** (nom d'origine, type MIME, taille, date, entité liée et clé S3).

Concrètement, les échanges suivent deux flux symétriques :

- **À l'upload (ou après génération d'un PDF ou d'un rapport côté serveur) :** l'API dépose le fichier binaire sur S3 sous une clé unique (`UUID`), puis enregistre ses métadonnées et cette clé en base de données.
- **Au téléchargement :** l'API retrouve la clé et les métadonnées (nom d'origine, `Content-Type`) en base, récupère le flux binaire depuis S3 et le renvoie au client HTTP.

## Quelle bibliothèque Java choisir ?

### Recommandation par défaut : AWS SDK for Java v2 (`software.amazon.awssdk:s3`)

Utilisez le client officiel **AWS SDK v2** (licence **Apache 2.0**) comme standard sur les nouveaux projets :

- **Portabilité totale :** le code applicatif s'appuie uniquement sur le standard S3 et reste identique que le serveur soit un **MinIO** interne, un **Ceph**, un cloud souverain ou AWS.
- **Écosystème et maintenance :** large communauté JVM, BOM unifié et correctifs rapides.
- **Sécurité (*Supply Chain*) :** dépendances transitives suivies de près, ce qui limite les alertes bloquantes lors des scans de vulnérabilités (CVE).

### Option Spring-idiomatique : Spring Cloud AWS (`spring-cloud-aws-starter-s3`)

Si vous souhaitez une intégration plus proche des conventions Spring Boot (auto-configuration via `spring.cloud.aws.*` et façade `S3Template` similaire à `JdbcTemplate`), **Spring Cloud AWS** (licence **Apache 2.0**) encapsule directement l'AWS SDK v2. Gardez simplement en tête qu'il s'agit d'une dépendance communautaire externe au *core* Spring Boot, à aligner lors des montées de version majeures.

### À éviter sur les nouveaux développements

- **SDK MinIO (`io.minio:minio`) :** crée un couplage fort à des classes propriétaires (`MinioClient`, `PutObjectArgs`) alors que le SDK AWS v2 dialogue nativement avec MinIO, et embarque des dépendances transitives (`OkHttp`, `Kotlin stdlib`) qui génèrent fréquemment des alertes CVE dans les analyses de sécurité.
- **AWS SDK v1 (`com.amazonaws:aws-java-sdk-s3`) :** ancienne génération officiellement en fin de vie (*End of Life*).

## Points d'attention

- **Travailler en flux plutôt qu'en `byte[]` :** privilégiez l'envoi et la lecture par flux (`InputStream` côté SDK, `InputStreamResource` côté Spring MVC) pour éviter de charger de gros fichiers en RAM.
- **Pas de transaction distribuée entre PostgreSQL et S3 :** si l'insertion en base échoue après l'envoi vers S3 (ou inversement), un fichier orphelin ou une métadonnée sans fichier peut exister. C'est d'ailleurs pour éviter cette complexité qu'il est souvent préférable de garder en base les petits binaires lorsqu'ils restent peu volumineux (voir [Réception de fichiers](/formation-api-rest/fichiers-performance/upload/#types-de-fichiers-et-traitements-courants)).
