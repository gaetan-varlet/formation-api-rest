package fr.insee.formationapirest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VinInvalideException extends RuntimeException {
	
	public VinInvalideException(String message) {
		super(message);
	}
}
