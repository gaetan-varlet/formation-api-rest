package fr.insee.formationapirest.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
public class Trace {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_trace")
	@SequenceGenerator(name = "seq_trace", sequenceName = "formation.trace_id_seq", allocationSize = 1)
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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIdep() {
		return idep;
	}

	public void setIdep(String idep) {
		this.idep = idep;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public String getUrlServeur() {
		return urlServeur;
	}

	public void setUrlServeur(String urlServeur) {
		this.urlServeur = urlServeur;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRequestHeaderAccept() {
		return requestHeaderAccept;
	}

	public void setRequestHeaderAccept(String requestHeaderAccept) {
		this.requestHeaderAccept = requestHeaderAccept;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getResponseHeaderContentType() {
		return responseHeaderContentType;
	}

	public void setResponseHeaderContentType(String responseHeaderContentType) {
		this.responseHeaderContentType = responseHeaderContentType;
	}

	public Long getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(Long timeTaken) {
		this.timeTaken = timeTaken;
	}

	public String getRequestHeaderUserAgent() {
		return requestHeaderUserAgent;
	}

	public void setRequestHeaderUserAgent(String requestHeaderUserAgent) {
		this.requestHeaderUserAgent = requestHeaderUserAgent;
	}

	public String getResponseHeaderContentLength() {
		return responseHeaderContentLength;
	}

	public void setResponseHeaderContentLength(String responseHeaderContentLength) {
		this.responseHeaderContentLength = responseHeaderContentLength;
	}

}
