package fr.insee.formationapirest.config;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.stereotype.Component;

import fr.insee.formationapirest.model.Trace;
import fr.insee.formationapirest.repository.TraceRepository;

@Component
public class LogConf implements HttpTraceRepository{
	
	@Autowired
	private TraceRepository traceRepository;
	
	@Autowired
	private Principal principal;
	
    @Override
    public List<HttpTrace> findAll() {
        // méthode appelée sur l'url /httptrace : on ne renvoie rien
        return Collections.emptyList();
    }

    @Override
    public void add(HttpTrace httpTrace) {
        // méthode appelée à chaque requête HTTP : on affiche dans la console les infos
        // il n'y a ni le corps de la requête, ni le corps de la réponse
        // si besoin, on peut utiliser une bibliothèque comme Logbook
        
        Trace trace = new Trace();
        trace.setIdep(principal.getName());
        trace.setTime(LocalDateTime.now());
        trace.setUrlServeur(httpTrace.getRequest().getUri().getAuthority());
        trace.setMethod(httpTrace.getRequest().getMethod());
        trace.setEndpoint(httpTrace.getRequest().getUri().getPath());
        trace.setRequestHeaderAccept(Optional.ofNullable(httpTrace.getRequest().getHeaders().get("accept")).orElse(Collections.emptyList()).toString());
        trace.setRequestHeaderUserAgent(Optional.ofNullable(httpTrace.getRequest().getHeaders().get("user-agent")).orElse(Collections.emptyList()).toString());
        trace.setResponseStatus(String.valueOf(httpTrace.getResponse().getStatus()));
        trace.setResponseHeaderContentType(Optional.ofNullable(httpTrace.getResponse().getHeaders().get("content-type")).orElse(Collections.emptyList()).toString());
        trace.setResponseHeaderContentType(Optional.ofNullable(httpTrace.getResponse().getHeaders().get("content-length")).orElse(Collections.emptyList()).toString());
        trace.setTimeTaken(httpTrace.getTimeTaken());
        traceRepository.save(trace);
    }
}