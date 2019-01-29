package fr.insee.formationapirest.service;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.repository.VinRepository;

@RunWith(MockitoJUnitRunner.class)
public class VinServiceTest {
	
	@InjectMocks
	private VinService vinService;
	
	@Mock
	private VinRepository vinRepository;
	
	@Before
	public void setUp() {
		Vin vin1 = new Vin(); vin1.setId(1); vin1.setChateau("Château 1"); vin1.setAppellation("Saint-Julien"); vin1.setPrix(10.0);
		Vin vin2 = new Vin(); vin2.setId(2); vin2.setChateau("Château 2"); vin2.setAppellation("Pomerol"); vin2.setPrix(25.0);
		List<Vin> liste = Arrays.asList(vin1, vin2);
		
		Mockito.when(vinRepository.findAll()).thenReturn(liste);
	}
	
	@Test
	public void recupererTousLesVins() {
		List<Vin> vinRetournes = vinService.getAll();
		assertEquals(2, vinRetournes.size());
	}
	
}
