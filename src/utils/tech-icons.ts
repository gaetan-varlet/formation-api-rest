/**
 * Résolveur local d'icônes SVG (Devicon + Lucide).
 *
 * Pourquoi ce fichier existe : les paquets `@iconify-json/devicon` et `@iconify-json/lucide`
 * ne fournissent pas de composants Astro importables, mais des dictionnaires JSON contenant
 * les chemins SVG bruts (`<path .../>`). Ce module lit ces JSON uniquement au moment du build
 * afin d'embarquer des SVG inline sans JavaScript client ni appel CDN externe.
 */
import devicon from '@iconify-json/devicon/icons.json';
import lucide from '@iconify-json/lucide/icons.json';

interface IconifyEntry {
	body: string;
	width?: number;
	height?: number;
}

interface IconifyCollection {
	width?: number;
	height?: number;
	icons: Record<string, IconifyEntry>;
	aliases?: Record<string, { parent: string }>;
}

export interface ResolvedTechIcon {
	body: string;
	viewBox: string;
}

const COLLECTIONS: Record<string, IconifyCollection> = {
	devicon: devicon as IconifyCollection,
	lucide: lucide as IconifyCollection,
};

// Tailles de grille par défaut utilisées lorsque l'entrée JSON omet `width` / `height`
const DEFAULT_SIZES: Record<string, number> = {
	devicon: 128,
	lucide: 24,
};

// Noms courts métiers alignés sur `../portail-animation-dev` ; permet aussi de substituer
// une icône Lucide (héritant de `currentColor`) si une icône Devicon manque de contraste.
const ICON_ALIASES: Record<string, string> = {
	springboot: 'devicon:spring',
	book: 'lucide:book-open',
	api: 'lucide:braces',
	storage: 'lucide:database',
	files: 'lucide:folder-open',
	security: 'lucide:shield-check',
	tests: 'lucide:flask-conical',
	deploy: 'lucide:rocket',
	bash: 'lucide:terminal',
	wrench: 'lucide:wrench',
	layers: 'lucide:layers',
	warning: 'lucide:triangle-alert',
};

function findInCollection(collectionName: string, key: string): ResolvedTechIcon | null {
	const collection = COLLECTIONS[collectionName];
	if (!collection) return null;

	// Iconify peut définir des alias internes vers une icône parente dans le même JSON
	const resolvedKey = collection.aliases?.[key]?.parent ?? key;
	const entry = collection.icons[resolvedKey];
	if (!entry) return null;

	const fallback = DEFAULT_SIZES[collectionName] ?? 24;
	const width = entry.width ?? collection.width ?? fallback;
	const height = entry.height ?? collection.height ?? fallback;

	return {
		body: entry.body,
		viewBox: `0 0 ${width} ${height}`,
	};
}

/**
 * Résout un identifiant d'icône (`springboot`, `puzzle`, `lucide:terminal`, etc.)
 * vers son SVG inline Devicon ou Lucide. Retourne `null` si l'identifiant est inconnu
 * (`TechIcon` n'affiche rien et `Card` affiche le titre sans icône).
 */
export function resolveTechIcon(name: string | undefined): ResolvedTechIcon | null {
	if (!name) return null;
	const normalized = name.trim().toLowerCase();
	if (!normalized) return null;

	const mapped = ICON_ALIASES[normalized] ?? normalized;
	const colonIdx = mapped.indexOf(':');
	if (colonIdx !== -1) {
		const prefix = mapped.slice(0, colonIdx);
		const key = mapped.slice(colonIdx + 1);
		return findInCollection(prefix, key);
	}

	return findInCollection('devicon', mapped) ?? findInCollection('lucide', mapped);
}
