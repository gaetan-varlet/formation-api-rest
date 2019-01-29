package fr.insee.formationapirest.controller;

import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.service.VinService;


@RunWith(SpringRunner.class)
//teste un seul controller sans à avoir à charger toute l’application Spring Boot et donc gagne en rapidité d’exécution
@WebMvcTest(VinController.class)
public class VinController {
	
	@Autowired
	private MockMvc mvc;

	@MockBean
	private VinService vinService;

	// le mapper permet de convertir nos données en JSON lorsque nous voulons invoquer notre API
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void DoitRecupererTousLesVins() throws Exception{

		Vin vin1 = new Vin(); vin1.setId(1); vin1.setChateau("Château 1"); vin1.setAppellation("Saint-Julien"); vin1.setPrix(10.0);
		Vin vin2 = new Vin(); vin2.setId(2); vin2.setChateau("Château 2"); vin2.setAppellation("Pomerol"); vin2.setPrix(25.0);
		List<Vin> liste = Arrays.asList(vin1, vin2);

		given(vinService.getAll()).willReturn(liste);

		mvc.perform(get("/vin"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.length()",is(2)))
		.andExpect(jsonPath("$.[0].appellation",is("Saint-Julien")));
	}

	
}
