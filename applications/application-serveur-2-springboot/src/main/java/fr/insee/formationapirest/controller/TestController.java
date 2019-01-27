package fr.insee.formationapirest.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TestController {
	
	private static final Logger log = LogManager.getLogger();
	
	@Value("${propertyNonSurchargee}")
	private String propertyCoucou;
	
	@RequestMapping(value="propertyNonSurchargee", method = RequestMethod.GET)
	public String propertyNonSurchargee() {
		return propertyCoucou;
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