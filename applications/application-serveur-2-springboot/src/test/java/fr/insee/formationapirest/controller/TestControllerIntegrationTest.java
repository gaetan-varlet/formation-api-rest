package fr.insee.formationapirest.controller;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import fr.insee.formationapirest.FormationApiRestApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FormationApiRestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:vin.properties")
public class TestControllerIntegrationTest {
	
	@Autowired
	private MockMvc mvc;
	
	@Test
	public void DoitRecupererNomPasAuthentifie() throws Exception{
		mvc.perform(get("/mon-nom"))
		.andExpect(status().isUnauthorized())
		;
	}
	
	@Test
	@WithMockUser
	public void DoitRecupererNom() throws Exception{
		mvc.perform(get("/mon-nom"))
		.andExpect(status().isOk())
		.andExpect(content().string("Gaetan"));
		;
	}
	
	@Test
	@WithMockUser(roles="TOUCAN_ADMIN")
	public void DoitRecupererEnvironnement() throws Exception{
		mvc.perform(get("/environnement"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$",is("environnement local")))
		;
	}
	
	@Test
	public void DoitRecupererEnvironnementPasAuthentifie() throws Exception{
		mvc.perform(get("/environnement"))
		.andExpect(status().isUnauthorized())
		;
	}
	
	@Test
	@WithMockUser(roles="MAUVAIS_ROLE")
	public void DoitRecupererEnvironnementRoleInvalide() throws Exception{
		mvc.perform(get("/environnement"))
		.andExpect(status().isForbidden())
		;
	}
	
	@Test
	public void uploadFichier() throws Exception{
		MockMultipartFile firstFile = new MockMultipartFile("multipartfile", "filename.txt", "text/plain", "coucou".getBytes());
		
		mvc.perform(multipart("/upload")
				.file(firstFile))
		.andExpect(status().isOk())
		.andExpect(content().string("coucou"));
	}
	
	@Test
	public void uploadFichier2() throws Exception{
		
		FileInputStream fis = new FileInputStream("src/test/resources/toto.txt");
		MockMultipartFile multipartFile = new MockMultipartFile("multipartfile", fis);
		
		mvc.perform(multipart("/upload")
				.file(multipartFile))
		.andExpect(status().isOk())
		.andExpect(content().string("Je m'appelle Toto"));
	}
	
}
