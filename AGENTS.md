# Guide de contribution pour les agents (`AGENTS.md`)

Ce fichier définit les conventions d'architecture, d'écriture et de style du support **Formation API REST** afin de garantir l'homogénéité des contributions sur l'ensemble du dépôt.

---

## 1. Principe directeur : privilégier les comportements par défaut d'Astro / Starlight

Ce dépôt est un **support de formation statique**, pas un portail sur mesure comme `../portail-animation-dev` :

- **Utiliser en priorité les composants et mécanismes natifs de Starlight** : `<CardGrid>`, `<Card>`, `<LinkCard>`, `<Tabs>` / `<TabItem>`, `<Steps>`, `<Badge>`, encadrés Markdown `:::note` / `:::tip` / `:::caution` / `:::danger`, `sidebar` déclarative dans `astro.config.mjs` et recherche Pagefind par défaut.
- **Éviter le code personnalisé superflu** : ne pas réécrire de composants de grilles, de cartes, de barre de titre ou de surcharges de mise en page Starlight (`components: { ... }`), et ne pas accumuler de CSS sur mesure quand le style natif Starlight convient.
- **Rester minimal lorsqu'un besoin spécifique existe** : n'ajouter du code dans `src/components/`, `src/utils/` ou `src/styles/custom.css` que si aucun comportement natif ne le couvre, sous forme d'adaptation légère réutilisant les composants et variables CSS Starlight (`--sl-*`).
- **Documenter le code et la configuration** : tout code spécifique (`src/components/`, `src/utils/`, `src/pages/`, `src/styles/custom.css`) ou option ajoutée dans `astro.config.mjs` doit être brièvement commenté en français pour expliquer *pourquoi* il existe.

---

## 2. Stack, environnement et commandes

- **Framework :** Astro + Starlight (`base: '/formation-api-rest'`, `site: 'https://gaetan-varlet.github.io'`).
- **Gestionnaire de paquets :** `pnpm` uniquement (`pnpm-lock.yaml`).
- **Langue :** contenu, titres, commentaires de code et messages de commit en **français**.
- **Autonomie réseau :** ne jamais ajouter de dépendance CDN externe chargée à l'exécution (polices, icônes, scripts). Toute ressource (dont `@iconify-json/devicon` et `@iconify-json/lucide`) doit être embarquée localement et rendue au build.
- **Commandes :**
  - `pnpm dev` (ou `astro dev --background`, géré via `astro dev stop`, `astro dev status` et `astro dev logs`) : serveur de développement (`http://localhost:4321/formation-api-rest/`)
  - `pnpm build` : build statique dans `dist/`, optimisation des images et indexation Pagefind
  - `pnpm preview` : prévisualisation du build final (indispensable pour tester la recherche Pagefind)
- **Documentation officielle Astro :** https://docs.astro.build ([routing](https://docs.astro.build/en/guides/routing/), [composants](https://docs.astro.build/en/basics/astro-components/), [content collections](https://docs.astro.build/en/guides/content-collections/), [styles](https://docs.astro.build/en/guides/styling/)).

---

## 3. Structure du contenu et navigation

- **Organisation des cours :** les 8 modules sont dans `src/content/docs/` (`fondamentaux/`, `spring-boot/`, `api-rest/`, `acces-donnees/`, `fichiers/`, `securite/`, `tests/`, `deploiement/`).
- **Navigation dans la sidebar (`astro.config.mjs`) :**
  - L'ordre des pages et les liens *Précédent / Suivant* sont définis explicitement dans le tableau `sidebar` de `astro.config.mjs`. Toute nouvelle page doit y être déclarée via son `slug`.
- **Rester en `.md` par défaut :** n'utiliser `.mdx` que lorsque la page importe réellement un composant (`<Tabs>`, `<Steps>`, `<Card>`, `<TechIcon>`, `<Badge>`, etc.). Lors de la conversion d'une page `.md` en `.mdx`, toujours supprimer l'ancien fichier `.md` pour éviter les doublons.
- **Niveaux de titres :** Starlight génère le `<h1>` à partir du `title` du frontmatter. Dans le corps des pages, commencer par `##` (`H2`) puis `###` (`H3`) sans saut de niveau.
- **Liens internes :** toujours préfixer par `/formation-api-rest/...` avec slash final (ex. `/formation-api-rest/spring-boot/presentation-spring/`).
- **Images et schémas :** placer les assets dans `src/assets/images/` et les référencer en chemin relatif depuis le Markdown (ex. `../../../assets/images/client-serveur.svg`).

