package fr.insee.formationapirest.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
}