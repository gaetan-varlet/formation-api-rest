package fr.insee.formationapirest.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfiguration {

	@Value("${keycloak.auth-server-url:}")
	private String keycloakUrl;

	@Value("${keycloak.realm:}")
	private String realmName;

	@Value("${info.versionApplication}")
	private String version;

	private static final String SCHEMEKEYCLOAK = "oAuthScheme";
	private static final String REALMS = "/realms/";

	@Bean
	public OpenAPI customOpenAPIKeycloak() {
		// configuration pour récupérer un jeton auprès de Keycloak
		Scopes scopes = new Scopes();
		scopes.put("role-as-group", "obtenir les rôles");
		scopes.put("profile", "obtenir le nom, prénom, preferred username...");

		final OpenAPI openapi = new OpenAPI().info(new Info().title("Swagger Formation API REST").version(version));
		openapi.components(new Components().addSecuritySchemes(SCHEMEKEYCLOAK, new SecurityScheme()
				.type(SecurityScheme.Type.OAUTH2).in(SecurityScheme.In.HEADER)
				.description("Authentification keycloak")
				.flows(new OAuthFlows().authorizationCode(new OAuthFlow().scopes(scopes)
						.authorizationUrl(keycloakUrl + REALMS + realmName + "/protocol/openid-connect/auth")
						.tokenUrl(keycloakUrl + REALMS + realmName + "/protocol/openid-connect/token")
						.refreshUrl(keycloakUrl + REALMS + realmName + "/protocol/openid-connect/token")))));
		return openapi;
	}

	@Bean
	public OperationCustomizer ajouterKeycloak() {
		// configuration pour que Swagger utilise le jeton récupéré auprès de Keycloak
		return (operation, handlerMethod) -> operation
				.addSecurityItem(new SecurityRequirement().addList(SCHEMEKEYCLOAK));
	}

}