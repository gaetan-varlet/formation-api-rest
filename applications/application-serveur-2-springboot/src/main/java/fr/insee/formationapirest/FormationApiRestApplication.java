package fr.insee.formationapirest;

import java.security.Principal;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.formationapirest.config.PropertiesLogger;
import jakarta.servlet.http.HttpServletRequest;

@SpringBootApplication
@EnableCaching
public class FormationApiRestApplication {

	public static void main(String[] args) {
		SpringApplication sa = new SpringApplication(FormationApiRestApplication.class);
		sa.addListeners(new PropertiesLogger());
		sa.run(args);
	}

	@Bean
	@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	// renvoie le principal mis dans la requête par Keycloak
	// ou un principal avec un "name" null sinon
	public Principal getPrincipal(HttpServletRequest httpRequest) {
		return Optional.ofNullable(httpRequest.getUserPrincipal()).orElse(() -> null);
	}

}
