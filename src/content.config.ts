/**
 * Configuration standard des collections de contenu Astro / Starlight.
 * - `docsLoader()` charge automatiquement les pages `.md` / `.mdx` depuis `src/content/docs/`.
 * - `docsSchema()` valide les champs de frontmatter YAML attendus par Starlight
 *   (`title`, `description`, `draft`, `lastUpdated`, `template`, `hero`, etc.).
 */
import { defineCollection } from 'astro:content';
import { docsLoader } from '@astrojs/starlight/loaders';
import { docsSchema } from '@astrojs/starlight/schema';

export const collections = {
	docs: defineCollection({ loader: docsLoader(), schema: docsSchema() }),
};
