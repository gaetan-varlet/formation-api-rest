package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/calcul")
public class CalculServlet extends HttpServlet {
	
	public void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException{
		int nombre1 = Integer.parseInt(request.getParameter("nombre1"));
		int nombre2 = Integer.parseInt(request.getParameter("nombre2"));
		int somme = nombre1+nombre2;
		int produit = nombre1*nombre2;

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("<meta charset=\"utf-8\" />");
		out.println("<title>Calcul</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<p>Calcul avec affichage de la réponse dans la servlet :</p>");
		out.println("<p>La somme de "+nombre1+" et "+nombre2+" est égale à "+somme+".</p>");
		out.println("<p>Le produit de "+nombre1+" et "+nombre2+" est égal à "+produit+".</p>");
		out.println("</body>");
		out.println("</html>");
	}
}
