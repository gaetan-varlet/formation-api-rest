---
title: "Neutralisation des entrées utilisateur : les bons réflexes"
---

## Pourquoi neutraliser les entrées utilisateur ?

Dès qu'une saisie utilisateur est réinjectée dans un contexte interprété (corps d'e-mail HTML, page web, template, rapport généré), le moteur de rendu ne fait aucune différence entre le code légitime de l'application et le texte saisi.

**Les risques concrets :**

- **XSS stocké (*Stored Cross-Site Scripting*) :**
  - Exécution de JavaScript arbitraire dans la session de la victime.
  - Conséquences : vol de jetons d'authentification (cookies, tokens JWT) ou exécution d'actions au nom de l'utilisateur ou de l'administrateur.
- **Hameçonnage interne (*Phishing*) et usurpation :**
  - Injection de faux blocs visuels, d'alertes ou de liens frauduleux au sein d'e-mails ou d'interfaces internes légitimes.
  - Exploitation de la confiance accordée au domaine expéditeur.
- **Défiguration et rupture d'affichage :**
  - Balises non fermées (`<!--`, `<div>`) qui masquent ou cassent la mise en page.
  - Disparition involontaire d'informations critiques situées après l'injection.

## Exemple 1 : l'usurpation par e-mail (cas malveillant)

**Code vulnérable (Java) :**

```java
// Le commentaire utilisateur est concaténé directement dans le corps HTML
String message = "Statut : VALIDÉ<br/>"
    + "Commentaire : " + nouvelEchange.getCommentaire() + "<br/>";
```

**Saisie malveillante envoyée :**

```plaintext
RAS.<br/><br/><b style="color:red">ALERTE SÉCURITÉ :</b> Veuillez vous reconnecter sur <a href="http://serveur-pirate.intra/login">le portail Analyzer</a> pour valider votre session.
```

**Impact visuel dans la boîte mail du destinataire :**

- **Sans échappement (vulnérable) :**

<div class="email-preview">
<div class="email-preview-header"><strong>De :</strong> ne-pas-repondre@monappli.insee.fr &nbsp;·&nbsp; <strong>Objet :</strong> Mise à jour dossier</div>
<div class="email-preview-body">
Statut : VALIDÉ<br/>
Commentaire : RAS.<br/><br/>
<strong style="color: #dc2626;">ALERTE SÉCURITÉ :</strong> Veuillez vous reconnecter sur <span style="color: #2563eb; text-decoration: underline;">le portail Analyzer</span> pour valider votre session.
</div>
</div>

Le mail provient du serveur officiel de l'application : l'attaque de phishing interne paraît 100 % légitime aux yeux du destinataire.

- **Avec échappement (`HtmlUtils.htmlEscape`) :**

<div class="email-preview">
<div class="email-preview-header"><strong>De :</strong> ne-pas-repondre@monappli.insee.fr &nbsp;·&nbsp; <strong>Objet :</strong> Mise à jour dossier</div>
<div class="email-preview-body">
Statut : VALIDÉ<br/>
Commentaire : RAS.&lt;br/&gt;&lt;br/&gt;&lt;b style="color:red"&gt;ALERTE SÉCURITÉ :&lt;/b&gt; Veuillez vous reconnecter sur &lt;a href="http://serveur-pirate.intra/login"&gt;le portail Analyzer&lt;/a&gt; pour valider votre session.
</div>
</div>

Aucun style appliqué, aucun lien cliquable : les balises sont affichées littéralement et la tentative est neutralisée.

## Exemple 2 : le faux positif qui casse le métier (cas accidentel)

La neutralisation n'est pas qu'un sujet de sécurité offensive : l'absence d'échappement crée aussi des bugs fonctionnels sur des saisies tout à fait légitimes.

**Saisie légitime de l'utilisateur :**

```plaintext
Problème résolu dans la balise <dependency>, version < 2.4 obligatoire.
```

