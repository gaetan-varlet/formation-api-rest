package fr.insee.formationapirest.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FilterConfig {

	@Value("${fr.insee.cors.authorized.urls}")
	private String[] urls;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		log.info("URLs autorisées pour faire des requêtes HTTP depuis le navigateur : {}", List.of(urls));
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of(urls));
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