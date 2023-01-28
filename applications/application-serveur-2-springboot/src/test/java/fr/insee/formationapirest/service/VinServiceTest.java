package fr.insee.formationapirest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.repository.VinRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VinServiceTest {

	@InjectMocks
	private VinService vinService;

	@Mock
	private VinRepository vinRepository;

	@BeforeEach
	public void setUp() {
		Vin vin1 = new Vin();
		vin1.setId(1);
		vin1.setChateau("Château 1");
		vin1.setAppellation("Saint-Julien");
		vin1.setPrix(10.0);
		Vin vin2 = new Vin();
		vin2.setId(2);
		vin2.setChateau("Château 2");
		vin2.setAppellation("Pomerol");
		vin2.setPrix(25.0);
		List<Vin> liste = Arrays.asList(vin1, vin2);

		Mockito.when(vinRepository.findAll()).thenReturn(liste);
	}

	@Test
	public void recupererTousLesVins() {
		List<Vin> vinRetournes = vinService.findAll(null);
		assertThat(vinRetournes).hasSize(2);
		assertThat(vinRetournes.get(0).getChateau()).isEqualTo("Château 1");
	}

	@Test
	public void controleValiditeVinTest() {
		assertFalse(vinService.controleValiditeVin(null));
		assertTrue(vinService.controleValiditeVin(new Vin(1, "Château Lascombes", "Margaux", 100d)));
		assertTrue(vinService.controleValiditeVin(new Vin(null, "Château Lascombes", "Margaux", 100d)));

		assertFalse(vinService.controleValiditeVin(new Vin(1, null, "Margaux", 100d)));
		assertFalse(vinService.controleValiditeVin(new Vin(1, "", "Margaux", 100d)));
		assertTrue(vinService.controleValiditeVin(new Vin(1, "C", "Margaux", 100d)));
		assertTrue(vinService.controleValiditeVin(
				new Vin(1, "12345678901234567890123456789012345678901234567890", "Margaux", 100d)));
		assertFalse(vinService.controleValiditeVin(
				new Vin(1, "123456789012345678901234567890123456789012345678901", "Margaux", 100d)));

		assertFalse(vinService.controleValiditeVin(new Vin(1, "Château Lascombes", null, 100d)));
		assertFalse(vinService.controleValiditeVin(new Vin(1, "Château Lascombes", "", 100d)));
		assertTrue(vinService.controleValiditeVin(new Vin(1, "Château Lascombes", "M", 100d)));
		assertTrue(vinService.controleValiditeVin(
				new Vin(1, "Château Lascombes", "12345678901234567890123456789012345678901234567890", 100d)));
		assertFalse(vinService.controleValiditeVin(
				new Vin(1, "Château Lascombes", "123456789012345678901234567890123456789012345678901", 100d)));

		assertFalse(vinService.controleValiditeVin(new Vin(1, "Château Lascombes", "Margaux", null)));
		assertFalse(vinService.controleValiditeVin(new Vin(1, "Château Lascombes", "Margaux", -5d)));
		assertFalse(vinService.controleValiditeVin(new Vin(1, "Château Lascombes", "Margaux", -0.1d)));
		assertTrue(vinService.controleValiditeVin(new Vin(1, "Château Lascombes", "Margaux", 0d)));
		assertTrue(vinService.controleValiditeVin(new Vin(1, "Château Lascombes", "Margaux", 0.1d)));
	}

}