package fr.insee.formationapirest;

import java.security.Principal;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

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

	public static void main(String[] args) {
		SpringApplication.run(FormationApiRestApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		// spring.config.location défintit les chemins où spring va chercher les
		// fichiers de properties à charger (ceux définis à la fin sont prioritaires)
		return application.properties(
				"spring.config.location=classpath:/formation-api-rest.properties, file:${catalina.base}/webapps/formation.properties")
				.sources(FormationApiRestApplication.class);
	}

	@Bean
	@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	// renvoie le principal mis dans la requête par Keycloak
	// ou un principal avec un "name" null sinon
	public Principal getPrincipal(HttpServletRequest httpRequest) {
		return Optional.ofNullable(httpRequest.getUserPrincipal()).orElse(() -> null);
	}

}