**Rendu côté destinataire :**

- **Sans échappement :** `Problème résolu dans la balise , version`
  Le moteur HTML tente d'interpréter `<dependency>` et `< 2.4 obligatoire.` comme des balises et masque le contenu. Le message devient incompréhensible.
- **Avec `HtmlUtils.htmlEscape` :** `Problème résolu dans la balise <dependency>, version < 2.4 obligatoire.`
  Les caractères `<` et `>` sont encodés en `&lt;` et `&gt;` : le texte s'affiche exactement comme l'utilisateur l'a saisi.

## Deux stratégies : échapper ou assainir

### 1. L'échappement : `HtmlUtils.htmlEscape` (Spring Web)

- **Approche :** neutralisation totale (considérer l'entrée comme du texte pur).
- **Fonctionnement :** remplace systématiquement les caractères structurels du HTML par leurs entités textuelles (`<` → `&lt;`, `>` → `&gt;`, `&` → `&amp;`, `"` → `&quot;`).
- **Résultat :** aucune balise interprétée ; le client affiche le texte brut (ex. `<b>test</b>` s'affiche textuellement `<b>test</b>`).
- **Cas d'usage :** champs de texte simples sans mise en page (commentaires bruts, noms, identifiants, libellés).

```java
import org.springframework.web.util.HtmlUtils;

String commentaireSecurise = HtmlUtils.htmlEscape(nouvelEchange.getCommentaire());
String message = "Statut : VALIDÉ<br/>Commentaire : " + commentaireSecurise + "<br/>";
```

### 2. L'assainissement : *Sanitizer* (OWASP Java HTML Sanitizer, Jsoup)

- **Approche :** filtrage sélectif (autoriser du texte enrichi sécurisé).
- **Fonctionnement :** parse la saisie sous forme d'arbre DOM et applique une liste blanche (*allowlist*) stricte de balises et d'attributs autorisés.
- **Résultat :**
  - conserve le HTML légitime (`<b>`, `<i>`, `<ul>`, `<li>`, `<p>`) ;
  - supprime les balises dangereuses (`<script>`, `<iframe>`, `<object>`) ;
  - nettoie les attributs à risque (gestionnaires d'événements `onload`, `onclick`, liens `href="javascript:..."`).
- **Cas d'usage :** zones avec éditeur de texte riche (WYSIWYG, description Jira, CMS, Markdown converti en HTML).

## Synthèse : comment choisir ?

| Critère | `HtmlUtils.htmlEscape` | Sanitizer (ex. OWASP, Jsoup) |
| :--- | :--- | :--- |
| **Objectif** | Rendre une saisie 100 % inerte (texte pur) | Permettre du HTML propre et sécurisé |
| **Rendu de** `<b>gras</b>` | Affiche le texte brut `<b>gras</b>` | Affiche **gras** (balise interprétée) |
| **Rendu de** `<script>alert(1)</script>` | Affiche le texte brut `<script>alert(1)</script>` | Supprime la balise et le script dangereux |
| **Complexité / Performance** | Très rapide (simple substitution de caractères) | Plus lourd (parsing complet du DOM) |
| **Dépendance** | Déjà inclus dans Spring Web (`spring-web`) | Nécessite une bibliothèque tierce |

:::note[Et dans une API REST JSON ?]
N'appliquez **pas** `HtmlUtils.htmlEscape` systématiquement sur les champs stockés en base ou renvoyés en JSON : vous corrompriez les données pour les autres consommateurs (application mobile, export CSV, front-end React/Angular qui échappent déjà l'affichage par défaut).

- **À l'entrée de l'API :** validez le format et la taille des données avec Bean Validation (voir [Contrats d'API](/formation-api-rest/api-rest/contrats-api/)).
- **À la sortie / au rendu :** échappez ou assainissez au moment précis où la donnée est injectée dans un contexte interprété (e-mail HTML, template serveur, export PDF/HTML).
:::
