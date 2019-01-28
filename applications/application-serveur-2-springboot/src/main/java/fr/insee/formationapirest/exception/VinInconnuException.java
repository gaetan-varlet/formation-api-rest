package fr.insee.formationapirest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VinInconnuException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public VinInconnuException(String message) {
		super(message);
	}
}
