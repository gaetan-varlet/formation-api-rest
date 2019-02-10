package fr.insee.formationapirest.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.github.ziplet.filter.compression.CompressingFilter;

@Configuration
public class FilterConfig {
	
	
	// Congifuration du filtre pour la gestion du CORS
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setMaxAge(3600L);
		source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorsFilter(source));
        registrationBean.setOrder(0);
		return registrationBean;
	}	
	
	// Configuration du filtre de compression de la réponse
	@Bean
	public FilterRegistrationBean<CompressingFilter> compressingFilter(){
		FilterRegistrationBean<CompressingFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new CompressingFilter());
		registrationBean.setOrder(1);
		return registrationBean;
	}
//	@Bean
//	public Filter compressFilter() {
//	    return new CompressingFilter();
//	}
	

	@Bean
	public FilterRegistrationBean<PartialFilter> loggingFilter(){
	    FilterRegistrationBean<PartialFilter> registrationBean = new FilterRegistrationBean<>();        
	    registrationBean.setFilter(new PartialFilter());
	    registrationBean.addUrlPatterns("/vin/*");
	    registrationBean.setOrder(3);        
	    return registrationBean;    
	}

}