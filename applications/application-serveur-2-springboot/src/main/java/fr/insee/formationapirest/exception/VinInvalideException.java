package fr.insee.formationapirest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VinInvalideException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public VinInvalideException(String message) {
		super(message);
	}
}
