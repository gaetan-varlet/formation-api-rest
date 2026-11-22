package resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import model.Calcul;

@Path("/calcul-jersey")
public class CalculResource {
	
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@GET
	public Calcul calcul(@QueryParam("nombre1") Integer nombre1,
			@QueryParam("nombre2") Integer nombre2){
		Calcul calcul = new Calcul();
		calcul.setSomme(nombre1+nombre2);
		calcul.setProduit(nombre1*nombre2);
		return calcul;
	}
}
