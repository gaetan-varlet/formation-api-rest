package resources;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import model.Adresse;
import model.Personne;

@Path("")
public class CalculResource2 {
	
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@GET
	@Path("/test-json")
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
	
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@GET
	@Path("/test-json-liste")
	public List<Personne> calcul1(){
		Personne personne1 = new Personne();
		personne1.setPrenom("Gaëtan");
		personne1.setAge(30);
		Adresse adresse1 = new Adresse();
		adresse1.setNomCommune("Montrouge");
		adresse1.setCodePostal("92120");
		personne1.setAdresse(adresse1);
		
		Personne personne2 = new Personne();
		personne2.setPrenom("Thibaut");
		personne2.setAge(23);
		Adresse adresse2 = new Adresse();
		adresse2.setNomCommune("Saint-Quentin");
		adresse2.setCodePostal("02100");
		personne2.setAdresse(adresse2);
		
		
		return Arrays.asList(personne1, personne2);
	}
	
	@Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
	@GET
	@Path("/test-xml")
	public Personne calcul2(){
		Personne personne1 = new Personne();
		personne1.setPrenom("Gaëtan");
		personne1.setAge(30);
		Adresse adresse1 = new Adresse();
		adresse1.setNomCommune("Montrouge");
		adresse1.setCodePostal("92120");
		personne1.setAdresse(adresse1);
		return personne1;
	}
	
	@Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
	@GET
	@Path("/test-xml-liste")
	public List<Personne> calcul3(){
		Personne personne1 = new Personne();
		personne1.setPrenom("Gaëtan");
		personne1.setAge(30);
		Adresse adresse1 = new Adresse();
		adresse1.setNomCommune("Montrouge");
		adresse1.setCodePostal("92120");
		personne1.setAdresse(adresse1);
		
		Personne personne2 = new Personne();
		personne2.setPrenom("Thibaut");
		personne2.setAge(23);
		Adresse adresse2 = new Adresse();
		adresse2.setNomCommune("Saint-Quentin");
		adresse2.setCodePostal("02100");
		personne2.setAdresse(adresse2);
		
		
		return Arrays.asList(personne1, personne2);
	}
}
