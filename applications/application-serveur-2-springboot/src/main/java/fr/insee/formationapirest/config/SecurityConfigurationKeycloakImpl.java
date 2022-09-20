package fr.insee.formationapirest.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

@ConditionalOnProperty(name = "formationapirest.security", havingValue = "keycloak")
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfigurationKeycloakImpl {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// désactivation CSRF car API
		http.csrf().disable();
		// désactivation des cookies de session
		http.sessionManagement().sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		// autoriser l'authentification par jeton JWT
		http.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())));
		// gestion des rôles : toutes les requêtes sont authentifiées sans rôle particulié
		// les droits supplémentaires sont gérés dans les controllers
		String[] urlsSwagger = { "/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**" };
		String[] urlsDivers = { "/info", "/healthcheck" };
		http.authorizeRequests(authz -> authz
				.antMatchers(HttpMethod.OPTIONS).permitAll()
				.antMatchers(urlsSwagger).permitAll()
				.antMatchers(urlsDivers).permitAll()
				.antMatchers("/vin", "/vin/**").permitAll()
				.antMatchers("/mon-nom").authenticated()
				.antMatchers("/environnement").hasRole("ADMIN_TOUCAN")
				.anyRequest().permitAll());
		return http.build();
	}

	// config pour avoir les rôles Keycloak et preffered_username comme principal
	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
		jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");
		return jwtAuthenticationConverter;
	}

	private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
		return new Converter<Jwt, Collection<GrantedAuthority>>() {
			@Override
			@SuppressWarnings({ "unchecked" })
			public Collection<GrantedAuthority> convert(Jwt source) {
				String oidcClaimRole = "realm_access.roles";
				String[] claimPath = oidcClaimRole.split("\\.");
				Map<String, Object> claims = source.getClaims();
				try {
					for (int i = 0; i < claimPath.length - 1; i++) {
						claims = (Map<String, Object>) claims.get(claimPath[i]);
					}
					if (claims == null) {
						return Collections.emptyList();
					}
					List<String> roles = (List<String>) claims
							.getOrDefault(claimPath[claimPath.length - 1], Collections.emptyList());
					return roles.stream()
							.map(s -> new GrantedAuthority() {
								@Override
								public String getAuthority() {
									return "ROLE_" + s;
								}
								@Override
								public String toString() {
									return getAuthority();
								}
							})
							.collect(Collectors.toList());
				} catch (ClassCastException e) {
					// role path not correctly found, assume that no role for this user
					return Collections.emptyList();
				}
			}
		};
	}
}
