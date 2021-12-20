package fr.insee.formationapirest;

import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

@SpringBootApplication
@EnableCaching
public class FormationApiRestApplication extends SpringBootServletInitializer {

	private static final String NOM_FICHIER_PROPERTIES = "formation-api-rest";

	public static void main(String[] args) {
		// définition des system properties pour le local
		System.setProperty("spring.config.name", NOM_FICHIER_PROPERTIES);
		SpringApplication.run(FormationApiRestApplication.class, args);
	}

	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		// spring.config.name permet de définir le nom du fichier de properties lu
		// automatiquement par springboot sous src/main/resources
		// spring.config.location permet de définir les chemins où spring va chercher
		// pour des fichiers de properties à charger (ceux définis à la fin sont
		// prioritaires)
		return application.properties(
				"spring.config.location=classpath:/formation-api-rest.properties, file:${catalina.base}/webapps/formation.properties",
				"spring.config.name=" + NOM_FICHIER_PROPERTIES
		// définition de la property pour le fonctionnement sur les plateformes du CEI
		).sources(FormationApiRestApplication.class);
	}

	@Bean
	@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	// renvoie le principal mis dans la requête par Keycloak ou un principal avec un
	// "name" null sinon
	public Principal getPrincipal(HttpServletRequest httpRequest) {
		return Optional.ofNullable(httpRequest.getUserPrincipal()).orElse(() -> null);
	}

}
