package fr.insee.formationapirest.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import fr.insee.formationapirest.exception.VinInconnuException;
import fr.insee.formationapirest.exception.VinInvalideException;
import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.repository.VinRepository;

@Service
public class VinService {
	
	@Autowired
	VinRepository vinRepository;
	
	public List<Vin> getAll(String appellation){
		if(appellation != null) {
			return vinRepository.findByAppellation(appellation);
		}
		return vinRepository.findAll();
	}
	
	public Iterable<Vin> get(Predicate predicate){
		return vinRepository.findAll(predicate);
	}
	
	public Vin getById(Integer id){
		if(id>0) {
			if(vinRepository.existsById(id)) {
				return vinRepository.findById(id).orElse(null);
			} else {
				throw new VinInconnuException("le vin avec l'id "+ id + " n'existe pas");
			}
		} else {
			throw new VinInvalideException("l'id renseigné (" + id + ") n'est pas valide");
		}
	}
	
	public void deleteById(Integer id){
		if(vinRepository.existsById(id)) {
			vinRepository.deleteById(id);
		} else {
			throw new VinInconnuException("le vin avec l'id "+ id + " n'existe pas");
		}
	}
	
	public Vin add(Vin vin){
	   if(controleValiditeVin(vin)) {
			return vinRepository.save(vin);
	   } else {
	   	throw new VinInvalideException("le vin renseigné (" + vin + ") n'est pas valide");
	   }
	}
	
	public Vin update(Vin vin){
		if(vinRepository.existsById(vin.getId())) {
			return vinRepository.save(vin);
		} else {
			throw new VinInconnuException("le vin avec l'id "+ vin.getId() + " n'existe pas");	
		}
	}
	
	public Page<Vin> pageable(Pageable p) {
		return vinRepository.findAll(p);
	}
	
	protected boolean controleValiditeVin(Vin vin) {
		if(vin.getChateau() == null || vin.getChateau().length()<1 || vin.getChateau().length()>50) {
			return false;
		} else if(vin.getAppellation() == null || vin.getAppellation().length()<1 || vin.getAppellation().length()>50) {
			return false;
		} else if (vin.getPrix() != null && vin.getPrix() < 0) {
			return false;
		}
		return true;
	}
	
}