package fr.insee.formationapirest.controller;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.formationapirest.FormationApiRestApplication;
import fr.insee.formationapirest.model.Vin;

//indique qui exécute les tests
@RunWith(SpringRunner.class)
// lance l'application complète sur un port
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FormationApiRestApplication.class)
@AutoConfigureMockMvc
// précise le nom du fichier de properties s'il est différent du nom par défaut
@TestPropertySource(locations = "classpath:formation-api-rest.properties")
//permet de rafraîchir la base entre chaque test
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
//@Sql({"/utilisateur.sql"})
public class VinControllerIntegrationTest {
	
	@Autowired
	private MockMvc mvc;
	
	// le mapper permet de convertir nos données en JSON lorsque nous voulons invoquer notre API
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void DoitRecupererTousLesVins() throws Exception{
		mvc.perform(get("/vin"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.length()",is(4)))
		.andExpect(jsonPath("$.[0].chateau",is("Château Margaux")))
		;
	}
	
	@Test
	public void DoitRecupererUnVin() throws Exception{
		mvc.perform(get("/vin/1"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id",is(1)))
		.andExpect(jsonPath("$.chateau",is("Château Margaux")))
		;
	}
	
	@Test
	public void DoitRecupererUnVinInexistant() throws Exception{
		mvc.perform(get("/vin/50"))
		.andExpect(status().isNotFound())
		;
	}
	
	@Test
	public void DoitRecupererUnVinInvalide() throws Exception{
		mvc.perform(get("/vin/0"))
		.andExpect(status().isBadRequest())
		;
	}
	
	@Test
	public void DoitAjouterVin() throws Exception{
		Vin vin1 = new Vin(); vin1.setChateau("Château 1"); vin1.setAppellation("Saint-Julien"); vin1.setPrix(10.0); 
		
		mvc.perform(post("/vin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(vin1)))
		.andExpect(status().isCreated());
		
		mvc.perform(get("/vin"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.length()",is(5)))
		.andExpect(jsonPath("$.[1].chateau",is("Château Cantemerle")))
		.andExpect(jsonPath("$.[1].prix",is(30.5)))
		.andExpect(jsonPath("$.[4].chateau",is("Château 1")))
		.andExpect(jsonPath("$.[4].appellation",is("Saint-Julien")))
		.andExpect(jsonPath("$.[4].id",is(5)))
		;
	}
	
	@Test
	public void NeDoitPasAjouterVinInvalide() throws Exception{
		// on essaie de créer un vin en fournissant un id qui existe, ce qi est interdit
		Vin vin1 = new Vin(); vin1.setId(1); vin1.setChateau("Château 1"); vin1.setAppellation("Saint-Julien"); vin1.setPrix(10.0);
		
		mvc.perform(post("/vin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(vin1)))
		.andExpect(status().isBadRequest());
		
		mvc.perform(get("/vin"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.length()",is(4)))
		.andExpect(jsonPath("$.[1].chateau",is("Château Cantemerle")))
		.andExpect(jsonPath("$.[1].prix",is(30.5)))
		;
	}
	
	@Test
	public void DoitMettreAJourVin() throws Exception{
		Vin vin1 = new Vin(); vin1.setId(1); vin1.setChateau("Château 1"); vin1.setAppellation("Saint-Julien"); vin1.setPrix(10.0);
		
		mvc.perform(put("/vin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(vin1)))
		.andExpect(status().isOk());
		
		mvc.perform(get("/vin/1"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.chateau",is("Château 1")))
		.andExpect(jsonPath("$.prix",is(10.0)))
		;
	}
	
	@Test
	public void NeDoitPasMettreAJourVinInexistant() throws Exception{
		Vin vin1 = new Vin(); vin1.setId(12345); vin1.setChateau("Château 1"); vin1.setAppellation("Saint-Julien"); vin1.setPrix(10.0);
		
		mvc.perform(put("/vin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(vin1)))
		.andExpect(status().isNotFound());
	}
	
}
