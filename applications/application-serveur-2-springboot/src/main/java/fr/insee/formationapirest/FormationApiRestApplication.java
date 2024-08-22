package fr.insee.formationapirest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import fr.insee.formationapirest.config.PropertiesLogger;

@SpringBootApplication
@EnableCaching
public class FormationApiRestApplication {

	public static void main(String[] args) {
		SpringApplication sa = new SpringApplication(FormationApiRestApplication.class);
		sa.addListeners(new PropertiesLogger());
		sa.run(args);
	}

}
