package fr.insee.formationapirest.service;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
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
			// si l'id n'est pas renseigné ou si l'id renseigné n'existe pas, alors on crée le vin
			if(vin.getId() == null || !vinRepository.existsById(vin.getId())){
				return vinRepository.save(vin);
			} else {
				throw new VinInvalideException("le vin renseigné (" + vin + ") existe déjà, vous ne pouvez pas le créer");
			}
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

	protected boolean controleValiditeVinOLD(Vin vin) {
		if (vin == null)
			return false;
		if (!vin.getChateau().isPresent() || vin.getChateau().get().length() < 1 || vin.getChateau().get().length() > 50) {
			return false;
		} else if (!vin.getAppellation().isPresent() || vin.getAppellation().get().length() < 1
			|| vin.getAppellation().get().length() > 50) {
			return false;
		} else if (!vin.getPrix().isPresent() || vin.getPrix().get() < 0) {
			return false;
		}
		return true;
	}
	
	protected boolean controleValiditeVin(Vin vin) {
		return Optional.ofNullable(vin)
			.filter(this::isChateauValid)
			.filter(this::isAppellationValid)
			.filter(this::isPrixValid)
			.isPresent();
	}

	protected boolean isChateauValid(Vin vin) {
		return Optional.ofNullable(vin).flatMap(Vin::getChateau)
			.filter(c -> c.length() >= 1 && c.length() <= 50)
			.isPresent();
	}

	protected boolean isAppellationValid(Vin vin) {
		return Optional.ofNullable(vin).flatMap(Vin::getAppellation)
			.filter(a -> a.length() >= 1 && a.length() <= 50)
			.isPresent();
	}

	protected boolean isPrixValid(Vin vin) {
		return Optional.ofNullable(vin).flatMap(Vin::getPrix)
			.filter(p -> p >= 0)
			.isPresent();
	}
	
	public void ecrireVinsDansCsv(Writer writer, List<Vin> listeAEcrire) throws IOException {
		// Création du writer openCSV qui va écrire dans le writer fourni en paramètre
		ICSVWriter csvWriter = new CSVWriterBuilder(writer)
				.withSeparator(';') // séparateur point-virgule (virgule par défaut)
				.withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER) // caractère autour de chaque attribut (doubles quotes par défaut)
				.build();
		
		listeAEcrire.forEach(vin -> {
			List<String> ligne = new ArrayList<>();
			ligne.add(vin.getChateau().get());
			ligne.add(vin.getAppellation().get());
			ligne.add(String.valueOf(vin.getPrix().get()));
			csvWriter.writeNext(ligne.toArray(new String[ligne.size()]));
		});
		csvWriter.close();
	}
	
}