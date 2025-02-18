package fr.insee.formationapirest.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Trace {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String idep;
	private LocalDateTime time;
	private String urlServeur;
	private String endpoint;
	private String method;
	private String requestHeaderAccept;
	private String requestHeaderUserAgent;
	private String responseStatus;
	private String responseHeaderContentType;
	private String responseHeaderContentLength;
	private Long timeTaken;

}
