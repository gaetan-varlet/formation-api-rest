package fr.insee.formationapirest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.repository.VinRepository;

@RestController
@RequestMapping("/vin")
public class VinController {
	
	@Autowired
	VinRepository vinRepository;

	@RequestMapping(method = RequestMethod.GET)
	public List<Vin> getAll(){
		return vinRepository.findAll();
	}
	
	@RequestMapping(value= "/{id}", method = RequestMethod.GET)
	public Vin getById(@PathVariable Integer id){
		return vinRepository.findById(id).orElse(null);
	}
	
	@RequestMapping(value= "/{id}", method = RequestMethod.DELETE)
	public void deleteById(@PathVariable Integer id){
		if(vinRepository.existsById(id)) { // renvoie un boolean (true si l'objet existe, false sinon)
			vinRepository.deleteById(id);
		}
	}
	
	@RequestMapping (method = RequestMethod.POST)
	public Vin add(@RequestBody Vin vin){
	    // ajouter un contrôle pour s'assurer que l'id n'est pas renseigné ou passer par un DTO
		return vinRepository.save(vin);
	}
	
	@RequestMapping (method = RequestMethod.PUT)
	public Vin update(@RequestBody Vin vin){
		if(vinRepository.existsById(vin.getId())) {
			return vinRepository.save(vin);
		}
		return null;
	}
	
}