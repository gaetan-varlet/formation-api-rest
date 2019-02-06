package fr.insee.formationapirest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class FormationApiRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormationApiRestApplication.class, args);
	}
	
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {		
		// spring.config.location permet de définir les chemins où spring va chercher des fichiers de properties pour la prod pour le CEI
		return application.properties(
				"spring.config.location=classpath:/,file:///${catalina.base}/webapps/formation.properties"
				).sources(FormationApiRestApplication.class);
	}

}

