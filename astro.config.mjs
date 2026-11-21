// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

/**
 * Plugin rehype minimaliste : ouvre les liens externes (http/https) dans un nouvel onglet
 * tout en restant accessible (icône visuelle masquée aux lecteurs d'écran + mention sr-only).
 */
function rehypeExternalLinks() {
	/** @param {any} node */
	function visit(node) {
		if (!node || typeof node !== 'object') return;
		if (
			node.type === 'element' &&
			node.tagName === 'a' &&
			typeof node.properties?.href === 'string' &&
			/^https?:\/\//.test(node.properties.href) &&
			!node.properties.target
		) {
			node.properties.target = '_blank';
			node.properties.rel = 'noopener noreferrer';
			node.children ??= [];
			node.children.push(
				{
					type: 'element',
					tagName: 'span',
					properties: { 'aria-hidden': 'true', className: ['external-link-icon'] },
					children: [
						{
							type: 'element',
							tagName: 'svg',
							properties: {
								viewBox: '0 0 24 24',
								fill: 'none',
								stroke: 'currentColor',
								strokeWidth: '2',
								strokeLinecap: 'round',
								strokeLinejoin: 'round',
							},
							children: [
								{
									type: 'element',
									tagName: 'path',
									properties: { d: 'M15 3h6v6' },
									children: [],
								},
								{
									type: 'element',
									tagName: 'path',
									properties: { d: 'M10 14 21 3' },
									children: [],
								},
								{
									type: 'element',
									tagName: 'path',
									properties: { d: 'M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6' },
									children: [],
								},
							],
						},
					],
				},
				{
					type: 'element',
					tagName: 'span',
					properties: { className: ['sr-only'] },
					children: [{ type: 'text', value: ' (nouvel onglet)' }],
				}
			);
			return;
		}
		if (Array.isArray(node.children)) {
			for (const child of node.children) visit(child);
		}
	}
	return visit;
}

// https://astro.build/config
export default defineConfig({
	site: 'https://gaetan-varlet.github.io',
	base: '/formation-api-rest',
	markdown: {
		rehypePlugins: [rehypeExternalLinks],
	},
	integrations: [
		starlight({
			title: 'Formation API REST',
			customCss: ['./src/styles/custom.css'],
			defaultLocale: 'root',
			locales: {
				root: {
					label: 'Français',
					lang: 'fr',
				},
			},
			social: [
				{
					icon: 'github',
					label: 'GitHub',
					href: 'https://github.com/gaetan-varlet/formation-api-rest',
				},
			],
			sidebar: [
				{
					label: 'Fondamentaux',
					items: [
						{ slug: 'fondamentaux/introduction' },
						{ slug: 'fondamentaux/protocole-http' },
						{ slug: 'fondamentaux/architecture' },
						{ slug: 'fondamentaux/exemple-java-ee' },
					],
				},
				{
					label: 'Démarrer avec Spring Boot',
					items: [
						{ slug: 'spring-boot/presentation-spring' },
						{ slug: 'spring-boot/injection-dependances' },
						{ slug: 'spring-boot/hello-world' },
						{ slug: 'spring-boot/configuration-bdd' },
						{ slug: 'spring-boot/configuration-logs' },
						{ slug: 'spring-boot/configuration-spring-boot' },
					],
				},
				{
					label: "Développement d'API REST",
					items: [
						{ slug: 'api-rest/creation-crud' },
						{ slug: 'api-rest/annotations' },
						{ slug: 'api-rest/gestion-erreurs' },
						{ slug: 'api-rest/contrats-api' },
						{ slug: 'api-rest/swagger' },
						{ slug: 'api-rest/consommation-api-rest' },
					],
				},
				{
					label: 'Accès aux données & SQL',
					items: [
						{ slug: 'acces-donnees/jpa-avance' },
						{ slug: 'acces-donnees/jdbc-sql' },
						{ slug: 'acces-donnees/benchmark-sql' },
					],
				},
				{
					label: 'Fichiers, Streaming & Cache',
					items: [
						{ slug: 'fichiers-performance/reception-fichier' },
						{ slug: 'fichiers-performance/export-csv' },
						{ slug: 'fichiers-performance/streaming' },
						{ slug: 'fichiers-performance/cache' },
					],
				},
				{
					label: 'Sécurité & Filtres',
					items: [
						{ slug: 'securite/spring-security-keycloak' },
						{ slug: 'securite/filtres' },
						{ slug: 'securite/neutralisation-entrees' },
					],
				},
				{
					label: 'Tests',
					items: [
						{ slug: 'tests/strategie-tests' },
						{ slug: 'tests/configuration-tests' },
						{ slug: 'tests/tests-unitaires' },
						{ slug: 'tests/tests-integration' },
						{ slug: 'tests/cucumber' },
						{ slug: 'tests/cucumber-it' },
						{ slug: 'tests/wiremock' },
						{ slug: 'tests/cucumber-gitlab' },
					],
				},
				{
					label: 'Production & Déploiement',
					items: [
						{ slug: 'deploiement/actuator' },
						{ slug: 'deploiement/scheduling' },
						{ slug: 'deploiement/packaging-environnements' },
						{ slug: 'deploiement/kubernetes' },
						{ slug: 'deploiement/jar-vm' },
						{ slug: 'deploiement/war-vm' },
					],
				},
			],
		}),
	],
});
