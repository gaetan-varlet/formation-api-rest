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

		HttpServletRequest httpRequest = (HttpServletRequest) request;
//		HttpServletResponse httpResponse = (HttpServletResponse) response;
		log.info("Début de la requête " + httpRequest.getRequestURI());
		
//		KeycloakSecurityContext securityContext = (KeycloakSecurityContext) httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
//		if(securityContext !=null) {
//			AccessToken token = securityContext.getToken();
//			log.info(token.getPreferredUsername()); // idep
//			log.info(token.getName()); // Prénom Nom
//			log.info(token.getGivenName()); // Prénom
//			log.info(token.getFamilyName()); // Nom
//			token.getRealmAccess().getRoles().forEach(x -> log.info(x)); // ensemble des rôles
			chain.doFilter(request, response);
//		} else {
//			httpResponse.sendError(403, "accès interdit");
//		}

		log.info("Fin de la requête " + httpRequest.getRequestURI());
	}
}
