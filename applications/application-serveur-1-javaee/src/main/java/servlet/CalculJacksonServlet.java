package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.Calcul;

@WebServlet("/calcul-jackson")
public class CalculJacksonServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int nombre1 = Integer.parseInt(request.getParameter("nombre1"));
		int nombre2 = Integer.parseInt(request.getParameter("nombre2"));
		Calcul calcul = new Calcul();
		calcul.setSomme(nombre1 + nombre2);
		calcul.setProduit(nombre1 * nombre2);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(out, calcul);
	}
}
