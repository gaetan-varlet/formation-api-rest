package fr.insee.formationapirest.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

@ConditionalOnProperty(name = "formationapirest.security", havingValue = "in-memory")
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfigurationInMemoryImpl {

        @Bean
        public InMemoryUserDetailsManager userDetailsService() {
                UserDetails user1 = User.withUsername("admin").password("{noop}admin")
                                .roles("ADMIN_TOUCAN", "CONSULTANT_TOUCAN").build();
                UserDetails user2 = User.withUsername("consul").password("{noop}consul").roles("CONSULTANT_TOUCAN")
                                .build();
                return new InMemoryUserDetailsManager(user1, user2);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.cors(Customizer.withDefaults());
                http.csrf(AbstractHttpConfigurer::disable);
                http.sessionManagement(
                                session -> session.sessionAuthenticationStrategy(
                                                new NullAuthenticatedSessionStrategy())
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                // gestion des droits (TODO à revoir avec Spring Secu 6.1)
                String[] urlsSwagger = { "/", "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**" };
                String[] urlsDivers = { "/info", "/healthcheck" };
                http.authorizeHttpRequests(authz -> authz
                                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                                .requestMatchers(urlsSwagger).permitAll()
                                .requestMatchers(urlsDivers).permitAll()
                                .requestMatchers(toH2Console()).permitAll()
                                // configuration des autres requêtes
                                .requestMatchers("/url1", "/url2").permitAll()
                                .requestMatchers("/vin", "/vin/**").permitAll()
                                .requestMatchers("/mon-nom").authenticated()
                                .requestMatchers("/environnement").hasRole("ADMIN_TOUCAN")
                                .anyRequest().authenticated());
                // mode basic
                http.httpBasic(Customizer.withDefaults());
                // autorisation d'afficher des frames dans l'appli pour afficher la console h2
                // (risque de clickjacking)
                http.headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin));
                return http.build();
        }

}
