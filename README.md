# Formation API REST

Support de formation sur le développement d'API REST avec Spring Boot, généré avec [Astro Starlight](https://starlight.astro.build/).

:arrow_forward: [Documentation en ligne](https://gaetan-varlet.github.io/formation-api-rest/)

---

## 🧭 Organisation et parcours de formation

Ce support rassemble un programme complet (**4 à 5 jours** en format animé avec travaux pratiques) : l'ensemble du contenu est bien trop dense pour tenir en une seule journée.

- 🎓 **En autoformation :** chaque module peut être suivi librement et à votre rythme directement depuis la documentation en ligne.
- 👩‍🏫 **En formation animée :** pour rester digeste, le programme peut être découpé en **3 formations distinctes** :

| Formation | Durée | Contenu couvert |
| --- | :---: | --- |
| 🚀 **1. Socle API REST & Spring Boot** | **2 jours** | **Fondamentaux** *(aperçu de l'exemple Java EE sans entrer dans le détail)*, **Démarrer avec Spring Boot**, **Développement d'API REST** *(consommation d'API présentée sans temps de pratique dédié)*, **Fichiers simples** *(réception simple et export en mémoire)*, **Sécurité & filtres**, et **Production & déploiement** *(passage en revue rapide)* |
| 🗄️ **2. Données, cache & streaming** | **1 jour** | **Accès aux données & SQL** *(Spring Data JPA avancé, JDBC/batching, gros volumes)*, **Fichiers en flux, streaming bout-en-bout et cache applicatif** |
| 🧪 **3. Tests d'API Spring Boot** | **1 jour** | **Stratégie de tests**, configuration Maven/qualité, **tests unitaires et d'intégration**, **BDD avec Cucumber**, **simulation d'API externes (WireMock)** et **gestion du temps (`Clock`)** |

> 🧰 **Prérequis pour suivre la formation :** consultez la section dédiée sur la [page d'accueil de la documentation](https://gaetan-varlet.github.io/formation-api-rest/#prérequis-pour-la-formation).

---

## 💻 Démarrer en local (première fois)

### 📋 1. Prérequis

- **Node.js** (version LTS récente)
- **pnpm** (gestionnaire de paquets utilisé par le projet via `pnpm-lock.yaml`)
  - Si `pnpm` n'est pas encore installé : `npm install -g pnpm` (ou `corepack enable`)

### ⚡ 2. Installer et lancer le serveur de développement

```bash
# Cloner le dépôt (si ce n'est pas déjà fait)
git clone https://github.com/gaetan-varlet/formation-api-rest.git
cd formation-api-rest

# Installer les dépendances
pnpm install

# Lancer le serveur local avec rechargement à chaud
pnpm dev
```

Le site est ensuite accessible dans le navigateur à l'adresse affichée dans le terminal, par exemple :
`http://localhost:4321/formation-api-rest/`

> ⚠️ **Important :** le site est configuré avec `base: '/formation-api-rest'` dans `astro.config.mjs` (pour GitHub Pages). En local, il faut donc bien conserver `/formation-api-rest/` dans l'URL.

---

## 🛠️ Commandes utiles

| Commande | Rôle |
| --- | --- |
| `pnpm dev` | Démarre le serveur de développement local (rechargement à chaud). |
| `pnpm build` | Génère le site statique dans `dist/`, optimise les images et construit l'index de recherche Pagefind. |
| `pnpm preview` | Sert localement le dossier `dist/` généré par `pnpm build` (utile pour tester la recherche et le rendu final avant de pousser). |

---

## ✍️ Mettre à jour le contenu et le site

### 📄 Modifier ou ajouter une page

- Les pages du cours sont dans `src/content/docs/` (fichiers `.md` ou `.mdx`).
- Chaque page doit avoir au minimum un `title` dans son frontmatter YAML (`---`).
- **Niveaux de titres :** Starlight génère automatiquement le `<h1>` à partir du `title`. Dans le corps de la page, commencez toujours par `##` (`H2`) puis `###` (`H3`).
- **Ordre des pages :** l'ordre affiché dans le menu latéral et les boutons *Précédent / Suivant* est défini explicitement dans le tableau `sidebar` de `astro.config.mjs` (et non par l'ordre alphabétique des fichiers). Si vous créez une nouvelle page, ajoutez son `slug` dans `astro.config.mjs`.

### 🖼️ Ajouter une image

- Placez les images dans `src/assets/images/` et référencez-les en chemin relatif depuis le Markdown (ex. `../../../assets/images/mon-schema.png`).
- Astro + `sharp` les convertissent et les optimisent automatiquement en WebP lors du `pnpm build`.
- Les styles globaux se trouvent dans `src/styles/custom.css`.

### 📦 Mettre à jour les dépendances (Astro / Starlight)

```bash
# Vérifier les mises à jour disponibles
pnpm outdated

# Mettre à jour selon les plages de package.json (ou cibler un paquet précis)
pnpm update

# Vérifier que le build complet passe toujours
pnpm build
```

Pensez à commiter `pnpm-lock.yaml` (et `package.json` si modifié) après une mise à jour.

---

## 🧩 Configuration Astro et code spécifique

Le site s'appuie au maximum sur les comportements natifs d'**Astro** et **Starlight**. Les quelques fichiers hors contenu Markdown ont chacun un rôle précis :

| Fichier | Rôle |
| --- | --- |
| `astro.config.mjs` | Configuration principale Astro / Starlight (`base`, `site`, langue française, barre latérale, liens GitHub/RSS) et plugin `rehypeExternalLinks` ouvrant les liens externes dans un nouvel onglet de façon accessible. |
| `src/content.config.ts` | Déclaration standard de la collection `docs` (`docsLoader` + `docsSchema` Starlight). |
| `src/pages/rss.xml.ts` | Génération du flux RSS (`/formation-api-rest/rss.xml`) trié selon `lastUpdated` ou l'historique Git (Starlight ne fournit pas de flux RSS natif). |
| `src/utils/tech-icons.ts` | Résolveur local des icônes `@iconify-json/devicon` et `@iconify-json/lucide` au moment du build (ces paquets ne contenant que des JSON bruts de SVG). |
| `src/components/TechIcon.astro` | Composant inline minimal rendant un SVG résolu par `tech-icons.ts` dans les textes, listes et tableaux MDX. |
| `src/components/Card.astro` | Wrapper minimal autour du `<Card>` natif Starlight permettant d'utiliser les icônes Devicon/Lucide tout en conservant le style natif. |
| `src/styles/custom.css` | Ajustements CSS minimaux par-dessus le thème Starlight (centrage des schémas, grille 3 colonnes `.card-grid-3`, comparatifs, aperçu e-mail, blocs `<details>` et icônes SVG). |

---

## 🌐 Déploiement

Le déploiement sur GitHub Pages est **automatique** via le workflow `.github/workflows/deploy.yml` :

- À chaque `git push` sur la branche `main`, GitHub Actions exécute le build Astro et déploie le site.
- Il est aussi possible de le déclencher manuellement depuis l'onglet **Actions** du dépôt GitHub (*Deploy to GitHub Pages* → *Run workflow*).

---

## 💡 Points d'attention

- ✅ **Tester le build avant de pousser :** lancez `pnpm build` en local avant de pousser sur `main` pour détecter immédiatement une image manquante, une erreur de syntaxe MDX ou un `slug` invalide dans `astro.config.mjs`.
- 🔍 **Recherche locale (Pagefind) :** l'index de recherche n'est généré que lors de `pnpm build`. Pour tester la barre de recherche en local, utilisez `pnpm build && pnpm preview`.
- 🔗 **Liens internes :** si vous écrivez un lien absolu vers une autre page du cours, préfixez-le par `/formation-api-rest/...` (en cohérence avec `base` dans `astro.config.mjs`).
- 🌗 **Thèmes clair et sombre :** Starlight propose un sélecteur de thème clair/sombre — vérifiez le contraste des schémas/images dans les deux modes (ou utilisez des images `light` / `dark` comme sur `src/content/docs/index.mdx`).
