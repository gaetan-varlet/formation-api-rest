package fr.insee.formationapirest;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SpringBootApplication
@EnableCaching
public class FormationApiRestApplication {
	
	private static final String NOM_FICHIER_PROPERTIES = "formation-api-rest";
	
	public static void main(String[] args) {
		// définition de la property pour le local
		System.setProperty("spring.config.name", NOM_FICHIER_PROPERTIES);
		SpringApplication.run(FormationApiRestApplication.class, args);
	}
	
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		// spring.config.location permet de définir les chemins où spring va chercher des fichiers de properties pour la prod pour le CEI
		return application.properties(
				"spring.config.location=classpath:/,file:///${catalina.base}/webapps/formation.properties",
				"spring.config.name="+NOM_FICHIER_PROPERTIES // définition de la property pour le foncitonnement sur les plateformes du CEI
				).sources(FormationApiRestApplication.class);
	}
	
	@Bean
	@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public AccessToken getAccessToken() {
		HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		KeycloakSecurityContext securityContext = (KeycloakSecurityContext) httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
		if(securityContext != null) {
			return securityContext.getToken(); 
		} else {
			return new AccessToken();
		} 
	}
	
}
