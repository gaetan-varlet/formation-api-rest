package fr.insee.formationapirest.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Lists;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationCodeGrant;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.TokenEndpoint;
import springfox.documentation.service.TokenRequestEndpoint;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	private String SECURITY_SCHEMA_OAUTH2 = "oauth2";
	@Value("${formationapirest.keycloak.client.id}")
	private String CLIENT_ID;
	@Value("${keycloak.auth-server-url}")
	private String URL_KEYCLOAK;
	@Value("${keycloak.realm}")
	private String REALM;
	
	@Bean
	public Docket productApi() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("fr.insee.formationapirest.controller")).build().apiInfo(apiInfo)
				.useDefaultResponseMessages(false)
				.globalResponseMessage(RequestMethod.GET,
						Lists.newArrayList(
								new ResponseMessageBuilder().code(500).message("Erreur interne du côté serveur").build(),
								new ResponseMessageBuilder().code(403).message("Interdit!").build()))
				.securitySchemes(Arrays.asList(securitySchema())).securityContexts(Arrays.asList(securityContext()));
	}
	
	private ApiInfo apiInfo = new ApiInfo("Formation API REST", "Documentation du webservice", "v1.0.0", "",
			new Contact("équipe info", null, "gaetan.varlet@insee.fr"), "", "", Collections.emptyList());
	
	private OAuth securitySchema() {
		String AUTH_SERVER = "/realms/" + REALM + "/protocol/openid-connect/auth";
		String AUTH_SERVER_TOKEN_ENDPOINT = "/realms/" + REALM + "/protocol/openid-connect/token";
		TokenRequestEndpoint tokenRequestEndpoint = new TokenRequestEndpoint(URL_KEYCLOAK + AUTH_SERVER, CLIENT_ID, null);
		TokenEndpoint tokenEndpoint = new TokenEndpoint(URL_KEYCLOAK + AUTH_SERVER_TOKEN_ENDPOINT, "access_token");
		final GrantType grantType = new AuthorizationCodeGrant(tokenRequestEndpoint, tokenEndpoint);
		final List<AuthorizationScope> scopes = new ArrayList<>();
		scopes.add(new AuthorizationScope("sampleScope", "there must be at least one scope here"));
		return new OAuth(SECURITY_SCHEMA_OAUTH2, scopes, Collections.singletonList(grantType));
	}
	
	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.any()).build();
	}
	
	private List<SecurityReference> defaultAuth() {
		final AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		final AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Collections.singletonList(new SecurityReference(SECURITY_SCHEMA_OAUTH2, authorizationScopes));
	}
	
	@Bean
	public SecurityConfiguration security() {
		return SecurityConfigurationBuilder.builder().clientId(CLIENT_ID).realm(REALM).scopeSeparator(",").build();
	}
}