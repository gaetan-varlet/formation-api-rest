package fr.insee.formationapirest.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

@ConditionalOnProperty(name = "formationapirest.security", havingValue = "keycloak")
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfigurationKeycloakImpl {

    @Value("${spring.h2.console.enabled}")
    private boolean h2Enable;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // désactivation CSRF car API (ne pas faire dans le cas d'une appli avec JSP)
        http.csrf(csrf -> csrf.disable());
        // désactivation des cookies de session
        // (ne pas faire dans le cas d'une appli avec JSP)
        http.sessionManagement(
                session -> session.sessionAuthenticationStrategy(
                        new NullAuthenticatedSessionStrategy())
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // autoriser l'authentification par jeton JWT
        http.oauth2ResourceServer(
                oauth2 -> oauth2.jwt(
                        jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(
                                jwtAuthenticationConverter())));
        // gestion des rôles
        http.authorizeHttpRequests(this::authorizedUrls);
        // autorisation d'afficher des frames dans l'appli pour afficher la console h2
        // (risque de clickjacking)
        if (h2Enable) {
            http.headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin));
        }
        return http.build();
    }

    private void authorizedUrls(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        String[] urlsSwagger = { "/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**" };
        String[] urlsDivers = { "/info", "/healthcheck" };
        String[] publicUrls = { "/vin", "/vin/**" };
        String[] restrictedUrls = { "/mon-nom" };
        String[] adminUrls = { "/environnement" };

        for (String url : urlsSwagger) {
            authorize.requestMatchers(antMatcher(HttpMethod.GET, url)).permitAll();
        }
        for (String url : urlsDivers) {
            authorize.requestMatchers(antMatcher(HttpMethod.GET, url)).permitAll();
        }
        for (String url : publicUrls) {
            authorize.requestMatchers(antMatcher(HttpMethod.GET, url)).permitAll();
        }
        for (String url : restrictedUrls) {
            authorize.requestMatchers(antMatcher(url)).authenticated();
        }
        for (String url : adminUrls) {
            authorize.requestMatchers(antMatcher(url)).hasRole("ADMIN_TOUCAN");
        }
        // H2
        if (h2Enable) {
            authorize.requestMatchers(toH2Console()).permitAll();
        }
        // autorisation des requetes OPTIONS
        authorize.requestMatchers(antMatcher(HttpMethod.OPTIONS)).permitAll();
        // interdiction de toutes les autres requêtes
        authorize.anyRequest().denyAll();
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
