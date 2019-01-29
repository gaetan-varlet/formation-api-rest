package fr.insee.formationapirest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.service.VinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/vin")
@Api(tags = { "vin" })
public class VinController {
	
	@Autowired
	VinService vinService;
	
	@ApiOperation(value = "Obtenir tous les vins, ou éventuellement uniquement les vins d'une appellation avec le paramètre appellation")
	@RequestMapping(method = RequestMethod.GET)
	public List<Vin> getAll(@RequestParam(required=false) String appellation){
		if(appellation != null) {
			return vinService.findByAppellation(appellation);
		}
		return vinService.getAll();
	}
	
	@RequestMapping(value="/pageable", method = RequestMethod.GET)
	public Page<Vin> getAllPageable(Pageable p){
		return vinService.pageable(p);
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