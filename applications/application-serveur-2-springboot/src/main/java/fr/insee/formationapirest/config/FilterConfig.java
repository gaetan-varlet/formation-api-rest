package fr.insee.formationapirest.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class FilterConfig {

	// Configuration du filtre pour la gestion du CORS
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		// config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new CorsFilter(source));
		registrationBean.setOrder(0);
		return registrationBean;
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