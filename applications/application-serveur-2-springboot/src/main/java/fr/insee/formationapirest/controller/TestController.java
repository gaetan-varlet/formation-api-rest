package fr.insee.formationapirest.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	private static final Logger log = LogManager.getLogger();
	
	@RequestMapping(value="hello", method = RequestMethod.GET)
	public String helloWorld() {
		log.info("passage dans le controller helloWorld");
		return "Hello World !";
	}
}