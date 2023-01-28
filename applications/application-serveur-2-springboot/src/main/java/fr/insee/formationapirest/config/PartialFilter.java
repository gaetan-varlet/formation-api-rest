package fr.insee.formationapirest.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartialFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(PartialFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		log.info("Filtre uniquement pour les requêtes sur le vin (requête) : {}, {}", req.getMethod(),
				req.getRequestURI());

		chain.doFilter(request, response);

		log.info("Filtre uniquement pour les requêtes sur le vin (Content-Type de la réponse) : {}",
				res.getContentType());
	}
}
