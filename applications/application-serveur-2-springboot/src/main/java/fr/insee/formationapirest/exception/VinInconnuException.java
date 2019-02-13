package fr.insee.formationapirest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class VinInconnuException extends RuntimeException {
	
	public VinInconnuException(String message) {
		super(message);
	}

	public VinInconnuException(Integer id) {
		super("Le vin avec l'id "+id+" n'existe pas");
	}
}
