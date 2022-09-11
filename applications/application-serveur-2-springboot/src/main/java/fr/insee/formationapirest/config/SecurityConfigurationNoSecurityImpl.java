package fr.insee.formationapirest.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

@ConditionalOnProperty(name = "formationapirest.security", havingValue = "no-security")
@Configuration
public class SecurityConfigurationNoSecurityImpl {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.headers().frameOptions().disable();
        http.sessionManagement().sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.authorizeRequests().antMatchers("/**").permitAll();
        return http.build();
    }
    
}
