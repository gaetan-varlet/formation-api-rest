package resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
