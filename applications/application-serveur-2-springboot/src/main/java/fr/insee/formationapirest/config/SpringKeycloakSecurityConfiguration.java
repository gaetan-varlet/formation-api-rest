//package fr.insee.formationapirest.config;
//
//
//
//import org.keycloak.adapters.KeycloakConfigResolver;
//import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
//import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
//import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
//import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
//import org.springframework.security.web.authentication.logout.LogoutFilter;
//import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
//import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
//import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
//
//// Tuto pour mise en place :
//// https://blog.ineat-conseil.fr/2017/12/securisez-vos-apis-spring-avec-keycloak-3-utilisation-des-connecteurs-spring-de-keycloak/
//
//public class SpringKeycloakSecurityConfiguration {
//	
//	@Configuration
//	@EnableWebSecurity
//	@ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true", matchIfMissing = true)
//	@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
//	public static class KeycloakConfigurationAdapter extends KeycloakWebSecurityConfigurerAdapter {
//		
//		@Bean
//		@Override
//		protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
//			// required for bearer-only applications.
//			return new NullAuthenticatedSessionStrategy();
//		}
//		
//		@Autowired
//		public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//			KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
//			// simple Authority Mapper to avoid ROLE_
//			keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
//			auth.authenticationProvider(keycloakAuthenticationProvider);
//		}
//		
//		@Bean
//		public KeycloakConfigResolver KeycloakConfigResolver() {
//			return new KeycloakSpringBootConfigResolver();
//		}
//		
//		@Override
//		protected void configure(HttpSecurity http) throws Exception {
//			http
//					// disable csrf because of API mode
//					.csrf().disable().sessionManagement()
//					// use previously declared bean
//					.sessionAuthenticationStrategy(sessionAuthenticationStrategy()).sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//					// keycloak filters for securisation
//					.and().addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class)
//					.addFilterBefore(keycloakAuthenticationProcessingFilter(), X509AuthenticationFilter.class).exceptionHandling()
//					.authenticationEntryPoint(authenticationEntryPoint()).and()
//					// manage routes securisation here
//					.authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
//					// configuration pour Swagger
//					.antMatchers("/swagger-ui.html/**", "/v2/api-docs","/csrf", "/", "/webjars/**", "/swagger-resources/**").permitAll()
//					// configuration de nos URLS
//					.antMatchers("/url1", "/url2").permitAll()
//					.antMatchers("/environnement").hasRole("TOUCAN_ADMIN")
//					.antMatchers("/mon-nom").authenticated()
////					.anyRequest().denyAll()
//					;
//		}
//		
//	}
//}