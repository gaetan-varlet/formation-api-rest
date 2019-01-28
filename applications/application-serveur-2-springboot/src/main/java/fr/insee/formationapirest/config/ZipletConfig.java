package fr.insee.formationapirest.config;

import javax.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.ziplet.filter.compression.CompressingFilter;

@Configuration
public class ZipletConfig {
	
	@Bean
	public Filter compressingFilter() {
		return new CompressingFilter();
	}
	
}
