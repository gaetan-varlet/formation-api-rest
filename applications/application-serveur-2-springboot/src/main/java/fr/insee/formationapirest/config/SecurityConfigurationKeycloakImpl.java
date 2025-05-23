package fr.insee.formationapirest.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
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
        // fonctionnement du filtre CORS avec Spring Security
        // chargement de la configuration CORS définie dans un Bean
        // CORS doit être traité en premier, sinon Spring Secu rejettera la demande
        http.cors(Customizer.withDefaults());
        // désactivation CSRF car API (ne pas faire dans le cas d'une appli avec JSP)
        http.csrf(AbstractHttpConfigurer::disable);
        // désactivation des cookies de session
        // (ne pas faire dans le cas d'une appli avec JSP)
        http.sessionManagement(
                session -> session.sessionAuthenticationStrategy(
                        new NullAuthenticatedSessionStrategy())
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // autoriser l'authentification par jeton JWT
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> Customizer.withDefaults()));
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
        String[] publicUrls = { "/vin", "/vin/**", "/hello" };
        String[] restrictedUrls = { "/mon-nom", "/hello-secured" };
        String[] adminUrls = { "/environnement" };
        // gestions de nos endpoints
        for (String url : urlsSwagger) {
            authorize.requestMatchers(HttpMethod.GET, url).permitAll();
        }
        for (String url : urlsDivers) {
            authorize.requestMatchers(HttpMethod.GET, url).permitAll();
        }
        for (String url : publicUrls) {
            authorize.requestMatchers(HttpMethod.GET, url).permitAll();
        }
        for (String url : restrictedUrls) {
            authorize.requestMatchers(url).authenticated();
        }
        for (String url : adminUrls) {
            authorize.requestMatchers(url).hasRole("ADMIN_TOUCAN");
        }
        // H2
        if (h2Enable) {
            authorize.requestMatchers(toH2Console()).permitAll();
        }
        // interdiction de toutes les autres requêtes
        authorize.anyRequest().authenticated();
    }

}
