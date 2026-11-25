import { execFileSync } from 'node:child_process';
import { resolve } from 'node:path';
import rss from '@astrojs/rss';
import type { APIContext } from 'astro';
import { getCollection } from 'astro:content';

/**
 * Récupère en une seule passe l'horodatage (ms) du dernier commit Git pour chaque fichier
 * sous `src/content/docs`, indexé par son chemin absolu normalisé.
 */
function getLatestCommitDatesByPath(): Map<string, Date> {
	const datesByPath = new Map<string, Date>();

	try {
		const repoRoot = execFileSync('git', ['rev-parse', '--show-toplevel'], {
			encoding: 'utf-8',
		}).trim();

		const gitLog = execFileSync(
			'git',
			['log', '--format=t:%ct', '--name-only', '-z', '--', 'src/content/docs'],
			{
				cwd: repoRoot,
				encoding: 'utf-8',
				maxBuffer: 10 * 1024 * 1024,
			},
		);

		let currentDate: Date | undefined;
		for (const rawEntry of gitLog.split('\0')) {
			const entry = rawEntry.replace(/^\n/, '');
			if (!entry) continue;

			if (/^t:\d+$/.test(entry)) {
				currentDate = new Date(Number.parseInt(entry.slice(2), 10) * 1000);
				continue;
			}

			if (!currentDate) continue;

			const absPath = resolve(repoRoot, entry);
			const existing = datesByPath.get(absPath);
			if (!existing || currentDate > existing) {
				datesByPath.set(absPath, currentDate);
			}
		}
	} catch {
		// Hors dépôt Git : on utilisera la date de build en secours.
	}

	return datesByPath;
}

export async function GET(context: APIContext) {
	if (!context.site) {
		throw new Error('L’option `site` doit être définie dans astro.config.mjs pour générer le flux RSS.');
	}

	const baseUrl = import.meta.env.BASE_URL.endsWith('/')
		? import.meta.env.BASE_URL
		: `${import.meta.env.BASE_URL}/`;

	const docs = await getCollection('docs', ({ data }) => !data.draft);
	const commitDates = getLatestCommitDatesByPath();
	const fallbackDate = new Date();

	const items = docs
		.map((doc) => {
			const gitDate = doc.filePath ? commitDates.get(resolve(doc.filePath)) : undefined;
			const pubDate = doc.data.lastUpdated instanceof Date ? doc.data.lastUpdated : (gitDate ?? fallbackDate);
			const slug = doc.id === 'index' ? '' : `${doc.id}/`;

			return {
				title: doc.data.title,
				description: doc.data.description,
				pubDate,
				link: `${baseUrl}${slug}`,
			};
		})
		.sort((a, b) => b.pubDate.getTime() - a.pubDate.getTime());

	return rss({
		title: 'Formation API REST',
		description: 'Support de formation pour concevoir, tester, sécuriser et déployer des API REST avec Spring Boot.',
		site: new URL(baseUrl, context.site),
		customData: '<language>fr</language>',
		items,
	});
}