---

## 4. Icônes et rédaction homogène

### Icônes SVG (`TechIcon` / `Card`)

- Utiliser exclusivement le résolveur local `src/utils/tech-icons.ts` (`@iconify-json/devicon` et `@iconify-json/lucide`) via :
  - `src/components/TechIcon.astro` pour les icônes inline dans le texte ou les tableaux (`<TechIcon name="..." />`) ;
  - `src/components/Card.astro` pour les cartes Starlight (`<Card title="..." icon="...">`) : ce wrapper minimal passe le SVG résolu au `<Card>` natif de Starlight afin de conserver intégralement son rendu et ses thèmes par défaut.
- Vérifier le contraste de toute nouvelle icône Devicon en **thème clair et thème sombre** (si une icône Devicon est monochrome trop sombre, utiliser un équivalent Lucide héritant de `currentColor` via `ICON_ALIASES` dans `src/utils/tech-icons.ts`).

### Règles rédactionnelles Markdown / MDX

- **Zéro emoji dans les titres (`##`, `###`), les titres d'encadrés (`:::tip[...]`) et les `<summary>`**.
- **Zéro emoji décoratif devant les listes à puces ou les liens** (`🌐`, `🧪`, `📘`, `🎯`, `💡`…) : utiliser `<TechIcon name="..." />` pour les icônes inline, ou un encadré natif Starlight. Les symboles `✅` / `⚠️` restent tolérés uniquement dans les schémas ASCII en blocs de code texte brut ou comparatifs.
- **Encadrés natifs Starlight obligatoires :** utiliser `:::note[Titre]`, `:::tip[Titre]`, `:::caution[Titre]` ou `:::danger[Titre]` à la place des citations `>` pour les notes, astuces et avertissements. Réserver `>` aux vraies citations.
- **Séparateurs `---` :** ne pas multiplier les filets horizontaux inutiles entre sections quand l'espacement naturel des titres `##` suffit.

---

## 5. Accessibilité (RGAA) et styles

- **Lecteurs d'écran :**
  - Privilégier le HTML sémantique natif et une hiérarchie de titres stricte sans saut de niveau.
  - Fournir un `alt` explicite sur chaque image informative et accompagner les schémas complexes d'une transcription textuelle dans un bloc `<details><summary>...</summary>...</details>`.
  - Marquer les icônes décoratives avec `aria-hidden="true"` (géré automatiquement par `TechIcon` et `Card`).
- **Contraste et navigation clavier :** vérifier la lisibilité en mode clair et en mode sombre, ne jamais transmettre une information uniquement par la couleur et préserver les indicateurs de focus natifs Starlight.
- **CSS centralisé et minimal (`src/styles/custom.css`) :** s'appuyer sur les variables Starlight (`--sl-color-*`, `--sl-card-*`, `--sl-text-*`) et conserver `custom.css` aussi court que possible.

---

## 6. Vérifications avant livraison

1. Vérifier qu'aucun dossier ne contient à la fois `page.md` et `page.mdx`.
2. Exécuter `pnpm build` dans le répertoire du dépôt et vérifier que toutes les pages compilent sans erreur.
3. En cas d'ajout ou de modification d'icônes, vérifier dans le HTML généré sous `dist/` que les icônes sont bien rendues en `<svg>` (`card-tech-icon` / `tech-icon-inline`).

---

## 7. Messages de commit

- Utiliser le format **Conventional Commits** en français (`feat(scope): ...`, `fix(scope): ...`, `docs: ...`, `refactor: ...`, `chore: ...`).
- Formuler les descriptions avec un **nom** plutôt qu'un verbe à l'infinitif (ex. `ajout de ...`, `simplification de ...`, `correction de ...`).
- Privilégier des commits atomiques (un changement logique cohérent par commit).
