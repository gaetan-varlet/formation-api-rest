package resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import model.Adresse;
import model.Personne;

@Path("/test")
public class CalculResource2 {
	
	@Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
	@GET
	public Personne calcul(){
		Personne personne1 = new Personne();
		personne1.setPrenom("Gaëtan");
		personne1.setAge(30);
		Adresse adresse1 = new Adresse();
		adresse1.setNomCommune("Montrouge");
		adresse1.setCodePostal("92120");
		personne1.setAdresse(adresse1);
		return personne1;
	}
}
