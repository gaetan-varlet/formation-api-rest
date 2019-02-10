package fr.insee.formationapirest.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class SimpleFilter implements Filter {	
	private static final Logger log = LoggerFactory.getLogger(SimpleFilter.class);

	@Override
	public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		log.info("Début de la requête " + req.getRequestURI());

		chain.doFilter(request, response);

		log.info("Fin de la requête " + req.getRequestURI());
	}
}
