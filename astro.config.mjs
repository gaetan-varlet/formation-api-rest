// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
	site: 'https://gaetan-varlet.github.io',
	base: '/formation-api-rest',
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
						{ slug: 'fondamentaux/consommation-api-rest' },
					],
				},
				{
					label: 'Démarrer avec Spring Boot',
					items: [
						{ slug: 'spring-boot/presentation-spring' },
						{ slug: 'spring-boot/injection-dependances' },
						{ slug: 'spring-boot/hello-world' },
						{ slug: 'spring-boot/config-bdd' },
						{ slug: 'spring-boot/config-log' },
						{ slug: 'spring-boot/configuration-spring-boot' },
					],
				},
				{
					label: "Développement d'API REST",
					items: [
						{ slug: 'api-rest/creation-crud' },
						{ slug: 'api-rest/gestion-erreurs' },
						{ slug: 'api-rest/annotations' },
						{ slug: 'api-rest/contrats-api' },
						{ slug: 'api-rest/requetes-sql' },
						{ slug: 'api-rest/swagger' },
					],
				},
				{
					label: 'Fichiers, Streaming & Performance',
					items: [
						{ slug: 'fichiers-performance/reception-fichier' },
						{ slug: 'fichiers-performance/creation-csv' },
						{ slug: 'fichiers-performance/streaming' },
						{ slug: 'fichiers-performance/benchmark-sql' },
						{ slug: 'fichiers-performance/cache' },
						{ slug: 'fichiers-performance/scheduling' },
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
						{ slug: 'tests/strategie-test' },
						{ slug: 'tests/config-tests' },
						{ slug: 'tests/tests-unitaires' },
						{ slug: 'tests/tests-spring' },
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
						{ slug: 'deploiement/deploiement-jar-vm' },
						{ slug: 'deploiement/deploiement-war-vm' },
						{ slug: 'deploiement/deploiement-kubernetes' },
					],
				},
			],
		}),
	],
});
