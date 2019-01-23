package fr.insee.formationapirest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.service.VinService;

@RestController
@RequestMapping("/vin")
public class VinController {
	
	@Autowired
	VinService vinService;

	@RequestMapping(method = RequestMethod.GET)
	public List<Vin> getAll(){
		return vinService.getAll();
	}
	
	@RequestMapping(value= "/{id}", method = RequestMethod.GET)
	public Vin getById(@PathVariable Integer id){
		return vinService.getById(id);
	}
	
	@RequestMapping(value= "/{id}", method = RequestMethod.DELETE)
	public void deleteById(@PathVariable Integer id){
		vinService.deleteById(id);
	}
	
	@RequestMapping (method = RequestMethod.POST)
	public Vin add(@RequestBody Vin vin){
		return vinService.add(vin);
	}
	
	@RequestMapping (method = RequestMethod.PUT)
	public Vin update(@RequestBody Vin vin){
		return vinService.update(vin);
	}
	
}