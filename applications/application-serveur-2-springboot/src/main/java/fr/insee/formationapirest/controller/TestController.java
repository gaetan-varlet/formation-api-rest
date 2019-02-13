package fr.insee.formationapirest.controller;

import java.io.IOException;
import java.util.stream.Collectors;

import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TestController {
	
	private static final Logger log = LoggerFactory.getLogger(TestController.class);
	
	@Value("${monNom}")
	private String nom;
	
	@Value("${formationapirest.environnement}")
	private String environnement;
	
	@Autowired
	private AccessToken accessToken;
	
	@RequestMapping(value="mon-nom", method = RequestMethod.GET)
	public String getNom() {
		return nom;
	}
	
	@RequestMapping(value="environnement", method = RequestMethod.GET)
	public String environnement() {
		return environnement;
	}
	
	@RequestMapping(value="hello", method = RequestMethod.GET)
	public String helloWorld() {
		log.info("passage dans le controller helloWorld");
		return "Hello World !";
	}
	
	@RequestMapping(value="/upload", method = RequestMethod.POST)
	public String upload (@RequestParam MultipartFile multipartfile) throws IOException {
		return new String(multipartfile.getBytes());
	}
	
	@GetMapping("token")
	public String getToken() {
		StringBuilder sb = new StringBuilder();
		sb.append("Bonjour, je m'appelle ");
		sb.append(accessToken.getName()); // Prénom + Nom
		sb.append(". Mon prénom est ");
		sb.append(accessToken.getGivenName()); // Prénom
		sb.append(". Mon nom est ");
		sb.append(accessToken.getFamilyName()); // Nom
		sb.append(". Mon idep est ");
		sb.append(accessToken.getPreferredUsername()); // idep
		sb.append(".\n");
		Access access = accessToken.getRealmAccess();
		if (access != null) {
			sb.append(access.getRoles()
					.stream().collect(Collectors.joining(", ", "Mes rôles sont : ", ".")));  // ensemble des rôles
		}else {
			sb.append("Je n'ai pas de rôles.");
		}
		return sb.toString();
	}
}