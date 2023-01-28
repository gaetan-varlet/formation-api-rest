package fr.insee.formationapirest.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.service.VinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/vin")
@Tag(name = "vin")
public class VinController {

	private static final Logger log = LoggerFactory.getLogger(VinController.class);

	@Autowired
	private VinService vinService;

	@GetMapping("appellation")
	public List<String> getListeAppellation() {
		return vinService.getListeAppellation();
	}

	@Operation(summary = "Obtenir tous les vins, ou éventuellement uniquement les vins d'une appellation avec le paramètre appellation")
	@GetMapping
	public List<Vin> findAll(@RequestParam(required = false) String appellation) {
		return vinService.findAll(appellation);
	}

	@GetMapping("/csv")
	public void getAllCsv(HttpServletResponse response) throws IOException {
		String nomFichier = "ma-cave";

		// en-tête qui permet de préciser au navigateur s'il doit afficher le contenu
		// (inline) ou le télécharger (attachment)
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", nomFichier + ".csv"));
		// aide le navigateur à savoir quel logiciel peut ouvrir le type de contenu
		// téléchargé
		// et suggère un logiciel pour l'ouvrir une fois le téléchargement terminé
		response.setContentType("text/csv");
		response.setCharacterEncoding("UTF-8");

		vinService.ecrireVinsDansCsv(response.getWriter(), vinService.findAll(null));
	}

	@GetMapping("pageable")
	public Page<Vin> getAllPageable(Pageable p) {
		return vinService.pageable(p);
	}

	@GetMapping("{id}")
	public Vin getById(@PathVariable Integer id) {
		return vinService.getById(id);
	}

	@DeleteMapping("{id}")
	public void deleteById(@PathVariable Integer id) {
		vinService.deleteById(id);
	}

	@PostMapping
	public ResponseEntity<Void> add(@RequestBody Vin vin) {
		Vin vinAjoute = vinService.add(vin);
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(vinAjoute.getId())
				.toUri();
		return ResponseEntity.created(location).build();
	}

	@PutMapping
	public Vin update(@RequestBody Vin vin) {
		return vinService.update(vin);
	}

	@GetMapping("long/{number}")
	public boolean longFonction(@PathVariable int number) throws Exception {
		log.info("avant long service");
		boolean b = vinService.longService(number);
		log.info("après long service");
		return b;
	}

}