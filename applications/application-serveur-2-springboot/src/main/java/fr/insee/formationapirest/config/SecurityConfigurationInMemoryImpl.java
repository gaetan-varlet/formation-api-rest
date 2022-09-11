package fr.insee.formationapirest.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

@ConditionalOnProperty(name = "formationapirest.security", havingValue = "in-memory")
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfigurationInMemoryImpl {

	@Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user1 = User.withUsername("admin").password("{noop}admin").roles("ADMIN_TOUCAN", "CONSULTANT_TOUCAN").build();
		UserDetails user2 = User.withUsername("consul").password("{noop}consul").roles("CONSULTANT_TOUCAN").build();
        return new InMemoryUserDetailsManager(user1, user2);
    }

	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// désactivation CSRF car API
		http.csrf().disable();
		// désactivation des cookies de session
		http.sessionManagement().sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
		.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.authorizeRequests(authz -> authz
			// configuration pour Swagger
			.antMatchers("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
			// autorisation des requetes OPTIONS
			.antMatchers(HttpMethod.OPTIONS).permitAll()
			// configuration des autres requêtes
			.antMatchers("/url1", "/url2").permitAll()
			.antMatchers("/vin", "/vin/**").permitAll()
			.antMatchers("/mon-nom").authenticated()
			.antMatchers("/environnement").hasRole("ADMIN_TOUCAN")
			.anyRequest().permitAll());
		// mode basic
		http.httpBasic();
		// autorisation d'afficher des frames dans l'appli pour afficher la console h2 (risque de clickjacking)
		http.headers().frameOptions().sameOrigin();
        return http.build();
    }

}
