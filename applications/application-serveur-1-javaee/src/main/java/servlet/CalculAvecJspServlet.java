package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/calcul-avec-jsp")
public class CalculAvecJspServlet extends HttpServlet {
	
	public void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException{
		int nombre1 = Integer.parseInt(request.getParameter("nombre1"));
		int nombre2 = Integer.parseInt(request.getParameter("nombre2"));
		int somme = nombre1+nombre2;
		int produit = nombre1*nombre2;
		request.setAttribute( "somme", somme );
		request.setAttribute( "produit", produit );

		response.setContentType("text/html");
		response.setCharacterEncoding( "UTF-8" );
		this.getServletContext().getRequestDispatcher( "/WEB-INF/jsp/reponse-calcul.jsp" )
		.forward( request, response );
	}
}
