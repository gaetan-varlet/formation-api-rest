package fr.insee.formationapirest.config;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class FilterConfig {

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

	@Bean
	public FilterRegistrationBean<PartialFilter> loggingFilter() {
		FilterRegistrationBean<PartialFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new PartialFilter());
		registrationBean.addUrlPatterns("/vin/*");
		registrationBean.setOrder(3);
		return registrationBean;
	}

}